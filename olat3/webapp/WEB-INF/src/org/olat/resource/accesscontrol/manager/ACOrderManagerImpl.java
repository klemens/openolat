/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.manager;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderImpl;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderLineImpl;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.OrderPartImpl;

/**
 * 
 * Description:<br>
 * manager for the order. Orders are a part of the confirmation of an access
 * to a resource. The second part is the transaction.
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACOrderManagerImpl extends BasicManager implements ACOrderManager {
	
	private DB dbInstance;
	
	private ACOrderManagerImpl() {
		//
	}

	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	@Override
	public Order createOrder(Identity delivery) {
		OrderImpl order = new OrderImpl();
		order.setDelivery(delivery);
		dbInstance.saveObject(order);
		return order;
	}

	@Override
	public OrderPart addOrderPart(Order order) {
		OrderPartImpl orderPart = new OrderPartImpl();
		dbInstance.saveObject(orderPart);
		order.getParts().add(orderPart);
		return orderPart;
	}

	@Override
	public OrderLine addOrderLine(OrderPart part, Offer offer) {
		OrderLineImpl line = new OrderLineImpl();
		line.setOffer(offer);
		dbInstance.saveObject(line);
		part.getOrderLines().add(line);
		return line;
	}
	
	@Override
	public Order save(Order order) {
		if(order.getKey() == null) {
			dbInstance.saveObject(order);
		} else {
			dbInstance.updateObject(order);
		}
		return order;
	}
	
	@Override
	public Order saveOneClick(Identity delivery, OfferAccess link) {
		Order order = createOrder(delivery);
		OrderPart part = addOrderPart(order);
		addOrderLine(part, link.getOffer());
		return order;
	}
	
	@Override
	public List<Order> findOrdersByDelivery(Identity delivery) {
		StringBuilder sb = new StringBuilder();
		sb.append("select order from ").append(OrderImpl.class.getName()).append(" order")
			.append(" where order.delivery.key=:deliveryKey");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("deliveryKey", delivery.getKey());
	
		List<Order> orders = query.list();
		return orders;
	}

	@Override
	public List<Order> findOrdersByResource(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from ").append(OrderImpl.class.getName()).append(" o")
			.append(" inner join o.parts orderPart ")
			.append(" inner join orderPart.orderLines orderLine ")
			.append(" inner join orderLine.offer offer ")
			.append(" where offer.resource.key=:resourceKey");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("resourceKey", resource.getKey());
	
		List<Order> orders = query.list();
		return orders;
	}

	@Override
	public Order loadOrderByKey(Long orderKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select order from ").append(OrderImpl.class.getName()).append(" order")
			.append(" where order.key=:orderKey");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("orderKey", orderKey);
	
		List<Order> orders = query.list();
		if(orders.isEmpty()) return null;
		return orders.get(0);
	}
}
