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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Structured (lowercase annotations like @param) query comments helper class
 * 
 * @author Bart.Hanssens
 */
public class QueryComment {
	private final static Pattern P = Pattern.compile(" ?@([a-z]+) (.+)");
	private final String desc;
	
	private final Map<String,List<String>> annots = new HashMap();
	
	/**
	 * Get description
	 * 
	 * @return description
	 */
	public String getDescription() {
		return this.desc;
	}
	
	/**
	 * Get a list of values for a given annotation, if any
	 * 
	 * @param annot annotation code
	 * @return list of values
	 */
	public List<String> get(String annot) {
		return annots.getOrDefault(annot, Collections.emptyList());
	}
	
	/**
	 * Add a annotation value 
	 * 
	 * @param annot
	 * @param value 
	 */
	private void add(String annot, String value) {
		List<String> lst = annots.get(annot);
		if (lst == null) {
			lst = new ArrayList<>();
			annots.put(annot,lst);
		}
		lst.add(value);
	}
	
	/**
	 * Constructor
	 * 
	 * @param text 
	 */
	public QueryComment(String text) {
		if (text != null) {
			StringBuilder sb = new StringBuilder(text.length());
			String[] splits = text.split("#");
			
			for(String s: splits) {
				Matcher m = P.matcher(s);
				if (m.matches()) {
					add(m.group(1), m.group(2));
				} else {
					sb.append(s);
				}
			}
			this.desc = sb.toString();
		} else {
			this.desc = "";
		}
	}
}
