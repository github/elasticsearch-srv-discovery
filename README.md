SRV Discovery Plugin for Elasticsearch
======================================

Use SRV records for Elasticsearch discovery, like the ones
[Consul][https://consul.io] provides.

## Configuration

```
discovery:
  type: srv
  srv:
    query: elasticsearch-transport.service.consul
```
