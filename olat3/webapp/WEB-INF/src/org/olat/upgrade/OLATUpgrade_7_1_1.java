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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.upgrade;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.repository.RepositoryEntry;


public class OLATUpgrade_7_1_1 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_7.1.1";

	private static final String MIGRATE_SECURITY_GROUP = "Migrate repository entry security groups";
	
	private static final int REPO_ENTRIES_BATCH_SIZE = 20;

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}
		
		migrateSecurityGroups(upgradeManager, uhd);
			
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		log.audit("Finished OLATUpgrade_7_1_1 successfully!" );
		return true;
	}
	
	//fxdiff VCRP-1: access control repository entry
	private void migrateSecurityGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(MIGRATE_SECURITY_GROUP)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Migrate the repository entry security groups from business groups     ...+");
			log.audit("+-----------------------------------------------------------------------------+");

			int counter = 0;
			List<RepositoryEntry> entries;
			do {
				entries = queryEntries(counter);
				for(RepositoryEntry entry:entries) {
					createRepoEntrySecurityGroups(entry);
					migrateRepoEntrySecurityGroups(entry);
				}
				counter += entries.size();
				log.audit("Processed entries: " + entries.size());
			} while(entries.size() == REPO_ENTRIES_BATCH_SIZE);
			
			log.audit("+... Migration processed " + counter + " repository entries     ...+");

			uhd.setBooleanDataValue(MIGRATE_SECURITY_GROUP, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private void createRepoEntrySecurityGroups(RepositoryEntry entry) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		
		boolean save = false;
		if(entry.getTutorGroup() == null) {
			// security group for tutors / coaches
			SecurityGroup tutorGroup = securityManager.createAndPersistSecurityGroup();
			// member of this group may modify member's membership
			securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_ACCESS, entry.getOlatResource());
			// members of this group are always tutors also
			securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_TUTOR);
			entry.setTutorGroup(tutorGroup);
	
			securityManager.createAndPersistPolicy(entry.getTutorGroup(), Constants.PERMISSION_COACH, entry.getOlatResource());

			DBFactory.getInstance().commit();
			save = true;
		}
		
		if(entry.getParticipantGroup() == null) {
			// security group for participants
			SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
			// member of this group may modify member's membership
			securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_ACCESS, entry.getOlatResource());
			// members of this group are always participants also
			securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_PARTICIPANT);
			entry.setParticipantGroup(participantGroup);
	
			securityManager.createAndPersistPolicy(entry.getParticipantGroup(), Constants.PERMISSION_PARTI, entry.getOlatResource());

			DBFactory.getInstance().commit();
			save = true;
		}
		
		if(save) {
			DBFactory.getInstance().updateObject(entry);
		}
	}
	
	private void migrateRepoEntrySecurityGroups(RepositoryEntry entry) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();

		List<BGContext> contexts = contextManager.findBGContextsForResource(entry.getOlatResource(), true, true);
		for(BGContext context:contexts) {
			List<BusinessGroup> groups = contextManager.getGroupsOfBGContext(context);
			for(BusinessGroup group:groups) {
				//migrate tutors
				if(group.getOwnerGroup() != null) {
					int count = 0;
					List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
					SecurityGroup tutorGroup = entry.getTutorGroup();
					for(Identity owner:owners) {
						if(securityManager.isIdentityInSecurityGroup(owner, tutorGroup)) {
							continue;
						}
						securityManager.addIdentityToSecurityGroup(owner, tutorGroup);
						if(count++ % 20 == 0) {
							DBFactory.getInstance().intermediateCommit();
						}
					}
					DBFactory.getInstance().intermediateCommit();
				}
				
				//migrate participants
				if(group.getPartipiciantGroup() != null) {
					int count = 0;
					List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
					SecurityGroup participantGroup = entry.getParticipantGroup();
					for(Identity participant:participants) {
						if(securityManager.isIdentityInSecurityGroup(participant, participantGroup)) {
							continue;
						}
						securityManager.addIdentityToSecurityGroup(participant, participantGroup);
						if(count++ % 20 == 0) {
							DBFactory.getInstance().intermediateCommit();
						}
					}
	
					DBFactory.getInstance().intermediateCommit();
				}
			}
		}
	}
	
	public List<RepositoryEntry> queryEntries(int firstResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" v inner join fetch v.olatResource as res order by v.key asc");
		DBQuery dbquery = DBFactory.getInstance().createQuery(sb.toString());
		dbquery.setFirstResult(firstResult);
		dbquery.setMaxResults(REPO_ENTRIES_BATCH_SIZE);
		return dbquery.list();
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#getVersion()
	 */
	@Override
	public String getVersion() {
		return VERSION;
	}
}
