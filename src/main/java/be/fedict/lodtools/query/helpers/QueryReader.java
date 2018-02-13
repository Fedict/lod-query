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
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read the query string from a source
 * 
 * @author Bart.Hanssens
 */
public class QueryReader {
	private final static Logger LOG = LoggerFactory.getLogger(QueryReader.class);
	
	private final String root;
	
	/**
	 * Read the query or frame from text file
	 * 
	 * @param repoName repository name
	 * @param file file name
	 * @return raw file content
	 * @throws IOException
	 */
	private String read(String repoName, String file) throws IOException {		
		Path p = Paths.get(root, repoName, file);
		LOG.info("Load from {}", file);
		
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader r = Files.newBufferedReader(p)) {
			r.lines().filter(line -> !line.startsWith("#"))
					.forEach(buffer::append);
		}
		return buffer.toString();
	}
	
	/**
	 * Get ordered list of repository names
	 * 
	 * @return array of names
	 */
	public String[] listRepositories() {
		File[] dirs = Paths.get(root).toFile().listFiles();
		if (dirs == null) {
			LOG.error("Could not list files in {}", root);
			return new String[0];
		}
		return Arrays.stream(dirs)
					.filter(File::isDirectory).map(File::getName).sorted()
					.toArray(String[]::new);
	}
	
	/**
	 * List all queries for a specific repository as an ordered map,
	 * with the filename as key and the structured comment as value
	 * 
	 * @param repoName
	 * @return list of file names and comments
	 */
	public Map<String,QueryComment> listQueries(String repoName) {
		Map<String,QueryComment> map = new TreeMap<>();
		
		File[] files = Paths.get(root, repoName).toFile()
				.listFiles(f -> { return f.getName().endsWith(".qr"); } );
		
		if (files == null) {
			LOG.error("Could not get queries for repository {}", repoName);
			return map;
		}
		
		for(File f: files) {
			StringBuilder buffer = new StringBuilder();
			try (BufferedReader r = Files.newBufferedReader(f.toPath())) {
				r.lines().filter(line -> line.startsWith("#"))
						.forEach(line -> buffer.append(line));
			} catch(IOException ioe) {
				LOG.warn("Couldn't read file {}", f);
			}
			map.put(f.getName(), new QueryComment(buffer.toString()));
		}
		return map;
	}
	
	/**
	 * Get the JSON-LD Frame
	 * 
	 * @param repoName repository name
	 * @param qryName query name
	 * @return JSON-LD frame or null
	 */
	public String getFrame(String repoName, String qryName) {
		try {
			return read(repoName, qryName + ".frame");
		} catch (IOException ex) {
			LOG.info("Could not read JSON-LD frame");
			return null;
		}
	}
	
	/**
	 * Get the query string
	 * 
	 * @param repoName repository name
	 * @param qryName query name
	 * @return raw query string
	 * @throws WebApplicationException
	 */
	public String getQuery(String repoName, String qryName) {
		try {
			return read(repoName, qryName + ".qr");
		} catch (IOException ex) {
			LOG.error("Couldn't read query {} for repo {}", qryName, repoName);
			throw new WebApplicationException(ex);
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param root root directory
	 */
	public QueryReader(String root) {
		this.root = root;
	}
}
