package de.unileipzig.xman.illness;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Description:<br>
 * This class represents a set of IllnessReports. Only a member of 
 * the exam office should add some entries here. 
 * 
 * <P>
 * Initial Date:  15.05.2008 <br>
 * @author gerb
 */
public class IllnessReportImpl extends PersistentObject implements IllnessReport {

	private List<IllnessReportEntry> illnessReports;
	private Date lastModified;
	
	IllnessReportImpl() {
		
		this.illnessReports = new ArrayList<IllnessReportEntry>();
	}

	/**
	 * @see de.xman.illness.IllnessReport#addIllnessReportEntry(de.xman.illness.IllnessReportEntry)
	 */
	public void addIllnessReportEntry(IllnessReportEntry illnessReportEntry) {
		
		this.illnessReports.add(illnessReportEntry);
	}

	/**
	 * @see de.xman.illness.IllnessReport#removeIllnessReportEntry(java.lang.Long)
	 */
	public void removeIllnessReportEntry(Long key) {
		
		IllnessReportEntry report = IllnessReportManager.getInstance().retrieveIllnessReportEntryByKey(key);
		this.illnessReports.remove(report);
	}
	
	/**
	 * @return the list if IllnessReports
	 */
	public List<IllnessReportEntry> getIllnessReports() {
		
		return illnessReports;
	}

	/**
	 * @param illnessReports - the IllnessReports to set
	 */
	public void setIllnessReports(List<IllnessReportEntry> illnessReports) {
		
		this.illnessReports = illnessReports;
	}
	
	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
}
