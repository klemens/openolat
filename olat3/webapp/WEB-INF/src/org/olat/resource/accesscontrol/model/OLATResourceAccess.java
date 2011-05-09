package org.olat.resource.accesscontrol.model;

import java.util.List;

import org.olat.resource.OLATResource;

public class OLATResourceAccess {
	
	private OLATResource resource;
	private List<AccessMethod> methods;
	
	public OLATResourceAccess() {
		//
	}
	
	public OLATResourceAccess(OLATResource resource, List<AccessMethod> methods) {
		this.resource = resource;
		this.methods = methods;
	}
	
	public OLATResource getResource() {
		return resource;
	}
	
	public void setResource(OLATResource resource) {
		this.resource = resource;
	}
	
	public List<AccessMethod> getMethods() {
		return methods;
	}
	
	public void setMethods(List<AccessMethod> methods) {
		this.methods = methods;
	}
}
