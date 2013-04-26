package de.unileipzig.xman.esf;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.studyPath.StudyPathManager;

/**
 * 
 * @author gerb
 */
public class ElectronicStudentFileManager {

	private static ElectronicStudentFileManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(ElectronicStudentFileManager.class);
	
	private ElectronicStudentFileManager() {
		// singleton
	}
	
	static { INSTANCE = new ElectronicStudentFileManager(); }
	
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
		dbquery.setString("identity", identity.getKey().toString());
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
	public ElectronicStudentFile updateElectronicStundentFile(ElectronicStudentFile esf) {

		DBFactory.getInstance().updateObject(esf);
		return this.retrieveESFByIdentity(esf.getIdentity());
	}
	
	/**
	 * 
	 * @param esf
	 */
	public void removeElectronicStudentFile(ElectronicStudentFile esf) {
		
		if ( esf != null ) {
			
			DBFactory.getInstance().deleteObject(esf);
			log.info("The ESF with the following key was deleted: " + esf.getKey());
		}
		else {
			log.warn("Parameter was null");
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

	/**
	 * 
	 * @return
	 */
	public List<ElectronicStudentFile> retrieveNonValidatedESF() {
		
		String query = "from de.unileipzig.xman.esf.ElectronicStudentFileImpl as esf where esf.validated = :validated";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setBoolean("validated", false);
		return dbquery.list();
	}
	
	/**
	 * 
	 * @return
	 */
	public List<ElectronicStudentFile> retrieveESFByValidation(boolean showValidatedOrNonValidated) {
		
		String query = "from de.unileipzig.xman.esf.ElectronicStudentFileImpl as esf where esf.validated = :validated";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setBoolean("validated", !showValidatedOrNonValidated);
		return dbquery.list();
	}
	
	/**
	 * @param identity - the identity that should be tested
	 * @return true if the ESF for the identity has already been validated 
	 */
	public boolean isESFValidatedForStudent(Identity identity) {
		
		ElectronicStudentFile esf = this.retrieveESFByIdentity(identity);
		if ( esf == null ) return false;
		return esf.getValidated();
	}
	
	public int getNumberOfEsfWithNonDefaultStudyPath() {
		
		int count = 0;
		List<ElectronicStudentFile> esfList = this.retrieveNonValidatedESF();
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
	
}
