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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * JSON Writer for JSON Node 
 * 
 * @author Bart.Hanssens
 */
@Provider
@Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
public class JsonCallbackMessageBodyWriter implements MessageBodyWriter<JsonCallback> {
	private final static JsonFactory FAC = new JsonFactory();
	
	@Override
	public boolean isWriteable(Class<?> type, Type generic, 
										Annotation[] antns, MediaType mt) {
		return generic == JsonCallback.class;
	}

	@Override
	public long getSize(JsonCallback jc, Class<?> type, Type generic, 
										Annotation[] antns, MediaType mt) {
		return 0; // ignored by Jersey 2.0 anyway
	}

	@Override
	public void writeTo(JsonCallback jc, Class<?> type, Type generic, 
						Annotation[] antns, MediaType mt, 
						MultivaluedMap<String, Object> mm, OutputStream out) 
									throws IOException, WebApplicationException {
		if (jc.getNode().isNull()) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		String callback = jc.getCallback();
		try {
			if (! callback.isEmpty()) {
				mm.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);
				out.write(callback.getBytes());
				out.write('(');
			} else {
				mm.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
			}
			JsonGenerator generator = FAC.createGenerator(out);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeTree(generator, jc.getNode());
			
			if (! callback.isEmpty()) {
				out.write(");".getBytes());
			}
		} catch (IOException ioe) {
			throw new WebApplicationException(ioe);
		}
	}
}
