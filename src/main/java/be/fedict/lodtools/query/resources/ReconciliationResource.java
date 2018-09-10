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
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
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
		
		int limit = 3;
		JsonNode l = node.get("limit");
		if (l != null) {
			try {
				limit = Integer.getInteger(l.textValue());
			} catch (NumberFormatException nfe) {
				throw new WebApplicationException("Invalid limit number");
			}
		}
		params.add("query", q.textValue());
		params.add("limit", String.valueOf(limit));
		
		return params;
	}
	 
	/**
	 * Convert RDF result into JSON result object
	 * 
	 * @param m RDF result
	 * @return object or null
	 */
	private JsonNode getResult(Model m) {
		if (m == null || m.isEmpty()) {
			return null;
		}
		ObjectNode res = FAC.objectNode();
		ArrayNode arr = FAC.arrayNode();
		
		Resource subject = Models.subject(m).get();
		for (String label: Models.objectStrings(m)) {
			arr.add(FAC.objectNode().put("id", subject.toString())
									.put("name", label));
		}
		return res.set("result", arr);
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
	public JsonNode queryJSON(@PathParam("repo") String repo, 
			@QueryParam("queries") String queries,
			@QueryParam("callback") Optional<String> callback) {
		JsonNode root;

		try {
			root = MAPPER.readTree(queries);
		} catch (IOException ex) {
			throw new WebApplicationException(ex);
		}
		ObjectNode results = FAC.objectNode();

		Iterator<String> fields = root.fieldNames();
        while (fields.hasNext()) {
			String name = fields.next();
			JsonNode node = root.get(name);
			Model m = query(repo, "reconcile", getParams(node), false).getModel();
			
			JsonNode result = getResult(m);
			results.set(name, result);
		}
		return results;
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
