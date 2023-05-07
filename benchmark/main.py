from benchmark.local import LocalBench
from benchmark.utils import Print, BenchError


def local(debug=True):
    """ Run benchmarks on localhost """
    bench_params = {
        'faults': 3,
        'nodes': 10,
        'transactions_number': 20_000,
        'transactions_per_consensus': 10,
        'duration': 10
    }

    try:
        ret = LocalBench(bench_params).run(debug)
        print(ret.result())
    except BenchError as e:
        Print.error(e)


if __name__ == '__main__':
    local()
