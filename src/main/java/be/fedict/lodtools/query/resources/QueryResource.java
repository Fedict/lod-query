/*
 * Copyright (c) 2017, Bart Hanssens <bart.hanssens@fedict.be>
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

import be.fedict.lodtools.query.helpers.ModelFrame;
import be.fedict.lodtools.query.helpers.QueryReader;
import be.fedict.lodtools.query.helpers.RDFMediaType;
import be.fedict.lodtools.query.views.QueryListView;
import be.fedict.lodtools.query.views.RepositoryListView;

import com.codahale.metrics.annotation.ExceptionMetered;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

/**
 * Query
 * 
 * @author Bart.Hanssens
 */
@Path("/_query")
public class QueryResource extends RdfResource {
	
	/**
	 * Show the list of available repositories
	 * 
	 * @return list of repositories
	 */
	@GET
	@Path("/")
	@Produces({MediaType.TEXT_HTML})
	public RepositoryListView repoList() {
		return new RepositoryListView("_query", listRepositories());
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
	 * Execute a query
	 * 
	 * @param repo repository name
	 * @param qry query name
	 * @param info HTTP context
	 * @return RDF model + JSON-LD frame
	 */
	@GET
	@Path("/{repo}/{query}")
	@ExceptionMetered
	@Produces({RDFMediaType.JSONLD + "; qs=1"})
	public ModelFrame queryJSON(@PathParam("repo") String repo, 
			@PathParam("query") String qry, @Context UriInfo info) {		
		return query(repo, qry + ".qr", info.getQueryParameters(), true);	
	}
	
	/**
	 * Execute a query
	 * 
	 * @param repo repository name
	 * @param qry query name
	 * @param info HTTP context
	 * @return RDF model
	 */
	@GET
	@Path("/{repo}/{query}")
	@ExceptionMetered
	@Produces({RDFMediaType.TTL + "; qs=0.75", RDFMediaType.NTRIPLES + "; qs=0.75"})
	public Model queryRDF(@PathParam("repo") String repo,
			@PathParam("query") String qry, @Context UriInfo info) {		
		return query(repo, qry + ".qr", info.getQueryParameters(), false).getModel();	
	}
	
	/**
	 * Constructor
	 * 
	 * @param mgr RDF repository manager
	 * @param qr query and frame reader
	 */
	public QueryResource(RepositoryManager mgr, QueryReader qr) {
		super(mgr, qr);
	}
}
