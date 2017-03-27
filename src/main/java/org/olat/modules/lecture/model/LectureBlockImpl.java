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
package org.olat.modules.lecture.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.Reason;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="lectureblock")
@Table(name="o_lecture_block")
public class LectureBlockImpl implements Persistable, LectureBlock {

	private static final long serialVersionUID = -1010006683915268916L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="l_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="l_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="l_descr", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="l_preparation", nullable=true, insertable=true, updatable=true)
	private String preparation;
	@Column(name="l_location", nullable=true, insertable=true, updatable=true)
	private String location;
	@Column(name="l_comment", nullable=true, insertable=true, updatable=true)
	private String comment;
	@Column(name="l_log", nullable=true, insertable=true, updatable=true)
	private String log;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_start_date", nullable=false, insertable=true, updatable=true)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_end_date", nullable=false, insertable=true, updatable=true)
	private Date endDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_eff_end_date", nullable=true, insertable=true, updatable=true)
	private Date effectiveEndDate;

	@Column(name="l_planned_lectures_num", nullable=true, insertable=true, updatable=true)
	private int plannedLecturesNumber;
	@Column(name="l_effective_lectures_num", nullable=true, insertable=true, updatable=true)
	private int effectiveLecturesNumber;
	@Column(name="l_effective_lectures", nullable=true, insertable=true, updatable=true)
	private String effectiveLectures;

	@Column(name="l_status", nullable=false, insertable=true, updatable=true)
	private String statusString;
	@Column(name="l_roll_call_status", nullable=false, insertable=true, updatable=true)
	private String rollCallStatusString;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_reason", nullable=true, insertable=true, updatable=true)
	private Reason reasonEffectiveEnd;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry entry;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_teacher_group", nullable=false, insertable=true, updatable=false)
	private Group teacherGroup;
	@OneToMany(targetEntity=LectureBlockToGroupImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_lecture_block")
	private Set<LectureBlockToGroup> groups;
	
	
	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getPreparation() {
		return preparation;
	}

	@Override
	public void setPreparation(String preparation) {
		this.preparation = preparation;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getLog() {
		return log;
	}

	@Override
	public void setLog(String log) {
		this.log = log;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public Date getEffectiveEndDate() {
		return effectiveEndDate;
	}

	@Override
	public void setEffectiveEndDate(Date effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	@Override
	public LectureBlockStatus getStatus() {
		return StringHelper.containsNonWhitespace(statusString) ? LectureBlockStatus.valueOf(statusString) : null;
	}

	@Override
	public void setStatus(LectureBlockStatus status) {
		statusString = status.name();
	}

	public String getRollCallStatusString() {
		return rollCallStatusString;
	}

	public void setRollCallStatusString(String rollCallStatusString) {
		this.rollCallStatusString = rollCallStatusString;
	}

	@Override
	public LectureRollCallStatus getRollCallStatus() {
		return StringHelper.containsNonWhitespace(rollCallStatusString) ? LectureRollCallStatus.valueOf(rollCallStatusString) : null;
	}

	@Override
	public void setRollCallStatus(LectureRollCallStatus rollCallStatus) {
		rollCallStatusString = rollCallStatus.name();
	}

	public Set<LectureBlockToGroup> getGroups() {
		return groups;
	}

	public void setGroups(Set<LectureBlockToGroup> groups) {
		this.groups = groups;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public Group getTeacherGroup() {
		return teacherGroup;
	}

	public void setTeacherGroup(Group teacherGroup) {
		this.teacherGroup = teacherGroup;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 8963587 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof LectureBlockImpl) {
			LectureBlockImpl block = (LectureBlockImpl)obj;
			return getKey() != null && block.getKey().equals(getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
