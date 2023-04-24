import yaml
from json import dump, load
from collections import OrderedDict


class ConfigError(Exception):
    pass


class Key:
    def __init__(self, name, secret):
        self.name = name
        self.secret = secret

    @classmethod
    def from_file(cls, filename):
        assert isinstance(filename, str)
        with open(filename, 'r') as f:
            data = load(f)
        return cls(data['name'], data['secret'])


class Committee:
    """ The committee looks as follows:
        "authorities": {
            "name": {
                "number": x,
                "public_key": "UUID",
                "address": {
                    "host": x.x.x.x,
                    "port": x
                }
            },
            ...
        }
    """

    def __init__(self, secrets_with_addresses, base_port):
        assert isinstance(secrets_with_addresses, OrderedDict)
        assert all(isinstance(x, str) for x in secrets_with_addresses.keys())
        assert isinstance(base_port, int) and base_port > 1024

        number = 0
        self.json = {'authorities': OrderedDict()}
        for name, (address, secret) in secrets_with_addresses.items():
            self.json['authorities'][name] = {
                'number': number,
                'public_key': str(secret),
                'address': {
                    'host': address,
                    'port': base_port + number
                }
            }
            number += 1

    def primary_addresses(self, faults=0):
        ''' Returns an ordered list of primaries' addresses. '''
        assert faults < self.size()
        addresses = []
        good_nodes = self.size() - faults
        for authority in list(self.json['authorities'].values())[:good_nodes]:
            addresses += [authority['primary']['primary_to_primary']]
        return addresses

    def size(self):
        ''' Returns the number of authorities. '''
        return len(self.json['authorities'])

    def workers(self):
        ''' Returns the total number of workers (all authorities altogether). '''
        return sum(len(x['workers']) for x in self.json['authorities'].values())

    def print(self, filename):
        assert isinstance(filename, str)
        with open(filename, 'w') as f:
            dump(self.json, f, indent=4, sort_keys=True)

    @staticmethod
    def ip(address):
        assert isinstance(address, str)
        return address.split(':')[0]


class LocalCommittee(Committee):
    def __init__(self, keys, port):
        assert isinstance(keys, list)
        assert isinstance(port, int)
        secrets_with_addresses = OrderedDict((x.name, ('127.0.0.1', x.secret)) for x in keys)
        super().__init__(secrets_with_addresses, port)


class BenchParameters:
    def __init__(self, json):
        try:
            self.faults = int(json['faults'])

            nodes = json['nodes']
            nodes = nodes if isinstance(nodes, list) else [nodes]
            if not nodes or any(x <= 1 for x in nodes):
                raise ConfigError('Missing or invalid number of nodes')
            self.nodes = [int(x) for x in nodes]
            self.transactions_number = json['transactions_number']
            self.transactions_per_consensus = json['transactions_per_consensus']

            if 'collocate' in json:
                self.collocate = bool(json['collocate'])
            else:
                self.collocate = True
        except KeyError as e:
            raise ConfigError(f'Malformed bench parameters: missing key {e}')

        except ValueError:
            raise ConfigError('Invalid parameters type')

        if min(self.nodes) <= self.faults:
            raise ConfigError('There should be more nodes than faults')


class PlotParameters:
    def __init__(self, json):
        try:
            faults = json['faults']
            faults = faults if isinstance(faults, list) else [faults]
            self.faults = [int(x) for x in faults] if faults else [0]

            nodes = json['nodes']
            nodes = nodes if isinstance(nodes, list) else [nodes]
            if not nodes:
                raise ConfigError('Missing number of nodes')
            self.nodes = [int(x) for x in nodes]

            workers = json['workers']
            workers = workers if isinstance(workers, list) else [workers]
            if not workers:
                raise ConfigError('Missing number of workers')
            self.workers = [int(x) for x in workers]

            if 'collocate' in json:
                self.collocate = bool(json['collocate'])
            else:
                self.collocate = True

            self.tx_size = int(json['tx_size'])

            max_lat = json['max_latency']
            max_lat = max_lat if isinstance(max_lat, list) else [max_lat]
            if not max_lat:
                raise ConfigError('Missing max latency')
            self.max_latency = [int(x) for x in max_lat]

        except KeyError as e:
            raise ConfigError(f'Malformed bench parameters: missing key {e}')

        except ValueError:
            raise ConfigError('Invalid parameters type')

        if len(self.nodes) > 1 and len(self.workers) > 1:
            raise ConfigError(
                'Either the "nodes" or the "workers can be a list (not both)'
            )

    def scalability(self):
        return len(self.workers) > 1


class BenchmarkConfig:

    def __init__(self, committee: Committee, bench_parameters: BenchParameters, coordinator_pk):
        self.committee = committee
        self.bench_params = bench_parameters
        self.coordinator_pk = coordinator_pk

    def print(self, filename):
        assert isinstance(filename, str)
        with open(filename, 'w') as f:
            yaml.dump(self._as_yuml(), f)

    def _as_yuml(self):
        conf = {
            'consensusStartThreshold': self.bench_params.transactions_per_consensus,
            'numberOfTransactionToGenerate': self.bench_params.transactions_number,
            'coordinatorPublicKey': self.coordinator_pk,
        }

        peer_configs = []
        for peer in self.committee.json['authorities']:
            peer_conf = self.committee.json['authorities'][peer]

            p_conf = {
                'publicKey': peer_conf['public_key'],
                'number': peer_conf['number'],
                'address': {
                    'host': peer_conf['address']['host'],
                    'port': peer_conf['address']['port']
                }
            }

            peer_configs.append(p_conf)

        conf['peerConfigs'] = peer_configs

        return conf
