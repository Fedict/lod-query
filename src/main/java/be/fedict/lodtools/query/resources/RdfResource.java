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
import be.fedict.lodtools.query.helpers.QueryComment;
import be.fedict.lodtools.query.helpers.QueryReader;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import org.eclipse.rdf4j.model.IRI;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.ROV;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.impl.MapBindingSet;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sparql.query.QueryStringUtil;
import org.eclipse.rdf4j.repository.util.Repositories;


/**
 * Abstract resource querying the RDF triple store.
 * 
 * @author Bart.Hanssens
 */
public abstract class RdfResource {
	private final RepositoryManager mgr;
	private final QueryReader qr;

	private final ValueFactory fac = SimpleValueFactory.getInstance();

	
	protected QueryReader getReader() {
		return qr;
	}
	
	protected IRI asIRI(String str) {
		return fac.createIRI(str);
		
	}
	/**
	 * Get string as RDF literal
	 * 
	 * @param lit
	 * @return literal 
	 */
	protected Literal asLiteral(String lit) {
		return fac.createLiteral(lit);
	}
	
	/**
	 * Get the repository from name
	 * 
	 * @param repoName name of the repository
	 * @return initialized repository
	 * @throws WebApplicationException if repository cannot be opened
	 */
	protected Repository getRepository(String repoName) {
		Repository repo = mgr.getRepository(repoName);
		if (repo == null) {
			throw new WebApplicationException(
							MessageFormat.format("Repo {0} not found", repoName));
		}
		if (!repo.isInitialized()) {
			repo.initialize();
		}
		return repo;
	}
	
	/**
	 * Set namespaces for most commonly used vocabularies
	 * 
	 * @param m triples
	 * @return model with namespace prefixes
	 */
	protected Model setNamespaces(Model m) {
		if (m != null && !m.isEmpty()) {
			m.setNamespace(DCAT.PREFIX, DCAT.NAMESPACE);
			m.setNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
			m.setNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
			m.setNamespace(ORG.PREFIX, ORG.NAMESPACE);
			m.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
			m.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
			m.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
			m.setNamespace(ROV.PREFIX, ROV.NAMESPACE);
			m.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
			m.setNamespace(XMLSchema.PREFIX, XMLSchema.NAMESPACE);
		}
		return m;
	}
	
	/**
	 * Turn HTTP parameters into query bindings
	 * 
	 * @param params HTTP params
	 * @return query bindings 
	 */
	protected HashMap<String,Value> bind(MultivaluedMap<String,?> params) {
		HashMap<String,Value> bindings = new HashMap<>();
		
		if (params != null) {
			for(String key: params.keySet()) {
				Object p = params.getFirst(key);
				if (p != null) {
					if (p instanceof IRI) {
						bindings.put(key, (IRI) p);
					} else {
						bindings.put(key, asLiteral((String) p));
					}
				}
			}
		}
		return bindings;
	}

	/**
	 * Safely get a value from a bindingset
	 * 
	 * @param bindingSet
	 * @param name name of the binding
	 * @return value or null
	 */
	protected String getBindVal(BindingSet bindingSet, String name) {
		Binding b = bindingSet.getBinding(name);
		return (b != null ? b.getValue().stringValue() : null);
	}
	
	/**
	 * Prepare and run a SPARQL tuple query
	 * 
	 * @param repoName repository name
	 * @param qryName query name
	 * @param params parameters for binding (if any)
	 * @return results in bindingset list
	 */
	protected List<BindingSet> query(String repoName, String qryName, 
										MultivaluedMap<String,Object> params) {
		Repository repo = getRepository(repoName);
		
		String qry = qr.getQuery(repoName, qryName);
		
		// Replace params here for performance (mostly within glue BIND's) 
		MapBindingSet bs = new MapBindingSet();
		bind(params).forEach((k,v) -> bs.addBinding(k, v));
		qry = QueryStringUtil.getTupleQueryString(qry, bs);
System.err.println(qry);
		try {
			return Repositories.tupleQuery(repo, qry, r -> QueryResults.asList(r));
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException("Error executing query", e);
		}
	}

	/**
	 * Prepare and run a SPARQL graph query
	 * 
	 * @param repoName repository name
	 * @param qryName query string
	 * @param params parameters for bindings (if any)
	 * @param getFrame read JSON-LD frame or not
	 * @return results in triple model
	 */
	protected ModelFrame query(String repoName, String qryName, 
							MultivaluedMap<String,?> params, boolean getFrame) {
		Repository repo = getRepository(repoName);
		
		String f = (getFrame) ? qr.getFrame(repoName, qryName) : "";
		String qry = qr.getQuery(repoName, qryName);
		
		// Replace params here for performance (mostly within glue BIND's) 
		MapBindingSet bs = new MapBindingSet();
		bind(params).forEach((k,v) -> bs.addBinding(k, v));
		qry = QueryStringUtil.getGraphQueryString(qry, bs);
	
		try (RepositoryConnection conn = repo.getConnection()) {
			GraphQuery q = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);
			Model m = setNamespaces(QueryResults.asModel(q.evaluate()));
			
			return new ModelFrame(m, f);
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException("Error executing query", e);
		}
	}
	
	/**
	 * List all available repositories
	 * 
	 * @return array of repository names
	 */
	protected String[] listRepositories() {
		return qr.listRepositories();
	}
	
	/**
	 * List all available queries for a repository and their descriptions
	 * 
	 * @param repoName repository name
	 * @return map of queries and descriptions
	 */
	protected Map<String,QueryComment> listQueries(String repoName) {
		return qr.listQueries(repoName);
	}
	
	/**
	 * Constructor
	 * 
	 * @param mgr repository manager
	 * @param qr query  reader
	 */
	public RdfResource(RepositoryManager mgr, QueryReader qr) {
		this.mgr = mgr;
		this.qr = qr;
	}
}
