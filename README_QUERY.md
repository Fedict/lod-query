# Query API

## List of available repositories and queries

A list of available repositories and their queries can be found on
```
/_query
```

## Where to store the query files

Queries must be stored within the `queryRoot` directory, using one subdirectory 
per repository (or "namespace" in Ontotext GraphDB speak)

```
/repository-name/query-name.qr
```

Currently the content of a query file is re-read upon every HTTP GET request.

### SPARQL query

CONSTRUCT queries are stored in a file with prefix `.qr`, e.g. `families.qr`

The (optional) HTTP GET request parameters are passed to the variables in the
query with matching names, e.g. the value of `name` is passed to `?name`.
 
SELECT queries are currently not supported.

### Comments

If the start of the sparql contains text (lines starting with a `#`), 
then this text will be shown on the HTML overview.

Optionally this documentation may include examples (`# @example ...`)
and documentation on parameters (`# @param ...`).

## (Optional) JSON-LD Frames

If the directory containing the query file also contains a matching file with 
prefix `.frame`, then this file will be used as
[JSON-LD Frame](https://json-ld.org/spec/latest/json-ld-framing/).

It is not used when serializing the results to Turtle or N-Triples.