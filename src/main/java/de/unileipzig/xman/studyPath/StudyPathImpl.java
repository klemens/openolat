package de.unileipzig.xman.studyPath;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

public class StudyPathImpl extends PersistentObject implements StudyPath {

	private String i18nKey;
	private Date lastModified;
	
	public StudyPathImpl() {
		
		// nothing to do here
	}
	
	/**
	 * @see de.xman.studyPath.StudyPath#getI18nKey()
	 */
	public String getI18nKey() {
		
		return this.i18nKey;
	}

	/**
	 * @see de.xman.studyPath.StudyPath#setI18nKey()
	 */
	public void setI18nKey(String i18nKey) {
		
		this.i18nKey = i18nKey;
	}
	
	/**
	 * @see org.olat.core.id.OLATResourceablegetId()
	 */
	public Long getResourceableId() {
		
		return this.getKey();
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetTypeName()
	 */
	public String getResourceableTypeName() {
		
		return StudyPath.ORES_TYPE_NAME;
	}
	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}

}
