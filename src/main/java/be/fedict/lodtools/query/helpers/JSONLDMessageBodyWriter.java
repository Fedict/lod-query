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
package be.fedict.lodtools.query.helpers;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RDF Writer for exporting RDF to JSON-LD files using JSON-LD Framing.
 * 
 * @author Bart.Hanssens
 */
@Provider
@Produces(RDFMediaType.JSONLD)
public class JSONLDMessageBodyWriter implements MessageBodyWriter<ModelFrame> {
	private final static Logger LOG = LoggerFactory.getLogger(QueryReader.class);
	
	@Override
	public boolean isWriteable(Class<?> type, Type generic, 
										Annotation[] antns, MediaType mt) {
		return generic == ModelFrame.class;
	}

	@Override
	public long getSize(ModelFrame mf, Class<?> type, Type generic, 
										Annotation[] antns, MediaType mt) {
		return 0; // ignored by Jersey 2.0 anyway
	}

	@Override
	public void writeTo(ModelFrame mf, Class<?> type, Type generic, 
						Annotation[] antns, MediaType mt, 
						MultivaluedMap<String, Object> mm, OutputStream out) 
									throws IOException, WebApplicationException {
		if (mf.getModel().isEmpty()) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		try {
			if (mf.getFrame() != null) {
				// get unaltered JSON-LD serialization 
				StringWriter w = new StringWriter();
				Rio.write(mf.getModel(), w, RDFFormat.JSONLD);
		
				// modify the output using JSON-LD Framing
				Object json = JsonUtils.fromString(w.toString());
				Object frame = JsonUtils.fromString(mf.getFrame());

				JsonUtils.writePrettyPrint(
						new OutputStreamWriter(out, Charsets.UTF_8),
						JsonLdProcessor.frame(json, frame, new JsonLdOptions()));
			} else {
				// no frame required
				Rio.write(mf.getModel(), out, RDFFormat.JSONLD);
			}
		} catch (RDFHandlerException|JsonLdError ex) {
			throw new WebApplicationException(ex);
		} 
	}
}
