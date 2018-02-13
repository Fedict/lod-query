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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import io.dropwizard.Configuration;
import io.dropwizard.bundles.assets.AssetsBundleConfiguration;
import io.dropwizard.bundles.assets.AssetsConfiguration;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.URL;

/**
 *
 * @author Bart.Hanssens
 */
public class AppConfig extends Configuration implements AssetsBundleConfiguration {
	@URL
	@NotNull
	private String sparqlPoint;

	private String username;
	private String password;
	
	@NotNull
	private String queryRoot;
	
	@NotNull
	private ImmutableMap<String, Map<String, String>> views;
	
	@Valid
	@NotNull
	@JsonProperty
	private final AssetsConfiguration assets = AssetsConfiguration.builder().build();

	@JsonProperty
	public String getSparqlPoint() {
		return sparqlPoint;
	}
	
	@JsonProperty
	public void setSparqlPoint(String sparqlPoint) {
		this.sparqlPoint = sparqlPoint;
	}	

	@JsonProperty
	public String getUsername() {
		return username;
	}
	
	@JsonProperty
	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}
	
	@JsonProperty
	public String getQueryRoot() {
		return queryRoot;
	}

	@JsonProperty
	public void setQueryRoot(String queryRoot) {
		this.queryRoot = queryRoot;
	}
	
	@JsonProperty
	public Map<String, Map<String, String>> getViews() {
		return this.views;
	}
	
	@JsonProperty
	public void setViews(Map<String, Map<String, String>> views) {
		final ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();
		for (Map.Entry<String, Map<String, String>> entry : views.entrySet()) {
			builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
		}
		this.views = builder.build();
	}

	@Override
	public AssetsConfiguration getAssetsConfiguration() {
		return assets;
	}
}
