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
package be.fedict.lodtools.query;

import be.fedict.lodtools.query.health.RdfStoreHealthCheck;
import be.fedict.lodtools.query.helpers.ManagedRepositoryManager;
import be.fedict.lodtools.query.helpers.QueryReader;
import be.fedict.lodtools.query.helpers.JSONLDMessageBodyWriter;
import be.fedict.lodtools.query.helpers.RDFMessageBodyWriter;
import be.fedict.lodtools.query.resources.QueryResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;

/**
 * Main Dropwizard web application
 * 
 * @author Bart.Hanssens
 */
public class App extends Application<AppConfig> {
	@Override
	public String getName() {
		return "lod-query";
	}
	
	@Override
    public void run(AppConfig config, Environment env) {
		
		// Query and JSONLD frame readers
		QueryReader qr = new QueryReader(config.getQueryRoot());
		
		// repository
		String endpoint = config.getSparqlPoint();
		RemoteRepositoryManager mgr = 
				(RemoteRepositoryManager) RepositoryProvider.getRepositoryManager(endpoint);
		if (config.getUsername() != null) {
			mgr.setUsernameAndPassword(config.getUsername(), config.getPassword());
		}
		mgr.initialize();
		
		// Managed resource
		env.lifecycle().manage(new ManagedRepositoryManager(mgr));	
	
		// RDF Serialization format
		env.jersey().register(new JSONLDMessageBodyWriter());
		env.jersey().register(new RDFMessageBodyWriter());			
		// Page regource
		env.jersey().register(new QueryResource(mgr, qr));
		
		// Monitoring
		RdfStoreHealthCheck check = new RdfStoreHealthCheck(mgr.getSystemRepository());
		env.healthChecks().register("triplestore", check);
	}
	
	/**
	 * Main 
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new App().run(args);
	}
}
