/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fedict.lodtools.query.helpers;

import org.eclipse.rdf4j.model.Model;

/**
 *
 * @author Bart.Hanssens
 */
public class ModelFrame {
	private final Model m;
	private final String  f;
	
	public Model getModel() {
		return this.m;
	}
	
	public String getFrame() {
		return f;
	}
	
	public ModelFrame(Model m, String f) {
		this.m = m;
		this.f = f;
	}
}
