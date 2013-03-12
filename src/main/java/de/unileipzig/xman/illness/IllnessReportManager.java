package de.unileipzig.xman.illness;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

public class IllnessReportManager {

	private static IllnessReportManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(IllnessReportManager.class);
	
	/**
	 * private because of singleton
	 */
	private IllnessReportManager() {}
	
	static { INSTANCE = new IllnessReportManager(); }
	
	/**
	 * @return Singleton.
	 */
	public static IllnessReportManager getInstance() { return INSTANCE; }

	/**
	 * @return a new IllnessReportEntry object
	 */
	public IllnessReportEntry createIllnessReportEntry() {
		
		return new IllnessReportEntry();
	}
	
	/**
	 * @return a new IllnessReport object
	 */
	public IllnessReport createIllnessReport() {
		
		return new IllnessReportImpl();
	}
	
	/**
	 * persists a given IllnessReportEntry in the database 
	 * @param the IllnessReportEntry to be persisted
	 */
	public void persistIllnessReportEntry(IllnessReportEntry entry) {
		
		DBFactory.getInstance().saveObject(entry);
		log.info("A new IllnessReportEntry with the key: " + entry.getKey() + " was persisted");
	}
	
	/**
	 * persists a given IllnessReport in the database
	 * @param IllnessReport to be persisted
	 */
	public void persistIllnessReport(IllnessReport illnessReport) {
		
		DBFactory.getInstance().saveObject(illnessReport);
		log.info("A new IllnessReport with the key: " + illnessReport.getKey() + " was persisted");
	}
	
	/**
	 * retrieves a IllnessReportEntry, identified by the given key
	 * 
	 * @param key - the key of the IllnessReportEntry
	 * @return the IllnessReportEntry or null if none was found
	 */
	public IllnessReportEntry retrieveIllnessReportEntryByKey(Long key) {
		
		String query = "from de.unileipzig.xman.illness.IllnessReportEntry as ire where ire.key = :key";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setString("key", key.toString());
		List<Object> illnessReportEntryList = dbquery.list();
		if ( illnessReportEntryList.size() > 0 ) return (IllnessReportEntry) illnessReportEntryList.get(0);
		else {
			log.warn("No IllnessReportEntry for the given key: " + key.toString() + " could be found");
			return null;
		}
	}
	
	/**
	 * retrieves a IllnessReport, identified by the given key
	 * 
	 * @param key - the key of the IllnessReport
	 * @return the IllnessReport or null if none was found
	 */
	public IllnessReport retrieveIllnessReportByKey(Long key) {
	
		String query = "from de.unileipzig.xman.illness.IllnessReport as ir where ir.key = :key";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setString("key", key.toString());
		List<Object> illnessReportList = dbquery.list();
		if ( illnessReportList.size() > 0 ) return (IllnessReport) illnessReportList.get(0);
		else {
			log.warn("No IllnessReport for the given key: " + key.toString() + " could be found");
			return null;
		}
	}
	
	/**
 * Removes the IllnessReportEntry identified by the given key from the local database.
	 * 
	 * @param key - The key of the IllnessReportEntry to be deleted
	 */
	public void removeIllnessReportEntryByKey(Long key) {
		
		IllnessReportEntry entry = this.retrieveIllnessReportEntryByKey(key);
		if ( entry != null ) {
			
			long entryKey = entry.getKey();
			DBFactory.getInstance().deleteObject(entry);
			log.info("The IllnessReportEntry with the key: " + entryKey + " was deleted!");
		}
		else {
			log.warn("The IllnessReportEntry with the key: " + key + " could not be deleted, cause there was no such IllnessReportEntry in the local database");
		}
	}
	
	/**
	 * Removes the IllnessReport identified by the given key from the local database.
	 * 
	 * @param key - The key of the IllnessReport to be deleted
	 */
	public void removeIllnessReportByKey(Long key){
		
		IllnessReport illnessReport = this.retrieveIllnessReportByKey(key);
		if ( illnessReport != null ) {
			
			long entryKey = illnessReport.getKey();
			DBFactory.getInstance().deleteObject(illnessReport);
			log.info("The IllnessReport with the key: " + entryKey + " was deleted!");
		}
		else {
			log.warn("The IllnessReport with the key: " + key + " could not be deleted, cause there was no such IllnessReport in the local database");
		}
	}
	
	/**
	 * Updates the given IllnessReportEntry in the local database
	 * 
	 * @param illnessReportEntry - the IllnessReportEntry to be updated
	 */
	public void updateIllnessReportEntry(IllnessReportEntry illnessReportEntry) {
		
		illnessReportEntry.setLastModified(new Date());
		DBFactory.getInstance().updateObject(illnessReportEntry);
	}
	
	/**
	 * Updates the given IllnessReport in the local database
	 * 
	 * @param illnessReport - to be updated
	 */
	public void updateIllnessReport(IllnessReport illnessReport) {
		
		illnessReport.setLastModified(new Date());
		DBFactory.getInstance().updateObject(illnessReport);
	}
}
