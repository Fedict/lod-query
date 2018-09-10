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

package be.fedict.lodtools.query.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.rdf4j.model.Model;

/**
 * Helper object for passing RDF model + JSON-LD Frame
 * 
 * @author Bart.Hanssens
 */
public class JsonCallback {
	private final String callback;
	private final JsonNode node;
	
	/**
	 * Get triples in RDF model
	 * 
	 * @return model
	 */
	public JsonNode getNode() {
		return node;
	}
	
	/**
	 * Get JSON-LD Frame
	 * 
	 * @return 
	 */
	public String getCallback() {
		return callback;
	}
	
	/**
	 * Constructor
	 * 
	 * @param model RDF model
	 * @param frame JSON-LD frame
	 */
	public JsonCallback(JsonNode node, String callback) {
		this.node = node;
		this.callback = callback;
	}
}
