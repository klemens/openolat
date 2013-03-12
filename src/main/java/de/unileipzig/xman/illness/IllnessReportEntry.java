package de.unileipzig.xman.illness;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Description:<br>
 * The Dates from when to when the illnessReport reaches.
 * 
 * <P>
 * Initial Date:  15.05.2008 <br>
 * @author gerb
 */
public class IllnessReportEntry extends PersistentObject implements ModifiedInfo {

	private Date fromDate;
	private Date toDate;
	private Date lastModified;
	
	IllnessReportEntry() {
		
		this.fromDate = null;
		this.toDate = null;
	}
	
	/**
	 * @return the date to which the illnessreport dates
	 */
	public Date getToDate(){
		
		return this.toDate;
	}
	
	/**
	 * @return the date from which the illnessreport started
	 */
	public Date getFromDate(){
		
		return this.fromDate;
	}
	
	/**
	 * @param toDate - the date to which the illnessreport dates
	 */
	public void setToDate(Date toDate) {
		
		this.toDate = toDate;
	}
	
	/**
	 * @param fromDate - the date from which the illnessreport started
	 */
	public void setFromDate(Date fromDate) {
		
		this.fromDate = fromDate;
	}
	
	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
}
