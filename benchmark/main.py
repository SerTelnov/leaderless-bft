from benchmark.local import LocalBench
from benchmark.utils import Print, BenchError


def local(debug=True):
    """ Run benchmarks on localhost """
    bench_params = {
        'faults': 0,
        'nodes': 6,
        'transactions_number': 1_000,
        'transactions_per_consensus': 15,
        'duration': 20
    }

    try:
        ret = LocalBench(bench_params).run(debug)
        print(ret.result())
    except BenchError as e:
        Print.error(e)


if __name__ == '__main__':
    local()
