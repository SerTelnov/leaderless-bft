from os.path import join

from benchmark.utils import PathMaker


class CommandMaker:

    @staticmethod
    def cleanup():
        return (
            f'rm .*.json ; rm .*.yaml rm .*.jar mkdir -p {PathMaker.results_path()}'
        )

    @staticmethod
    def clean_logs():
        return f'rm -r {PathMaker.logs_path()} ; mkdir -p {PathMaker.logs_path()}'

    @staticmethod
    def compile():
        return './gradlew shadowJar'

    @staticmethod
    def generate_key(filename):
        assert isinstance(filename, str)
        return f'./node generate_keys --filename {filename}'

    @staticmethod
    def run_peer(app_node, parameters):
        return (f'java -DappName={app_node} -Dlog4j.configurationFile=log4j.xml '
                f'-jar ConsensusApp.jar ' + parameters)

    @staticmethod
    def run_coordinator(parameters, a_lambda):
        l_param = f'-l {a_lambda} ' if a_lambda is not None else ''
        return (f'java -DappName=coordinator -Dlog4j.configurationFile=log4j.xml '
                f'-jar ConsensusApp.jar -coordinator ' + l_param + parameters)

    @staticmethod
    def kill():
        return 'tmux kill-server'

    @staticmethod
    def alias_binaries(origin):
        assert isinstance(origin, str)
        app_name = 'ConsensusApp.jar'
        app = join(origin, app_name)
        return f'rm {app_name} ; ln -s {app}'
