SRV Discovery Plugin for Elasticsearch
======================================

Use SRV records for Elasticsearch discovery, like the ones
[Consul](https://consul.io) provides.

Looking for a plugin that uses the Consul API directly? Check out
[lithiumtech/elasticsearch-consul-discovery](https://github.com/lithiumtech/elasticsearch-consul-discovery).

## Installation

Based on the version of Elasticsearch that you're running, pick the compatible plugin version (e.g. `1.5.0`), then run this command:

```bash
bin/plugin install srv-discovery --url https://github.com/github/elasticsearch-srv-discovery/releases/download/1.5.0/elasticsearch-srv-discovery-1.5.0.zip
```

Verify that the plugin was installed:

```
$ bin/plugin -l
Installed plugins:
    - srv-discovery

$ curl localhost:9200/_cat/plugins?v
name  component             version type url
Pluto srv-discovery         ...     j
```

## Compatibility

The SRV Discovery plugin is known to be compatible with these versions of Elasticsearch:

Elasticsearch|SRV Discovery plugin
---|---
1.7.3|1.5.0
1.7.2|1.5.0
1.7.1|1.5.0
1.7.0|1.5.0
1.6.2|1.5.0
1.6.1|1.5.0
1.6.0|1.5.0
1.5.2|1.5.0
1.5.1|1.5.0
1.5.0|1.5.0

## Configuration

Key|Example|Description
---|---|---
`discovery.srv.query`|`elasticsearch-9300.service.consul`|The query string to use when querying for SRV records.
`discovery.srv.servers`|`127.0.0.1:8600`|DNS Servers to contact. Can be an array or a comma-delimited string. Port numbers are optional.
`discovery.srv.protocol`|`tcp`|Which protocol to use. Options are `tcp` and `udp`. Default is `tcp`.

Note: Consul will return maximum 3 records when using UDP queries. All records are returned when using TCP.

### Simple Example
```yaml
discovery:
  type: srv
  srv:
    query: elasticsearch-9300.service.consul
```

### Complex Example
```yaml
discovery:
  type: srv
  srv:
    query: elasticsearch-9300.service.consul
    protocol: tcp
    servers:
      - 127.0.0.1:8600
      - 192.168.1.1
```

## Development

To see the effects of a change on a real Elasticsearch instance, build the package and install it like so:

```
plugin="/Users/you/elasticsearch-<version>/bin/plugin"
name="srv-discovery"
zip="file:///Users/you/elasticsearch-srv-discovery/target/releases/elasticsearch-srv-discovery-<version>.zip"

mvn package -Dmaven.test.skip=true || exit 1
$plugin remove $name
$plugin install $name --url $zip
```
