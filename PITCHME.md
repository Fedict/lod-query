# ID.belgium query tool

> Executes predefined SPARQL queries

---

## Why this tool

- Abstract/hide SPARQL API

--

## Features

- Different formats via `HTTP Accept:` header
  - RDF N-Triples, Turtle, JSON-LD
- Optional transformation via [JSON-LD Framing](https://json-ld.org/spec/latest/json-ld-framing/)

--

## How it works

- One directory with queries per repository
- HTTP GET executes SPARQL with parameters
- Optional JSON-LD frame file

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