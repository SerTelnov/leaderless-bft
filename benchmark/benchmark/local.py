import subprocess
import uuid
from os.path import basename, splitext
from time import sleep

from benchmark.commands import CommandMaker
from benchmark.config import Key, LocalCommittee, BenchParameters, BenchmarkConfig, ConfigError
from benchmark.logs import LogParser, ParseError
from benchmark.utils import Print, BenchError, PathMaker


class LocalBench:
    BASE_PORT = 3000

    def __init__(self, bench_parameters_dict):
        try:
            self.duration = bench_parameters_dict['duration']
            self.bench_parameters = BenchParameters(bench_parameters_dict)
        except ConfigError as e:
            raise BenchError('Invalid nodes or bench parameters', e)

    def __getattr__(self, attr):
        return getattr(self.bench_parameters, attr)

    def _background_run(self, command, log_file):
        name = splitext(basename(log_file))[0]
        cmd = f'{command} 2> {log_file}'
        subprocess.run(['tmux', 'new', '-d', '-s', name, cmd], check=True)

    def _kill_nodes(self):
        try:
            cmd = CommandMaker.kill().split()
            subprocess.run(cmd, stderr=subprocess.DEVNULL)
        except subprocess.SubprocessError as e:
            raise BenchError('Failed to kill testbed', e)

    def run(self, debug=False):
        assert isinstance(debug, bool)
        Print.heading('Starting local benchmark')

        # Kill any previous testbed.
        self._kill_nodes()

        nodes = self.bench_parameters.nodes[0]

        try:
            Print.info('Setting up testbed...')

            # Cleanup all files.
            cmd = f'{CommandMaker.clean_logs()} ; {CommandMaker.cleanup()}'
            subprocess.run([cmd], shell=True, stderr=subprocess.DEVNULL)
            sleep(0.5)  # Removing the store may take time.

            # Recompile the latest code.
            # cmd = CommandMaker.compile().split()
            # subprocess.run(cmd, check=True)

            # Create alias for the client and nodes binary.
            cmd = CommandMaker.alias_binaries(PathMaker.binary_path())
            subprocess.run([cmd], shell=True)

            # Generate configuration files.
            keys = []
            for i in range(nodes):
                public_key = uuid.uuid4()
                keys.append(Key(f'peer-{i}', public_key))

            committee = LocalCommittee(keys, self.BASE_PORT)
            committee.print(PathMaker.committee_file())

            coordinator_pk = str(uuid.uuid4())
            benchmark_config = BenchmarkConfig(
                committee,
                self.bench_parameters,
                coordinator_pk
            )
            benchmark_config.print(PathMaker.benchmark_file())

            failed_nodes = keys[-self.bench_parameters.faults:] if self.bench_parameters.faults > 0 else []
            good_nodes = keys[:-self.bench_parameters.faults] if self.bench_parameters.faults > 0 else keys

            # Run the peers (except the faulty ones).
            for i, key in enumerate(good_nodes):
                cmd = CommandMaker.run_peer(
                    key.name,
                    f'-pk {key.secret} -config {PathMaker.benchmark_file()}')
                log_file = PathMaker.peer_log_file(key.name)
                self._background_run(cmd, log_file)

            # Run the failed peers
            for i, key in enumerate(failed_nodes):
                cmd = CommandMaker.run_peer(
                    key.name,
                    f'-f -pk {key.secret} -config {PathMaker.benchmark_file()}')
                log_file = PathMaker.peer_log_file(key.name)
                self._background_run(cmd, log_file)

            # Run coordinator
            cmd = CommandMaker.run_coordinator(
                f'-config {PathMaker.benchmark_file()}')
            log_file = PathMaker.coordinator_log_file()
            self._background_run(cmd, log_file)

            # Wait for all transactions to be processed.
            Print.info(f'Running benchmark ({self.duration} sec)...')
            self.countdown_timer(self.duration)
            self._kill_nodes()

            # Parse logs and return the parser.
            Print.info('Parsing logs...')
            return LogParser.process(PathMaker.logs_path(), faults=self.faults)

        except (subprocess.SubprocessError, ParseError) as e:
            self._kill_nodes()
            raise BenchError('Failed to run benchmark', e)

    def countdown_timer(self, seconds):
        if seconds < 60:
            sleep(seconds)
            return

        while seconds > 0:
            print(f"{seconds} seconds remaining")
            sleep(30 if seconds > 30 else seconds)
            seconds -= 30

        print("Time's up!")
