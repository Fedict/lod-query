# lod-query
Basic front-end for querying a triple store.

Queries are performed via HTTP GET requests:
```
/repository-name/query-name?param1=value1
```

## HTTP Content Negotiation

Serializations to JSON-LD (default), N-Triples and Turtle are supported
and can be selected by setting the correct HTTP `Accept:` header.

## Files

Queries must be stored within the `queryRoot` directory, using one subdirectory 
per repository (or "namespace" in Ontotext GraphDB speak)

```
/repository-name/query-name.rq
```

Currently the content of a query file is re-read upon every HTTP GET request.

### SPARQL query

CONSTRUCT queries are stored in a file with prefix `.rq`, e.g. `families.rq`

The (optional) HTTP GET request parameters are passed to the variables in the
query with matching names, e.g. the value of `name` is passed to `?name`.
 
SELECT queries are currently not supported.

### (Optional) JSON-LD Frames

If the directory containing the query file also contains a matching file with 
prefix `.frame`, then this file will be used as JSON-LD Frame.

It is not used when serializing the results to Turtle or N-Triples.


## Configuration file

Example config file

```
sparqlPoint: "http://localhost:7200/"
username: readonly
password: readonly

queryRoot: c:/data/query

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

`queryRoot`is the root (top-level) directory where the query files are stored

