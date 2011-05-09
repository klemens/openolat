package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderPart;

public class OrderDetailController extends FormBasicController {
	
	private FormLink backLink;
	private TableController tableCtr;
	
	private final Order order;
	private List<AccessTransaction> transactions;
	
	private final AccessControlModule acModule;
	private final ACFrontendManager acFrontendManager;
	
	public OrderDetailController(UserRequest ureq, WindowControl wControl, Order order, List<AccessTransaction> transactions) {
		super(ureq, wControl, "order");
		
		this.order = order;
		this.transactions = transactions;
		acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		acFrontendManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		FormLayoutContainer mainLayout = FormLayoutContainer.createDefaultFormLayout("mainCmp", getTranslator());
		mainLayout.setRootForm(mainForm);
		formLayout.add("mainCmp", mainLayout);
		
		String orderNr = order.getOrderNr();
		uifactory.addStaticTextElement("order-nr", "order.nr", orderNr, mainLayout);	

		Date creationDate = order.getCreationDate();
		String creationDateStr = Formatter.getInstance(getLocale()).formatDateAndTime(creationDate);
		uifactory.addStaticTextElement("creation-date", "order.creationDate", creationDateStr, mainLayout);	
		
		User user = order.getDelivery().getUser();
		String delivery = user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
		uifactory.addStaticTextElement("delivery", "order.delivery", delivery, mainLayout);

		if(formLayout instanceof FormLayoutContainer) {
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setDownloadOffered(false);		
			tableConfig.setTableEmptyMessage(translate("orders.empty"));

			tableCtr = new TableController(tableConfig, ureq, getWindowControl(), Collections.<ShortName>emptyList(), null, null , null, false, getTranslator());
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.item.name", 0, null, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.part.payment", 1, null, getLocale()));
			
			loadOrderItems();
			listenTo(tableCtr);
			
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			layoutContainer.put("orderItemList", tableCtr.getInitialComponent());
		}
	}
	
	private void loadOrderItems() {
		List<OrderItemWrapper> items = new ArrayList<OrderItemWrapper>();
		for(OrderPart part: order.getParts()) {
			boolean first = true;
			
			AccessTransaction transaction = null;
			if(transactions != null) {
				for(AccessTransaction trx:transactions) {
					if(trx.getOrderPart().equals(part)) {
						transaction = trx;
						break;
					}
				}
			}

			for(OrderLine line:part.getOrderLines()) {
				OLATResource resource = line.getOffer().getResource();
				String displayName;
				if(resource == null) {
					displayName = line.getOffer().getResourceDisplayName();
				} else {
					displayName = acFrontendManager.resolveDisplayName(resource);
				}
				OrderItemWrapper wrapper = new OrderItemWrapper(part, line, transaction, displayName, first);
				items.add(wrapper);
				first = false;
			}
		}
		
		tableCtr.setTableDataModel(new OrderItemsDataModel(items));
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public class OrderItemsDataModel implements TableDataModel {
		
		private List<OrderItemWrapper> items;
		
		public OrderItemsDataModel(List<OrderItemWrapper> items) {
			this.items = items;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			OrderItemWrapper wrapper = getObject(row);
			switch(col) {
				case 0: {
					String name = wrapper.getDisplayName();
					if(StringHelper.containsNonWhitespace(name)) {
						return name;
					}
					return "-";
				}
				case 1: {
					if(wrapper.isFirst() && wrapper.getTransaction() != null) {
						String type = wrapper.getTransaction().getMethod().getType();
						AccessMethodHandler handler = acModule.getAccessMethodHandler(type);
						// FIXME: check if we can move CSS rendering to an implementation of CustomCssCellRenderer. 
						// return handler.getMethodName(getLocale());
						return "<span class='b_with_small_icon_left " + wrapper.getTransaction().getMethod().getMethodCssClass() +"_icon'>" + handler.getMethodName(getLocale()) + "</span>";
					}
					return "";
				}
				default: return wrapper;
			}
		}

		@Override
		public OrderItemWrapper getObject(int row) {
			return items.get(row);
		}

		@Override
		public void setObjects(List objects) {
			this.items = objects;
		}

		@Override
		public OrderItemsDataModel createCopyWithEmptyList() {
			return new OrderItemsDataModel(Collections.<OrderItemWrapper>emptyList());
		}
	}
	
	public class OrderItemWrapper {
		
		private final boolean first;
		private final OrderPart part;
		private final OrderLine item;
		private final AccessTransaction transaction;
		private final String displayName;
		
		public OrderItemWrapper(OrderPart part, OrderLine item, AccessTransaction transaction, String displayName,  boolean first) {
			this.part = part;
			this.item = item;
			this.first = first;
			this.transaction = transaction;
			this.displayName = displayName;
		}

		public boolean isFirst() {
			return first;
		}

		public String getDisplayName() {
			return displayName;
		}

		public OrderPart getPart() {
			return part;
		}

		public OrderLine getItem() {
			return item;
		}
		
		public AccessTransaction getTransaction() {
			return transaction;
		}
	}
}
