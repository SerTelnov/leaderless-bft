from datetime import datetime
from glob import glob
from multiprocessing import Pool
from os.path import join
from re import findall, search
from statistics import mean


class ParseError(Exception):
    pass


class LogParser:

    def __init__(self, peers, faults=0):
        self.faults = faults
        if isinstance(faults, int):
            self.committee_size = len(peers)
        else:
            self.committee_size = '?'

        # Parse the peers logs.
        try:
            with Pool() as p:
                results = p.map(self._parse_peers, peers)
        except (ValueError, IndexError, AttributeError) as e:
            raise ParseError(f'Failed to parse nodes\' logs: {e}')
        proposals, commits = zip(*results)
        self.proposals = self._merge_dicts(proposals)
        self.commits = self._merge_dicts(commits)

        self.merged_peers_info = self._merge_commits(self.commits['heights'], self.proposals['heights'])

    @staticmethod
    def _merge_commits(commits, proposals):
        peers = {}

        for height, commit in commits.items():
            for pk, date in commit.items():
                if pk == 'hash':
                    continue

                if pk not in peers:
                    peers[pk] = []

                duration = date - proposals[height][pk]
                peers[pk].append(duration)

        return peers

    def _merge_dicts(self, dicts):
        dd = {'hashes': {}, 'heights': {}}

        for d in dicts:
            dd['hashes'] = self._simple_merge_dicts([dd['hashes'], d['hashes']])

            for h, info in d['heights'].items():
                if h not in dd['heights']:
                    dd['heights'][h] = {'hash': set()}
                for pk, peer_info in info.items():
                    dd['heights'][h][pk] = peer_info['date']
                    dd['heights'][h]['hash'].add(peer_info['hash'])

        return dd

    @staticmethod
    def _simple_merge_dicts(dicts):
        dd = {}

        for d in dicts:
            for key, value in d.items():
                if key not in dd:
                    dd[key] = value
                else:
                    dd[key] = min(dd[key], d[key])

        return dd

    @staticmethod
    def _merge_log_result(input):
        merged = {'hashes': {}, 'heights': {}}

        for x in input:
            for y in x:
                if len(y) == 0:
                    continue

                h, pk, height, date = y

                if h not in merged['hashes'] or merged['hashes'][h] > date:
                    merged['hashes'][h] = date
                if height not in merged['heights']:
                    merged['heights'][height] = {}

                merged['heights'][height][pk] = {'hash': h, 'date': date}

        return merged

    def _parse_peers(self, log):
        if search(r'(?:panicked|Error|Exception)', log) is not None:
            raise ParseError('Peer(s) panicked')

        tmp = findall(r'(.*Z) .* PROPOSE_BLOCK \[pk=(.*),n=\d+\] on Height:(\d+) hash=(-?\d+)', log)
        tmp = [(h, pk, height, self._to_posix(t)) for t, pk, height, h in tmp]
        proposals = self._merge_log_result([tmp])

        tmp = findall(r'(.*Z) .* COMMIT \[pk=(.*),n=\d+\] on Height:(\d+) hash=(-?\d+)', log)
        tmp = [(h, pk, height, self._to_posix(t)) for t, pk, height, h in tmp]
        commits = self._merge_log_result([tmp])

        return proposals, commits

    @staticmethod
    def _to_posix(string):
        x = datetime.fromisoformat(string.replace('Z', '+00:00'))
        return datetime.timestamp(x)

    def _consensus_throughput(self):
        if not self.commits['hashes']:
            return 0

        start, end = min(self.proposals['hashes'].values()), max(self.commits['hashes'].values())
        duration = end - start
        return duration

    def _consensus_latency(self):
        if not self.commits['hashes']:
            return 0

        latency = [c - self.proposals['hashes'][d] for d, c in self.commits['hashes'].items()]
        return mean(latency) if latency else 0

    def _peers_latency(self):
        out = []

        for pk, values in self.merged_peers_info.items():
            out.append(f'  Peer {pk} mean latency {round(mean(values) * 1_000):,} ms')

        return '\n'.join(out)

    def _mean_peers_consensus_latency(self):
        means = []

        for pk, values in self.merged_peers_info.items():
            means.append(round(mean(values) * 1_000))

        return mean(means) if len(means) > 0 else 0

    def result(self):
        consensus_latency = self._consensus_latency() * 1_000
        cons_duration = self._consensus_throughput()

        return (
            '\n'
            '-----------------------------------------\n'
            ' SUMMARY:\n'
            '-----------------------------------------\n'
            ' + CONFIG:\n'
            f' Committee: {self.committee_size} node(s)\n'
            f' Faults: {self.faults} node(s)\n'
            f' Execution time: {round(cons_duration):,} s\n'
            '\n'
            ' + RESULTS:\n'
            f' Consensus latency: {round(consensus_latency):,} ms\n'
            f' Process consensus: {len(self.commits["hashes"])}\n'
            f' Latency per peers:\n' + self._peers_latency() + '\n'
            f' Mean consensus latency for peers: {self._mean_peers_consensus_latency():,}ms\n'
            '-----------------------------------------\n'
        )

    def print(self, filename):
        assert isinstance(filename, str)
        with open(filename, 'a') as f:
            f.write(self.result())

    @classmethod
    def process(cls, directory, faults=0):
        assert isinstance(directory, str)

        peers = []
        for filename in sorted(glob(join(directory, 'peer-*.log'))):
            with open(filename, 'r') as f:
                peers += [f.read()]

        return cls(peers, faults=faults)
