# ID.belgium query tool

> Executes predefined SPARQL queries

---

## Why this tool

- Abstract/hide SPARQL API
- Re-users don't have to know SPARQL
- Limit queries to "safe" queries

---

## Features

- Different formats via `HTTP Accept:` header
  - RDF N-Triples, Turtle, JSON-LD
- Optional transformation via [JSON-LD Framing](https://json-ld.org/spec/latest/json-ld-framing/)

---

## How it works

- One directory with queries per repository
- HTTP GET executes SPARQL with parameters
- Optional a JSON-LD Frame file is applied
  - Result is still valid JSON-LD

---

## Example

```
GET https://id.belgium.be/_query/fsb-services/families
```

+++?code=prod/query/fsb-services/families.qr

SPARQL query

+++?code=prod/query/fsb-services/families.frame

JSON-LD Frame

---

## Limitations

- JSON-LD transformations are somewhat limited

---

## Technology

- [DropWizard](http://www.dropwizard.io) REST server
- Java [WatchService](https://docs.oracle.com/javase/tutorial/essential/io/notification.html) API
- [RDFJ4](http://rdf4j.org/) Java API

---

## Thank you

Questions ? 

@fa[twitter] @BartHanssens
