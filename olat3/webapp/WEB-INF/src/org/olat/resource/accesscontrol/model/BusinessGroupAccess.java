package org.olat.resource.accesscontrol.model;

import java.util.List;

import org.olat.group.BusinessGroup;

public class BusinessGroupAccess {
	
	private BusinessGroup group;
	private List<AccessMethod> methods;
	
	public BusinessGroupAccess() {
		//
	}
	
	public BusinessGroupAccess(BusinessGroup group, List<AccessMethod> methods) {
		this.group = group;
		this.methods = methods;
	}
	
	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public List<AccessMethod> getMethods() {
		return methods;
	}
	
	

}
