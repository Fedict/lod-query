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
package be.fedict.lodtools.query.views;

import io.dropwizard.views.View;

import java.nio.charset.StandardCharsets;

/**
 * HTML view for  reconciliation preview
 * 
 * @author Bart.Hanssens
 */
public class PreviewView extends View {
	private final String id;
	private final String[] labels;
	
	/**
	 * Get the ID of the object to preview
	 * 
	 * @return ID as string
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Get list of labels associated with the ID
	 * 
	 * @return array of services
	 */
	public String[] getLabels() {
		return this.labels;
	}
	
	/** 
	 * Constructor
	 * 
	 * @param id id of thing to preview
	 * @param labels list of labels
	 */
	public PreviewView(String id, String[] labels) {
		super("preview.ftl", StandardCharsets.UTF_8);
		this.id = id;
		this.labels = labels;
	}
}

