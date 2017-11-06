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

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read the query string from a source
 * 
 * @author Bart.Hanssens
 */
public class FrameReader {
	private final static Logger LOG = LoggerFactory.getLogger(FrameReader.class);
	
	private final String root;
	/**
	 * Get the query string
	 * 
	 * @param repoName repository name
	 * @param qryName query name
	 * @return raw query string
	 */
	public String get(String repoName, String qryName) {
		StringBuffer buffer = new StringBuffer();
		
		Path file = Paths.get(root, repoName, qryName + ".frame");
		LOG.info("Load frame from {}", file);
		try (BufferedReader r = Files.newBufferedReader(file)) {
			r.lines().forEach(buffer::append);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
		LOG.info("{}", buffer.toString());
		return buffer.toString();
	}
	
	/**
	 * Constructor
	 * 
	 * @param root root directory
	 * 
	 */
	public FrameReader(String root) {
		this.root = root;
	}
}
