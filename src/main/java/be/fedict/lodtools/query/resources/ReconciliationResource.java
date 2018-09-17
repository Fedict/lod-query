/*
 * Copyright (c) 2018, Bart Hanssens <bart.hanssens@bosa.fgov.be>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.fedict.lodtools.query.resources;

import be.fedict.lodtools.query.helpers.JsonCallback;
import be.fedict.lodtools.query.helpers.ReconcileReader;
import be.fedict.lodtools.query.views.PreviewView;
import be.fedict.lodtools.query.views.QueryListView;
import be.fedict.lodtools.query.views.RepositoryListView;

import com.codahale.metrics.annotation.ExceptionMetered;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

/**
 * Reconciliation Service page
 * Access point for (OntoRefine) Reconciliation Service API
 * 
 * @author Bart Hanssens
 */
@Path("/_reconcile")
public class ReconciliationResource extends RdfResource {
	private final String ID = "id";
	private final String ID_NS = "identifierSpace";
	private final String QUERY = "query";
	private final String SCORE  = "score";
	private final String TYPE = "type";
	private final String TYPE_NS = "schemaSpace";
	
	private final JsonNodeFactory FAC = new JsonNodeFactory(false);
	private final ObjectMapper MAPPER = new ObjectMapper();
	 
	/**
	 * Convert json input parameter from GET/POST into parameters for query engine
	 * 
	 * @param node JSON node
	 * @return K/V map
	 */
	private MultivaluedMap<String,Value> getParams(JsonNode node) {
		MultivaluedMap<String,Value> params = new MultivaluedHashMap<>();
		
		JsonNode q = node.get(QUERY);
		if (q == null) {
			throw new WebApplicationException("No query field present");
		}
		params.add(QUERY, asLiteral(q.asText()));
		
		JsonNode t = node.get(TYPE);
		if (t != null) {
			params.add(TYPE, asLiteral(t.asText()));
		}
		
		//params.add("limit", l == null ? "10" : String.valueOf(l.asInt(3)));
		
		return params;
	}
	
	/**
	 * Convert RDF tuple result into JSON result object
	 * 
	 * @param table result set
	 * @param idNs ID namespaces
	 * @param typeNs type namespaces
	 * @return object
	 */
	private JsonNode getResult(List<BindingSet> table, String idNs, String typeNs) {	
		ObjectNode res = FAC.objectNode();
		ArrayNode arr = FAC.arrayNode();
		res.set("result", arr);
		
		if (table == null || table.isEmpty()) {
			return res;
		}
		
		for(BindingSet row: table) {
			double score = Double.valueOf(getBindVal(row, SCORE));
			boolean match = score > 10;
			
			ObjectNode obj = FAC.objectNode()
								.put(ID, getBindVal(row, ID).replaceFirst(idNs, ""))
								.put("name", getBindVal(row, "name"))
								.put(SCORE, score)
								.put("match", match);

			ArrayNode arrt = FAC.arrayNode()
								.add(getBindVal(row, TYPE).replaceFirst(typeNs, ""));
			obj.set(TYPE, arrt);
			arr.add(obj);
		}
		return res;
	}

	/**
	 * Execute one reconciliation query
	 * 
	 * @param repo repository name
	 * @param cl class
	 * @param node
	 * @param idNs ID namespace
	 * @param typeNs type namespace
	 * @return JSON node
	 */
	private JsonNode queryJson(String repo, String cl, JsonNode node, String idNs, String typeNs) {
		// try exact match first
		MultivaluedMap<String,Value> params = getParams(node);

		// rewrite String (partial) identifier into full URI
		List<Value> values = params.get(TYPE);
		if (values != null) {
			List<Value> iris = values.stream()
									.map(t -> asIRI(typeNs, t.stringValue()))
									.collect(Collectors.toList());
			params.put(TYPE, iris);
		}

		List<BindingSet> res = query(repo, cl + ".qr", params);
		if (res == null || res.isEmpty()) {
			// set Lucene fuzzyness parameter
			String fuzzy = params.getFirst(QUERY).toString().concat("~0.8");
			params.addFirst(QUERY, asLiteral(fuzzy));
			res = query(repo, "_" + cl + "_fuzzy.qr", params);
		}

		return getResult(res, idNs, typeNs);
	}
	
	/**
	 * Show the list of available repositories
	 * 
	 * @return list of repositories
	 */
	@GET
	@Path("/")
	@Produces({MediaType.TEXT_HTML})
	public RepositoryListView repoList() {
		return new RepositoryListView("_reconcile", listRepositories());
	}

