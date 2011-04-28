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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;

/**
 * 
 * Description:<br>
 * The access control is not intend for security check.
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACFrontendManager extends BasicManager {
	
	private BaseSecurity securityManager;
	private AccessControlModule accessModule;
	private ACOfferManager accessManager;
	private ACMethodManager methodManager;
	private ACOrderManager orderManager;
	private ACTransactionManager transactionManager;
	
	private ACFrontendManager() {
		//
	}

	/**
	 * [used by Spring]
	 * @param accessModule
	 */
	public void setAccessModule(AccessControlModule accessModule) {
		this.accessModule = accessModule;
	}

	/**
	 * [used by Spring]
	 * @param accessmanager
	 */
	public void setAccessManager(ACOfferManager accessManager) {
		this.accessManager = accessManager;
	}
	
	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}
	
	/**
	 * [used by Spring]
	 * @param methodManager
	 */
	public void setMethodManager(ACMethodManager methodManager) {
		this.methodManager = methodManager;
	}
	
	/**
	 * [used by Spring]
	 * @param orderManager
	 */
	public void setOrderManager(ACOrderManager orderManager) {
		this.orderManager = orderManager;
	}
	
	/**
	 * [used by Spring]
	 * @param transactionManager
	 */
	public void setTransactionManager(ACTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * The rule to access the repository entry:<br/>
	 * -No offer, access is free<br/>
	 * -Owners have always access to the resource<br/>
	 * -Tutors have access to the resource<br/>
	 * -Participants have access to the resource<br/>
	 * @param entry
	 * @param forId
	 * @return
	 */
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}
		
		List<Offer> offers = accessManager.findOfferByResource(entry.getOlatResource(), true, new Date());
		if(offers.isEmpty()) {
			return new AccessResult(true);
		}
		
		boolean owner = securityManager.isIdentityInSecurityGroup(forId, entry.getOwnerGroup());
		if(owner) {
			return new AccessResult(true);
		}
		
		if(entry.getTutorGroup() != null) {
			boolean tutor = securityManager.isIdentityInSecurityGroup(forId, entry.getTutorGroup());
			if(tutor) {
				return new AccessResult(true);
			}
		}
		
		if(entry.getParticipantGroup() != null) {
			boolean participant = securityManager.isIdentityInSecurityGroup(forId, entry.getParticipantGroup());
			if(participant) {
				return new AccessResult(true);
			}
		}
		return isAccessible(forId, offers, allowNonInteractiveAccess);
	}
	
	/**
	 * The rule to access a business group:<br/>
	 * -No offer, access is free<br/>
	 * -Owners have always access to the resource<br/>
	 * -Tutors have access to the resource<br/>
	 * -Participants have access to the resource<br/>
	 * @param entry
	 * @param forId
	 * @return
	 */
	public AccessResult isAccessible(BusinessGroup group, Identity forId, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}
		
		OLATResource resource = OLATResourceManager.getInstance().findResourceable(group);
		List<Offer> offers = accessManager.findOfferByResource(resource, true, new Date());
		if(offers.isEmpty()) {
			return new AccessResult(true);
		}
		
		boolean tutor = securityManager.isIdentityInSecurityGroup(forId, group.getOwnerGroup());
		if(tutor) {
			return new AccessResult(true);
		}
		
		if(group.getPartipiciantGroup() != null) {
			boolean participant = securityManager.isIdentityInSecurityGroup(forId, group.getPartipiciantGroup());
			if(participant) {
				return new AccessResult(true);
			}
		}
		return isAccessible(forId, offers, allowNonInteractiveAccess);
	}
	
	protected AccessResult isAccessible(Identity identity, List<Offer> offers, boolean allowNonInteractiveAccess) {
		List<OfferAccess> offerAccess = methodManager.getOfferAccess(offers, true);
		if(offerAccess.isEmpty()) {
			return new AccessResult(false);
		}
		if(allowNonInteractiveAccess && offerAccess.size() == 1) {
			//is it a method without user interaction as the free access?
			OfferAccess link = offerAccess.get(0);
			if(!link.getMethod().isNeedUserInteraction()) {
				return accessResource(identity, link, null);
			}
		}
		return new AccessResult(false, offerAccess);
	}
	
	public Offer createOffer(OLATResource resource, String resourceName) {
		return accessManager.createOffer(resource, resourceName);
	}
	
	public void deleteOffer(Offer offer) {
		accessManager.deleteOffer(offer);
	}
	
	public List<RepositoryEntry> filterRepositoryEntriesWithAC(List<RepositoryEntry> repoEntries) {
		List<Long> resourceKeys = new ArrayList<Long>();
		for(RepositoryEntry entry:repoEntries) {
			OLATResource ores = entry.getOlatResource();
			resourceKeys.add(ores.getKey());
		}
		
		Set<Long> resourceWithOffers = accessManager.filterResourceWithOffer(resourceKeys);
		
		List<RepositoryEntry> entriesWithOffers = new ArrayList<RepositoryEntry>();
		for(RepositoryEntry entry:repoEntries) {
			OLATResource ores = entry.getOlatResource();
			if(resourceWithOffers.contains(ores.getKey())) {
				entriesWithOffers.add(entry);
			}
		}
		return entriesWithOffers;
	}
	
	public Set<Long> filterResourcesWithAC(Collection<Long> resourceKeys) {
		Set<Long> resourceWithOffers = accessManager.filterResourceWithOffer(resourceKeys);
		return resourceWithOffers;
	}
	
	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate) {
		return accessManager.findOfferByResource(resource, valid, atDate);
	}
	
	public void save(Offer offer) {
		accessManager.saveOffer(offer);
	}
	
	public OfferAccess saveOfferAccess(OfferAccess link) {
		accessManager.saveOffer(link.getOffer());
		methodManager.save(link);
		return link;
	}
	
	public void saveOfferAccess(List<OfferAccess> links) {
		for(OfferAccess link:links) {
			accessManager.saveOffer(link.getOffer());
			methodManager.save(link);
		}
	}
	
	public AccessResult accessResource(Identity identity, OfferAccess link, Object argument) {
		if(link == null || link.getOffer() == null || link.getMethod() == null) {
			return new AccessResult(false);
		}
		
		AccessMethodHandler handler = accessModule.getAccessMethodHandler(link.getMethod().getType());
		if(handler == null) {
			return new AccessResult(false);
		}
		
		if(handler.checkArgument(link, argument)) {
			if(allowAccesToResource(identity, link)) {
				Order order = orderManager.saveOneClick(identity, link);
				AccessTransaction transaction = transactionManager.createTransaction(order, order.getParts().get(0), link.getMethod());
				transactionManager.save(transaction);
				return new AccessResult(true);
			} else {
				//TODO error handling
			}
		}
		return new AccessResult(false);
	}
	
	private boolean allowAccesToResource(Identity identity, OfferAccess link) {
		//check if offer is ok: key is stupid but further check as date, validity...
		Offer offer = link.getOffer();
		if(offer.getKey() == null) {
			return false;
		}
		
		//check the resource
		OLATResource resource = offer.getResource();
		if(resource == null || resource.getKey() == null || resource.getResourceableId() == null || resource.getResourceableTypeName() == null) {
			return false;
		}
		
		String resourceType = resource.getResourceableTypeName();
		if("CourseModule".equals(resourceType)) {
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(resource, false);
			if(entry != null) {
				if(!securityManager.isIdentityInSecurityGroup(identity, entry.getParticipantGroup())) {
					securityManager.addIdentityToSecurityGroup(identity, entry.getParticipantGroup());
				}
				return true;
			}
		} else if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(resource, false);
			if(group != null) {
				if(!securityManager.isIdentityInSecurityGroup(identity, group.getPartipiciantGroup())) {
					securityManager.addIdentityToSecurityGroup(identity, group.getPartipiciantGroup());
				}
				return true;
			}
		}
		return false;
	}
	
	public String resolveDisplayName(OLATResource resource) {
		String resourceType = resource.getResourceableTypeName();
		if("CourseModule".equals(resourceType)) {
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(resource, false);
			if(entry != null) {
				return entry.getDisplayname();
			}
		} else if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(resource, false);
			if(group != null) {
				return group.getName();
			}
		}
		return null;
	}
	
	public List<AccessMethod> getAvailableMethods(Identity identity) {
		return methodManager.getAvailableMethods(identity);
	}
	
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		return methodManager.createOfferAccess(offer, method);
	}
	
	public void deletedLinkToMethod(OfferAccess link) {
		methodManager.delete(link);
	}
	
	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid) {
		return methodManager.getOfferAccess(offer, valid);
	}
	
	public List<Order> findOrders(Identity delivery) {
		return orderManager.findOrdersByDelivery(delivery);
	}
	
	public List<AccessTransaction> findTransactions(List<Order> orders) {
		return transactionManager.loadTransactionsForOrders(orders);
	}
	
	public List<Order> findOrders(OLATResource resource) {
		return orderManager.findOrdersByResource(resource);
	}
}
