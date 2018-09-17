# lod-query
Basic front-end for querying a triple store.

Two APIs are available

- [custom api](README_QUERY.md)
- (OntoRefine) [Reconciliation Service API](README_RECONCILE.md)


## HTTP Content Negotiation

Serializations to JSON-LD (default), N-Triples and Turtle are supported
and can be selected by setting the correct HTTP `Accept:` header.

## Configuration file

Example config file

```
sparqlPoint: "http://localhost:7200/"
username: readonly
password: readonly

queryRoot: c:/data/query
reconcileRoot: c:/data/reconcile

server:
  requestLog:
    appenders:
      - type: file
        archive: false
        currentLogFilename: ./query.log

logging:
  level: INFO
  appenders:
    - type: file
      archive: false
      currentLogFilename: ./application.log
```

`queryRoot` is the root (top-level) directory where the query files for the custom API are stored
`reconcileRoot` is the root (top-level) directory where the query files for the Reconciliation API are stored

