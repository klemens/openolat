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

package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderPart;

/**
 * 
 * Description:<br>
 * A data model which hold the orders and their transactions
 * 
 * <P>
 * Initial Date:  20 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersDataModel implements TableDataModel {
	
	private final Locale locale;
	private List<OrderTableItem> orders;
	private final AccessControlModule acModule;
	
	public OrdersDataModel(List<OrderTableItem> orders, Locale locale) {
		this.orders = orders;
		this.locale = locale;
		acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return orders.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		OrderTableItem order = orders.get(row);
		switch(Col.get(col)) {
			case orderNr: return order.getOrder().getOrderNr();
			case creationDate: return order.getOrder().getCreationDate();
			case delivery: {
				User user = order.getOrder().getDelivery().getUser();
				return user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
			}
			case methods: {
				List<AccessTransaction> transactions = order.getTransactions();
				StringBuilder sb = new StringBuilder();
				for(AccessTransaction transaction:transactions) {
					String type = transaction.getMethod().getType();
					if(sb.length() > 0) {
						sb.append(", ");
					}
					AccessMethodHandler handler = acModule.getAccessMethodHandler(type);
					String methodName = handler.getMethodName(locale);
//					sb.append(methodName);
// FIXME: check if we can move CSS rendering to an implementation of CustomCssCellRenderer or something
					sb.append("<span class='b_with_small_icon_left ");
					sb.append(transaction.getMethod().getMethodCssClass()).append("_icon'>");
					sb.append(methodName);
					sb.append("</span>");

				}
				return sb.toString();
			}
			case summary: {
				StringBuilder sb = new StringBuilder();
				for(OrderPart part:order.getOrder().getParts()) {
					for(OrderLine lines:part.getOrderLines()) {
						String displayName = lines.getOffer().getResourceDisplayName();
						if(sb.length() > 0) {
							sb.append(", ");
						}
						sb.append(displayName);
					}
				}
				return sb.toString();
			}
			default: return order;
		}
	}

	@Override
	public OrderTableItem getObject(int row) {
		return orders.get(row);
	}

	@Override
	public void setObjects(List objects) {
		this.orders = objects;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new OrdersDataModel(Collections.<OrderTableItem>emptyList(), locale);
	}
	
	
	public static List<OrderTableItem> create(List<Order> orders, List<AccessTransaction> transactions) {
		List<OrderTableItem> items = new ArrayList<OrderTableItem>();
		
		for(Order order:orders) {
			OrderTableItem item = new OrderTableItem(order);
			for(AccessTransaction transaction:transactions) {
				if(transaction.getOrder().equals(order)) {
					item.getTransactions().add(transaction);
				}
			}
			items.add(item);
		}
		
		return items;
	}
	
	public enum Col {
		orderNr,
		creationDate,
		delivery,
		methods,
		summary;

		public static Col get(int index) {
			return values()[index];
		}
	}
}
