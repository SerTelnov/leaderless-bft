from benchmark.local import LocalBench
from benchmark.utils import Print


def local(debug=True):
    """ Run benchmarks on localhost """
    bench_params = {
        'faults': 1,
        'nodes': 10,
        'transactions_number': 200_000,
        'transactions_per_consensus': 10,
        'duration': 20,
        'lambda': 1
    }

    _run(bench_params, 20, debug)


def _run(bench_params, times, debug):
    nodes = bench_params['nodes']
    faults = bench_params['faults']
    duration = bench_params['duration']
    a_lambda = bench_params['lambda']

    for i in range(times):
        print(f'Run call number {i + 1}')
        try:
            ret = LocalBench(bench_params).run(debug)
            ret.print(f'results/.results-{a_lambda}-n-{nodes}-f-{faults}-duration-{duration}.txt')
        except Exception as e:
            Print.error(e)


if __name__ == '__main__':
    local()
