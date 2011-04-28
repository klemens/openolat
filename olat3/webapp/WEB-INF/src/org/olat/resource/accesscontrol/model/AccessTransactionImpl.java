package org.olat.resource.accesscontrol.model;

import org.olat.core.commons.persistence.PersistentObject;

public class AccessTransactionImpl extends PersistentObject implements AccessTransaction {

	private Order order;
	private OrderPart orderPart;
	
	private AccessMethod method;
	private AccessTransactionStatus status = AccessTransactionStatus.is_new;
	
	public AccessTransactionImpl(){
		//
	}
	
	@Override
	public AccessMethod getMethod() {
		return method;
	}
	
	public void setMethod(AccessMethod method) {
		this.method = method;
	}
	
	@Override
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	@Override
	public OrderPart getOrderPart() {
		return orderPart;
	}
	
	public void setOrderPart(OrderPart orderPart) {
		this.orderPart = orderPart;
	}
	
	public AccessTransactionStatus getStatus() {
		return status;
	}

	public void setStatus(AccessTransactionStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 93791 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AccessTransactionImpl) {
			AccessTransactionImpl accessTransaction = (AccessTransactionImpl)obj;
			return equalsByPersistableKey(accessTransaction);
		}
		return false;
	}
}
