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

package org.olat.resource.accesscontrol.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Description:<br>
 * The order contains a list of order part. Every Order part links
 * a set of order lines to a payment.
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderImpl extends PersistentObject implements Order, ModifiedInfo {

	private boolean valid;
	private Date lastModified;
	private Identity delivery;
	private List<OrderPart> parts;
	
	
	@Override
	public String getOrderNr() {
		return getKey() == null ? "" : getKey().toString();
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public Identity getDelivery() {
		return delivery;
	}

	public void setDelivery(Identity delivery) {
		this.delivery = delivery;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public List<OrderPart> getParts() {
		if(parts == null) {
			parts = new ArrayList<OrderPart>();
		}
		return parts;
	}

	public void setParts(List<OrderPart> parts) {
		this.parts = parts;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 27591 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrderImpl) {
			OrderImpl order = (OrderImpl)obj;
			return equalsByPersistableKey(order);
		}
		return false;
	}
}
