package de.unileipzig.xman.illness;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * Description:<br>
 * TODO
 * 
 * <P>
 * Initial Date:  15.05.2008 <br>
 * @author gerb
 */
public interface IllnessReport extends ModifiedInfo, CreateInfo, Persistable {
	
	/**
	 * Adds a illnessReport to the list of those
	 *  
	 * @param illnessReport - the IllnessReport to be added
	 */
	public void addIllnessReportEntry(IllnessReportEntry illnessReportEntry);
	
	/**
	 * Removes a IllnessReport from the list of those
	 * 
	 * @param key - the key of the IllnessReport to be deleted
	 */
	public void removeIllnessReportEntry(Long key);
	
	/**
	 * @return a list of all illnessReports
	 */
	public List<IllnessReportEntry> getIllnessReports();
	
}