	/**
	 * Show the list of available queries
	 * 
	 * @param repo repository name
	 * @return list of queries
	 */
	@GET
	@Path("/{repo}")
	@Produces({MediaType.TEXT_HTML})
	public QueryListView queryList(@PathParam("repo") String repo) {
		return new QueryListView(repo, listQueries(repo));
	}

	/**
	 * Execute one or more reconciliation queries
	 * 
	 * @param repo repository
	 * @param cl class
	 * @param queries JSON object containing one or more objects
	 * @param callback optional callback parameter
	 * @return JSON result wrapper
	 */
	@POST
	@Path("/{repo}/{class}")
	@ExceptionMetered
	@Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
	public JsonCallback queriesJsonPost(
			@PathParam("repo") String repo, 
			@PathParam("class") String cl, 
			@FormParam("queries") Optional<String> queries,
			@FormParam("callback") Optional<String> callback) {
		return queriesJsonGet(repo, cl, queries, callback);
	}
	
	/**
	 * Execute one or more reconciliation queries
	 * 
	 * @param repo repository
	 * @param cl class
	 * @param queries JSON object containing one or more objects
	 * @param callback optional callback parameter
	 * @return JSON result wrapper
	 */
	@GET
	@Path("/{repo}/{class}")
	@ExceptionMetered
	@Produces({MediaType.APPLICATION_JSON})
	public JsonCallback queriesJsonGet(
			@PathParam("repo") String repo, 
			@PathParam("class") String cl, 	
			@QueryParam("queries") Optional<String> queries,
			@QueryParam("callback") Optional<String> callback) {
		String idNs;
		String typeNs;
		
		JsonNode root;

		// Use service metadata files a config, for namespaces
		// Not very efficient, but faster than doing STRREPLACE etc in SPARQL
		try {
			String str = getReader().read(repo, cl + ".json");
			JsonNode config = MAPPER.readTree(str);
			if (!queries.isPresent()) {
				return new JsonCallback(config, callback.orElse(""));
			}
			// "namespaces" for identifiers
			idNs = config.get(ID_NS).textValue();
			typeNs = config.get(TYPE_NS).textValue();
		} catch (IOException ex) {
			throw new WebApplicationException("Could not read/parse config", ex);
		}
		
		try {
			root = MAPPER.readTree(queries.get());
		} catch (IOException ex) {
			throw new WebApplicationException("Could not read/parse query file", ex);
		}
		ObjectNode results = FAC.objectNode();

		// Multiple Query Mode
		Iterator<String> fields = root.fieldNames();
        while (fields.hasNext()) {
			String name = fields.next();
			JsonNode result = queryJson(repo, cl, root.get(name), idNs, typeNs);
			results.set(name, result);
		}
		return new JsonCallback(results, callback.orElse(""));
	}
	
	/**
	 * Get a preview
	 * 
	 * @param repo repository
	 * @param cl
	 * @param id ID to preview
	 * @return HTML preview
	 */
	@GET
	@Path("/{repo}/_{class}_preview")
	@ExceptionMetered
	@Produces({MediaType.TEXT_HTML})
	public PreviewView preview(@PathParam("repo") String repo, 
					@PathParam("class") String cl, @QueryParam("id") String id) {
		String idNs = "";
		// Use service metadata files a config, for namespaces
		// Not very efficient, but faster than doing STRREPLACE etc in SPARQL
		try {
			String str = getReader().read(repo, cl + ".json");
			JsonNode config = MAPPER.readTree(str);
	
			// "namespaces" for identifiers
			idNs = config.get(ID_NS).textValue();
		} catch (IOException ex) {
			throw new WebApplicationException("Could not read/parse config", ex);
		}
		
		MultivaluedMap<String,Value> params = new MultivaluedHashMap<>();
		params.add("id", asIRI(idNs, id));

		List<BindingSet> bs = query(repo, "_" + cl + "_preview.qr", params);
		String[] labels = bs.stream()
							.filter(b -> b.hasBinding("label"))
							.map(b -> b.getBinding("label").getValue().stringValue())
							.toArray(String[]::new);

		return new PreviewView(id, labels);
	}
	
	/**
	 * Constructor
	 * 
	 * @param mgr RDF repository manager
	 * @param rr query reader
	 */
	public ReconciliationResource(RepositoryManager mgr, ReconcileReader rr) {
		super(mgr, rr);
	}
}
