from datetime import datetime
from glob import glob
from multiprocessing import Pool
from os.path import join
from re import findall, search
from statistics import mean
from collections.abc import Iterable


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
        self.proposals = self._merge_results([x.items() for x in proposals])
        self.commits = self._merge_results([x.items() for x in commits])

    @staticmethod
    def _merge_results(input):
        # Keep the earliest timestamp.
        merged = {}
        for x in input:
            for y in x:
                if len(y) == 0:
                    continue

                h, date = y
                if h not in merged or merged[h] > date:
                    merged[h] = date

        return merged

    def _parse_peers(self, log):
        if search(r'(?:panicked|Error|Exception)', log) is not None:
            raise ParseError('Peer(s) panicked')

        tmp = findall(r'(.*Z) .* PROPOSE_BLOCK \[pk=(.*),n=\d+\] hash=(-?\d+)', log)
        tmp = [(h, self._to_posix(t)) for t, pk, h in tmp]
        proposals = self._merge_results([tmp])

        tmp = findall(r'(.*Z) .* COMMIT \[pk=(.*),n=\d+\] hash=(-?\d+)', log)
        tmp = [(h, self._to_posix(t)) for t, pk, h in tmp]
        commits = self._merge_results([tmp])

        return proposals, commits

    def _to_posix(self, string):
        x = datetime.fromisoformat(string.replace('Z', '+00:00'))
        return datetime.timestamp(x)

    def _consensus_throughput(self):
        if not self.commits:
            return 0
        start, end = min(self.proposals.values()), max(self.commits.values())
        duration = end - start
        return duration

    def _consensus_latency(self):
        latency = [c - self.proposals[d] for d, c in self.commits.items()]
        return mean(latency) if latency else 0

    def _end_to_end_throughput(self):
        if not self.commits:
            return 0, 0, 0
        start, end = min(self.start), max(self.commits.values())
        duration = end - start
        bytes = sum(self.sizes.values())
        bps = bytes / duration
        tps = bps / self.size[0]
        return tps, bps, duration

    def _end_to_end_latency(self):
        latency = []
        for sent, received in zip(self.sent_samples, self.received_samples):
            for tx_id, batch_id in received.items():
                if batch_id in self.commits:
                    assert tx_id in sent  # We receive txs that we sent.
                    start = sent[tx_id]
                    end = self.commits[batch_id]
                    latency += [end - start]
        return mean(latency) if latency else 0

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
            f' Process consensus: {len(self.commits)}\n'
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
