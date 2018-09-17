# Reconciliation Service API

Basic API for retrieving IDs from 'name registries'

## List of available repositories and queries

A list of available repositories and their queries can be found on

```
/_reconcile
```

Queries are performed via HTTP GET or POST requests, results are in JSON format.

Only multi-query is implemented (single query is deprecated anyway).
For example, to get a list of IDs matching the label "BOSA" or "DAV"

```
/_reconcile/organizations/0?queries="{"q0":{"query":"BOSA"},"q1":{"query":"DAV"}}
```

Where `q0` and `q1` can be chosen freely.

See the [Reconciliation Service API] (https://github.com/OpenRefine/OpenRefine/wiki/Reconciliation-Service-API)
for more information.


### Files

Queries must be stored within the `reconcileRoot` directory, using one subdirectory 
per repository (or "namespace" in Ontotext GraphDB speak)

```
/repository-name/name.json
/repository-name/name.qr
/repository-name/_name_fuzzy.qr
/repository-name/_name_preview.qr
```

The `name.json` contains server metadata for registering the server with 
reconciliation clients.

The `name.qr` contains the query to perform by the server for exact match (tried first),
and `_name_fuzzy.qr` contains the SPARQL full text query for fuzzy search.

The `_name_preview.qr` contains the SPARQL query used to get the labels for rendering
an HTML preview (which can be used by Reconciliation clients to show end-users more information).

Currently the content of a query files are re-read upon every HTTP GET request.

### Comments

If the start of the sparql contains text (lines starting with a `#`), 
then this text will be shown on the HTML overview.

Optionally this documentation may include examples (`# @example ...`)
and documentation on parameters (`# @param ...`).