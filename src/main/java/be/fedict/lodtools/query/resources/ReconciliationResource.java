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
import be.fedict.lodtools.query.views.ServiceListView;

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
import java.util.Set;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

/**
 * Reconciliation
 * 
 * @author Bart Hanssens
 */
@Path("/_reconcile")
public class ReconciliationResource extends RdfResource {
	 private final JsonNodeFactory FAC = new JsonNodeFactory(false);
	 private final ObjectMapper MAPPER = new ObjectMapper();
	 
	/**
	 * Convert json input parameter into parameters for query
	 * 
	 * @param node
	 * @return 
	 */
	private MultivaluedMap<String,String> getParams(JsonNode node) {
		MultivaluedMap<String,String> params = new MultivaluedHashMap<>();
		
		JsonNode q = node.get("query");
		if (q == null) {
			throw new WebApplicationException("No query field present");
		}
		
		//JsonNode l = node.get("limit");
		params.add("query", q.textValue());
		//params.add("limit", l == null ? "10" : String.valueOf(l.asInt(3)));
		
		return params;
	}
	
	
	/**
	 * Convert RDF tuple result into JSON result object
	 * 
	 * @param table
	 * @return object
	 */
	private JsonNode getResult(List<BindingSet> table) {	
		ObjectNode res = FAC.objectNode();
		ArrayNode arr = FAC.arrayNode();
		res.set("result", arr);
		
		if (table == null || table.isEmpty()) {
			return res;
		}
		
		for(BindingSet row: table) {
			double score = Double.valueOf(getBindVal(row, "score"));
			boolean match = score > 0.9;
			
			ObjectNode obj = FAC.objectNode()
								.put("id", getBindVal(row, "id"))
								.put("score", score)
								.put("match", match);
			
			ArrayNode arrt = FAC.arrayNode().add(getBindVal(row, "type"));
			obj.set("type", arrt);
			arr.add(obj);
		}
		return res;
	}

	/**
	 * Show the list of available repositories
	 * 
	 * @return list of repositories
	 */
	@GET
	@Path("/")
	@Produces({MediaType.TEXT_HTML})
	public ServiceListView repoList() {
		return new ServiceListView(listRepositories());
	}
	
	@POST
	@Path("/{repo}")
	@ExceptionMetered
	@Produces({MediaType.APPLICATION_JSON})
	public JsonCallback queryJSONPost(@PathParam("repo") String repo, 
			@FormParam("queries") Optional<String> queries,
			@FormParam("callback") Optional<String> callback) {
		return queryJSON(repo, queries, callback);
	}

	/**
	 * Execute a reconciliation query
	 * 
	 * @param repo repository
	 * @param queries JSON query object
	 * @param callback optional callback parameter
	 * @return RDF model + JSON-LD frame
	 */
	@GET
	@Path("/{repo}")
	@ExceptionMetered
	@Produces({MediaType.APPLICATION_JSON})
	public JsonCallback queryJSON(@PathParam("repo") String repo, 
			@QueryParam("queries") Optional<String> queries,
			@QueryParam("callback") Optional<String> callback) {
		JsonNode root;

		try {
			if (!queries.isPresent()) {
				String str = getReader().read(repo, "reconcile.json");
				return new JsonCallback(MAPPER.readTree(str), callback.orElse(""));
			}
			root = MAPPER.readTree(queries.get());
		} catch (IOException ex) {
			throw new WebApplicationException(ex);
		}
		ObjectNode results = FAC.objectNode();

		Iterator<String> fields = root.fieldNames();
        while (fields.hasNext()) {
			String name = fields.next();
			JsonNode node = root.get(name);

			List<BindingSet> res = query(repo, "reconcile", getParams(node));
			if (res == null || res.isEmpty()) {
				res = query(repo, "reconcile_fuzzy", getParams(node));
			}
			JsonNode result = getResult(res);
			results.set(name, result);
		}
		return new JsonCallback(results, callback.orElse(""));
	}
	
	/**
	 * Get a preview
	 * 
	 * @param repo repository
	 * @param id ID to preview
	 * 
	 * @return HTML preview
	 */
	@GET
	@Path("/{repo}/preview")
	@ExceptionMetered
	@Produces({MediaType.TEXT_HTML})
	public PreviewView preview(@PathParam("repo") String repo, @QueryParam("id") String id) {
		MultivaluedMap<String,String> params = new MultivaluedHashMap<>();
		params.add("id", id);

		Model m = query(repo, "preview", params, false).getModel();
		Set<String> labels = Models.objectStrings(m);
		
		return new PreviewView(id, labels.toArray(new String[0]));
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
