from benchmark.local import LocalBench
from benchmark.utils import Print, BenchError


def local(debug=True):
    """ Run benchmarks on localhost """
    bench_params = {
        'faults': 1,
        'nodes': 5,
        'transactions_number': 200_000,
        'transactions_per_consensus': 10,
        'duration': 15
    }

    nodes = bench_params['nodes']
    faults = bench_params['faults']
    duration = bench_params['duration']

    for i in range(120):
        print(f'Run call number {i + 1}')
        try:
            ret = LocalBench(bench_params).run(debug)
            ret.print(f'results/.results-n-{nodes}-f-{faults}-duration-{duration}.txt')
        except Exception as e:
            Print.error(e)


if __name__ == '__main__':
    local()
