package de.unileipzig.xman.studyPath;
import java.util.List;
import java.util.Vector;
import java.io.*;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import de.unileipzig.xman.esf.DuplicateObjectException;

public class StudyPathManager {
	
	private static StudyPathManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(this.getClass());
	public static final String DEFAULT_STUDY_PATH = "Keiner";
	private static final String OTHER_STUDY_PATH = "sonstiges";

	private StudyPathManager() {
		// singleton
	}
	
	/**
	 * @return Singleton.
	 * @throws IOException 
	 */
	public static StudyPathManager getInstance(){
		if ( INSTANCE == null ) {
			INSTANCE = new StudyPathManager();
		}
		
		return INSTANCE;
	}
			
	public void createAllStudyPaths(Vector<StudyPath> studyPaths)  {
		List<StudyPath> oldStudyPaths = this.findAllStudyPaths();
		
		for(int i = 0; i < oldStudyPaths.size(); i++){
			DBFactory.getInstance().deleteObject(oldStudyPaths.get(i));
		}
		
		StudyPath defaultStudyPath = this.createStudyPath();
		defaultStudyPath.setName(DEFAULT_STUDY_PATH);
		StudyPath otherStudyPath = this.createStudyPath();
		otherStudyPath.setName(OTHER_STUDY_PATH);
		
		try {
			saveStudyPath(defaultStudyPath);
			saveStudyPath(otherStudyPath);
		} catch (DuplicateObjectException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(int i = 0; i < studyPaths.size(); i++){
			try{
				saveStudyPath(studyPaths.get(i));
			} catch(DuplicateObjectException e1){
				e1.printStackTrace();
			}
		}
	} 
	/**
	 * Creates a new studyPath and returns it.
	 * @return the new created studyPath
	 */
	public StudyPath createStudyPath() {
		return new StudyPathImpl();
	}
	
	/**
	 * Persists the given StudyPath in the database.

	 * @param studyPath - the studyPath, which is to be saved
	 * @throws DuplicateObjectException if a studyPath with the same key was found
	 */
	public void saveStudyPath(StudyPath newStudyPath) throws DuplicateObjectException {
		if ( this.findStudyPath(newStudyPath.getName()) == null) {
			DBFactory.getInstance().saveObject(newStudyPath);
			log.info("New studyPath with id " + newStudyPath.getKey() + " was created.");
		}
		else throw new DuplicateObjectException("There is already a studyPath with the name " + newStudyPath.getName() + " in the database");
		
	}
	
	/**
	 * Deletes the given studyPath.
	 * 
	 * @param studyPath - the studyPath, which is to be deleted
	 */
	public void deleteStudyPath(StudyPath studyPath) {
		if ( studyPath != null ) {
			DBFactory.getInstance().deleteObject(studyPath);
			log.info("The studyPath with id " + studyPath.getKey() + " was deleted.");
		}
	}
	
	/**
	 * @param name - a string representing the name of the studyPath
	 * @return the studyPath with the given name or null, if no studyPaths were found
	 */
	public StudyPath findStudyPath(String name) {
		String query = "from de.unileipzig.xman.studyPath.StudyPathImpl as spi where spi.name = '" + name + "'";
		List<StudyPath> studyPathList = DBFactory.getInstance().find(query);
		if ( studyPathList.size() == 1 ) return (StudyPath) studyPathList.get(0);
		else {
			log.info("No studyPath with name " + name + " could be found!");
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<StudyPath> findAllStudyPaths() {
		List<StudyPath> res = (List<StudyPath>) DBFactory.getInstance().find("from de.unileipzig.xman.studyPath.StudyPathImpl");
		return res;
	}
	
	/**
	 * 
	 * @return An array of all Studypaths
	 */
	public String[] getAllStudyPathsAsString() {
		List<String> res = (List<String>) DBFactory.getInstance().find("select name from de.unileipzig.xman.studyPath.StudyPathImpl");
		
		String[] keys = new String[res.size()];
		for (int i = 0; i < res.size(); i++) keys[i] = res.get(i);
		
		return keys;
	}
}