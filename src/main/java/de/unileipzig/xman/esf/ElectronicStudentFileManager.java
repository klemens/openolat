package de.unileipzig.xman.esf;

import java.util.List;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserDataDeletable;

import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.studyPath.StudyPathManager;

/**
 * 
 * @author gerb
 */
public class ElectronicStudentFileManager implements UserDataDeletable {

	private static ElectronicStudentFileManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(ElectronicStudentFileManager.class);
	
	/**
	 * [spring]
	 * @param userDeletionManager
	 */
	private ElectronicStudentFileManager(UserDeletionManager userDeletionManager) {
		INSTANCE = this;
	}
	
	/**
	 * @return Singleton.
	 */
	public static ElectronicStudentFileManager getInstance() { return INSTANCE; }
	
	/**
	 * 
	 * @return
	 */
	public ElectronicStudentFile createElectronicStudentFileForStudent(Identity identity){
		
		return new ElectronicStudentFileImpl(identity);
	}
	
	/**
	 * Persists a given ElectronicStudentFile in the local database.
	 * If there is already a ESF for this student, an DuplicateObjectException is thrown.
	 * 
	 * @param esf - the ElectronicStudentFile to be persisted
	 * @throws DuplicateObjectException if there is already an ESF for this student in the database
	 */
	public void persistElectronicStudentFile(ElectronicStudentFile esf) throws DuplicateObjectException {
		
		ElectronicStudentFile fileFromDB = this.retrieveESFByIdentity(esf.getIdentity());
		
		if ( fileFromDB == null ) {
			
			DBFactory.getInstance().saveObject(esf);
			log.info("A new ElectronicStudentFile with the id: " + esf.getKey() + " was persisted.");
			
			OLATResourceManager rm = OLATResourceManager.getInstance();
			OLATResource ores = rm.createOLATResourceInstance(esf);
			rm.saveOLATResource(ores);
		}
		else throw new DuplicateObjectException("There is already a ElectronicStudentFile for the student: " + esf.getIdentity().getName() + " in the local database.");		
	}
	
	/**
	 * 
	 * @param identity
	 * @return
	 */
	public ElectronicStudentFile retrieveESFByIdentity(Identity identity){
		
		String query = "from de.unileipzig.xman.esf.ElectronicStudentFileImpl as esf where esf.identity = :identity";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setLong("identity", identity.getKey());
		List<Object> esfList = dbquery.list();
		if ( esfList.size() > 0 ) {
			
			return (ElectronicStudentFile)esfList.get(0);
		}
		else {
			log.warn("No ElectronicStudentFile for the given identity: " + identity.getKey() + " could be found!");
			return null;
		}		
	}
	
	/**
	 * 
	 * @param esf
	 * @return
	 */
	public void updateElectronicStundentFile(ElectronicStudentFile esf) {

		DBFactory.getInstance().updateObject(esf);
	}
	
	/**
	 * Removes the esf and all its protocols and makes the oral appointments
	 * available again
	 */
	public void removeElectronicStudentFile(ElectronicStudentFile esf) {
		if(esf != null ) {
			for(Protocol protocol : esf.getProtocolList()) {
				// make appointments available again
				if(protocol.getExam().getIsOral()) {
					protocol.getAppointment().setOccupied(false);
				}
				
				// delete the protocol
				DBFactory.getInstance().deleteObject(protocol);
			}
			
			// delete the esf
			DBFactory.getInstance().deleteObject(esf);
			
			// commit changes
			DBFactory.getInstance().intermediateCommit();
			
			log.info("The ESF with the following key was deleted: " + esf.getKey());
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<ElectronicStudentFile> retrieveAllElectronicStudentFiles() {
		
		String query = "from de.unileipzig.xman.esf.ElectronicStudentFileImpl";
		return DBFactory.getInstance().createQuery(query).list();
	}
		
	public int getNumberOfEsfWithNonDefaultStudyPath() {
		
		int count = 0;
		List<ElectronicStudentFile> esfList = this.retrieveAllElectronicStudentFiles();
		for ( ElectronicStudentFile esf : esfList ) {
			
			if ( !esf.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, null).equals(StudyPathManager.DEFAULT_STUDY_PATH)) {
				
				count++;
			}
		}
		return count;
	}
	
    public boolean isMatrikelNumberAvailable(Integer matrikel) {
    
    	List<ElectronicStudentFile> esfList = this.retrieveAllElectronicStudentFiles();
    	
    	for ( ElectronicStudentFile esf : esfList ) {
    		
    		String matrikelNr = esf.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
    		if ( matrikelNr.equals( matrikel.toString() ) ) {
    			
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * This method is called when a user is deleted and then deletes its esf
     */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		ElectronicStudentFile esf = retrieveESFByIdentity(identity);
		if(esf != null) {
			removeElectronicStudentFile(esf);
		}
	}
}
