package org.olat.resource.accesscontrol.model;

public interface AccessTransaction {

	public Long getKey();
	
	public Order getOrder();
	
	public OrderPart getOrderPart();
	
	public AccessMethod getMethod();
	
}
