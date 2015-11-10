SRV Discovery Plugin for Elasticsearch
======================================

Use SRV records for Elasticsearch discovery, like the ones
[Consul](https://consul.io) provides.

Looking for a plugin that uses the Consul API directly? Check out
[lithiumtech/elasticsearch-consul-discovery](https://github.com/lithiumtech/elasticsearch-consul-discovery).

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
