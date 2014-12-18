/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.assessment.model.AssessmentModeToGroupImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("assessmentModeManager")
public class AssessmentModeManagerImpl implements AssessmentModeManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public AssessmentMode createAssessmentMode(RepositoryEntry entry) {
		AssessmentModeImpl mode = new AssessmentModeImpl();
		mode.setCreationDate(new Date());
		mode.setLastModified(new Date());
		mode.setRepositoryEntry(entry);
		return mode;
	}

	@Override
	public AssessmentMode save(AssessmentMode assessmentMode) {
		AssessmentMode reloadedMode;
		assessmentMode.setLastModified(new Date());
		if(assessmentMode.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(assessmentMode);
			reloadedMode = assessmentMode;
		} else {
			reloadedMode = dbInstance.getCurrentEntityManager()
					.merge(assessmentMode);
		}
		return reloadedMode;
	}

	@Override
	public AssessmentMode getAssessmentModeById(Long key) {
		String q = "select mode from courseassessmentmode mode where mode.key=:modeKey";
		List<AssessmentMode> modes = dbInstance.getCurrentEntityManager()
			.createQuery(q, AssessmentMode.class)
			.setParameter("modeKey", key)
			.getResultList();
		
		return modes == null || modes.isEmpty() ? null : modes.get(0);
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(RepositoryEntryRef entry) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("assessmentModeByRepoEntry", AssessmentMode.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(IdentityRef identity) {
		List<AssessmentMode> currentModes = getCurrentAssessmentModes();
		List<AssessmentMode> myModes = null;
		if(currentModes.size() > 0) {
			//check permissions, groups, areas, course
			myModes = loadAssessmentModeFor(identity, currentModes);
		}
		return myModes == null ? Collections.<AssessmentMode>emptyList() : myModes;
	}
	
	private List<AssessmentMode> loadAssessmentModeFor(IdentityRef identity, List<AssessmentMode> currentModes) {
		StringBuilder sb = new StringBuilder(1000);
		sb.append("select mode from courseassessmentmode mode ")
		  .append(" inner join fetch mode.repositoryEntry entry")
		  .append(" left join mode.groups as modeToGroup")
		  .append(" where mode.key in (:modeKeys)")
		  .append("  and ((mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.groups.name()).append("')")
		  .append("   and exists (select businessGroup from ").append(BusinessGroupImpl.class.getName()).append(" as businessGroup, bgroupmember as membership  ")
		  .append("     where modeToGroup.businessGroup=businessGroup and membership.group=businessGroup.baseGroup and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  )) or (mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.course.name()).append("')")
		  .append("   and exists (select rel from repoentrytogroup as rel,  bgroupmember as membership ")
		  .append("     where mode.repositoryEntry=rel.entry and membership.group=rel.group and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  ))")
		  .append(" )");
		
		List<Long> modeKeys = new ArrayList<>(currentModes.size());
		for(AssessmentMode mode:currentModes) {
			modeKeys.add(mode.getKey());
		}
		List<AssessmentMode> modeList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("modeKeys", modeKeys)
				.getResultList();
		//quicker than distinct
		return new ArrayList<AssessmentMode>(new HashSet<AssessmentMode>(modeList));
	}

	@Override
	public Set<Long> getAssessedIdentities(AssessmentMode assessmentMode) {
		Target targetAudience = assessmentMode.getTargetAudience();
		RepositoryEntry re = assessmentMode.getRepositoryEntry();
		
		Set<Long> assessedKeys = new HashSet<>();
		if(targetAudience == Target.course || targetAudience == Target.courseAndGroups) {
			List<Long> courseMemberKeys = assessmentMode.isApplySettingsForCoach()
					? repositoryEntryRelationDao.getMemberKeys(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name(), GroupRoles.participant.name())
					: repositoryEntryRelationDao.getMemberKeys(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
			assessedKeys.addAll(courseMemberKeys);
		}
		if(targetAudience == Target.groups || targetAudience == Target.courseAndGroups) {
			Set<AssessmentModeToGroup> modeToGroups = assessmentMode.getGroups();
			if(modeToGroups.size() > 0) {
				List<BusinessGroup> groups = new ArrayList<>(modeToGroups.size());
				for(AssessmentModeToGroup modeToGroup: modeToGroups) {
					groups.add(modeToGroup.getBusinessGroup());
				}

				List<Long> groupMemberKeys = assessmentMode.isApplySettingsForCoach()
						? businessGroupRelationDao.getMemberKeys(groups, GroupRoles.coach.name(), GroupRoles.participant.name())
						: businessGroupRelationDao.getMemberKeys(groups, GroupRoles.participant.name());
				assessedKeys.addAll(groupMemberKeys);		
			}
			
		}
		
		return assessedKeys;
	}

	@Override
	public List<AssessmentMode> getCurrentAssessmentModes() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		Date now = cal.getTime();
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("currentAssessmentModes", AssessmentMode.class)
				.setParameter("now", now)
				.getResultList();
	}

	@Override
	public AssessmentModeToGroup createAssessmentModeToGroup(AssessmentMode mode, BusinessGroup group) {
		AssessmentModeToGroupImpl modeToGroup = new AssessmentModeToGroupImpl();
		modeToGroup.setAssessmentMode(mode);
		modeToGroup.setBusinessGroup(group);
		dbInstance.getCurrentEntityManager().persist(modeToGroup);
		return modeToGroup;
	}
	
	
}