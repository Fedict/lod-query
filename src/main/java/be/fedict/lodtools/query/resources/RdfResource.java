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
import java.io.IOException;

import java.text.MessageFormat;
import java.util.HashMap;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
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

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;


/**
 * Abstract resource querying the RDF triple store.
 * 
 * @author Bart.Hanssens
 */

@Produces({RDFMediaType.JSONLD})
public abstract class RdfResource {
	private final RepositoryManager mgr;
	private final QueryReader qr;

	private final ValueFactory fac = SimpleValueFactory.getInstance();

	
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
			throw new WebApplicationException(MessageFormat.format("Repo {0} not found", repo));
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
	 * Return queryresult
	 * 
	 * @param q query to evaluate
	 * @return queryresult
	 */
	protected QueryResult evaluate(Query q) {
		if (q instanceof TupleQuery) {
			return ((TupleQuery) q).evaluate();
		}
		if (q instanceof GraphQuery) {
			return ((GraphQuery) q).evaluate();
		}
		return null;
	}
	
	/**
	 * Turn HTTP parameters into query bindings
	 * 
	 * @param params HTTP params
	 * @return query bindings 
	 */
	protected HashMap<String,Literal> bind(MultivaluedMap<String,String> params) {
		HashMap<String,Literal> bindings = new HashMap<>();
		
		if (params != null) {
			for(String key: params.keySet()) {
				String p = params.getFirst(key);
				if (p != null) {
					bindings.put(key, asLiteral(p));
				}
			}
		}
		return bindings;
	}
	
	/**
	 * Prepare and run a SPARQL query
	 * 
	 * @param repoName repository name
	 * @param qryName query string
	 * @param params parameters for bindings (if any)
	 * @return results in triple model
	 */
	protected ModelFrame query(String repoName, String qryName, MultivaluedMap<String,String> params) {
		Repository repo = getRepository(repoName);
		
		String f = qr.getFrame(repoName, qryName);
		String qry = qr.getQuery(repoName, qryName);
			
		try (RepositoryConnection conn = repo.getConnection()) {
			GraphQuery q = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);

			bind(params).forEach((k,v) -> q.setBinding(k, v));
			
			Model m = setNamespaces(QueryResults.asModel((GraphQueryResult) evaluate(q)));
			
			return new ModelFrame(m, f);
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException(e);
		}
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
