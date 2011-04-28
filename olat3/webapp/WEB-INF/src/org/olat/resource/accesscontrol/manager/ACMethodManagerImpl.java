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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.OfferAccessImpl;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;

/**
 * 
 * Description:<br>
 * This class manages the methods available to access the resource.
 * As standard "static" (static as singleton), there is Token  and Free
 * based access.
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACMethodManagerImpl extends BasicManager implements ACMethodManager {

	private DB dbInstance;
	private final AccessControlModule acModule;
	
	public ACMethodManagerImpl(AccessControlModule acModule) {
		this.acModule = acModule;
	}

	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	/**
	 * [used by Spring]
	 */
	public void init() {
		//used by Spring
		if(acModule.isTokenEnabled()) {
			activateTokenMethod();
		}
		if(acModule.isFreeEnabled()) {
			activateFreeMethod();
		}
	}

	@Override
	public List<AccessMethod> getAvailableMethods(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName()).append(" method")
			.append(" where method.valid=true");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		if(identity != null) {
			//query.setLong("identityKey", identity.getKey());
		}
	
		List<AccessMethod> methods = query.list();
		return methods;
	}
	
	public List<AccessMethod> getAvailableMethodsByType(Class<? extends AccessMethod> type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName()).append(" method")
			.append(" where method.valid=true")
			.append(" and method.class=").append(type.getName());
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		return methods;
	}

	@Override
	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select access from ").append(OfferAccessImpl.class.getName()).append(" access")
			.append(" where access.offer=:offer")
			.append(" and access.valid=").append(valid);

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setEntity("offer", offer);
		
		List<OfferAccess> methods = query.list();
		return methods;
	}
	
	@Override
	public List<OfferAccess> getOfferAccess(List<Offer> offers, boolean valid) {
		if(offers == null || offers.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select access from ").append(OfferAccessImpl.class.getName()).append(" access")
			.append(" where access.offer in (:offers)")
			.append(" and access.valid=").append(valid);
		

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setParameterList("offers", offers);
		
		List<OfferAccess> methods = query.list();
		return methods;
	}
	
	@Override
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		OfferAccessImpl access = new OfferAccessImpl();
		access.setOffer(offer);
		access.setMethod(method);
		access.setValid(true);
		return access;
	}
	
	@Override
	public void save(OfferAccess link) {
		if(link.getKey() == null) {
			dbInstance.saveObject(link);
		} else {
			dbInstance.updateObject(link);
		}
	}
	
	@Override
	public void delete(OfferAccess link) {
		OfferAccessImpl access = (OfferAccessImpl)link;
		access.setValid(false);
	
		if(link.getKey() == null) return;
		dbInstance.updateObject(access);
	}
	
	/**
	 * Activate the token method if not already configured.
	 */
	protected void activateTokenMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName())
			.append(" method where method.class=").append(TokenAccessMethod.class.getName());
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		if(methods.isEmpty()) {
			dbInstance.saveObject(new TokenAccessMethod());
		}
	}
	
	protected void activateFreeMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName())
			.append(" method where method.class=").append(FreeAccessMethod.class.getName());
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		if(methods.isEmpty()) {
			dbInstance.saveObject(new FreeAccessMethod());
		}
	}
}