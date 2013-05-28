package de.unileipzig.xman.exam;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.exam.Exam;

public class ExamDBManager {
	
	private static ExamDBManager INSTANCE = null;

	private ExamDBManager() {
		// singleton
	}
	
	static { INSTANCE = new ExamDBManager(); }
	
	/**
	 * @return Singleton.
	 */
	public static ExamDBManager getInstance() { return INSTANCE; }
	
	/**
	 * Creates a new exam and returns it.
	 * @return the new created exam
	 */
	public Exam createExam() {
		
		return new ExamImpl();
	}
	
	/**
	 * Persists the given exam in the database.
	 * It also creates a OLATResource belonging to the exam.
	 * @param exam - the exam, which is to be saved
	 */
	public void saveExam(Exam newExam) {
		
		newExam.setLastModified(new Date());
		DBFactory.getInstance().saveObject(newExam);
		Tracing.createLoggerFor(ExamDBManager.class).info("New exam with id " + newExam.getKey() + " was created.");
		
		//newExam = (Exam)DBFactory.getInstance().loadObject(newExam);
				
		OLATResourceManager rm = OLATResourceManager.getInstance();
		OLATResource ores = rm.createOLATResourceInstance(newExam);
		rm.saveOLATResource(ores);
	}
	
	/**
	 * Updates an existing exam.
	 * @param exam - the exam, which is to be updated
	 */
	public void updateExam(Exam exam) {
		
		DBFactory.getInstance().updateObject(exam);
		Tracing.createLoggerFor(ExamDBManager.class).info("Exam with the id: " + exam.getKey() + "was updated");
	}
	
	/**
	 * Deletes the given exam.
	 * Don't forget to delete also all other related objects (like appointments, calendar events etc.).
	 * It does not delete the belonging OLATResource. A proper way to delete exams is given in ExamHandler.cleanUpOnDelete()
	 * @param deleteExam - the exam, which is to be deleted
	 */
	public void deleteExam(Exam deleteExam) {
		
		if ( deleteExam != null ) {
			
			DBFactory.getInstance().deleteObject(deleteExam);
			Tracing.createLoggerFor(ExamDBManager.class).info("The exam with id " + deleteExam.getKey() + " was deleted.");
		}
		
	}
	
	/**
	 * Deletes the exam given by the ID.
	 * Don't forget to delete also all other related objects (like appointments, calendar events etc.).
	 * It does not delete the belonging OLATResource.
	 * @param id - a Long object representing the id of the exam
	 */
	public void deleteExamByID(Long id) {
		
		Exam deleteExam = this.findExamByID(id);
		this.deleteExam(deleteExam);
	}
	
	/**
	 * @param id - a Long object representing the id of the exam
	 * @return the exam with the given id or null, if no exam was found
	 */
	public Exam findExamByID(Long id) {
		
		String query = "from de.unileipzig.xman.exam.ExamImpl as b where b.key = " + id;
		List examList = DBFactory.getInstance().find(query);
		if ( examList.size() > 0 ) return (Exam)examList.get(0);
		else {
			Tracing.createLoggerFor(ExamDBManager.class).info("No exam with id " + id + " could be found!");
			return null;
		}
	}
	
	/**
	 * @param exam
	 * @return the name of the given exam
	 */
	public String getExamName(Exam exam) {
		
		String query = "from org.olat.repository.RepositoryEntry as rep where rep.olatResource = "
			+ "(select key from org.olat.resource.OLATResourceImpl as res where res.resId = " + exam.getKey() + ")";
		List<RepositoryEntry> list = DBFactory.getInstance().find(query);
		if ( list.size() == 1 ) return list.get(0).getDisplayname();
		return "n/a";
	}
	
	/**
	 * @param exam
	 * @return the RepositoryEntry of the given exam 
	 */
	public RepositoryEntry findRepositoryEntryOfExam(Exam exam) {
		
		String query = "from org.olat.repository.RepositoryEntry as rep where rep.olatResource = "
			+ "(select key from org.olat.resource.OLATResourceImpl as res where res.resId = " + exam.getKey() + ")";
		List<RepositoryEntry> list = DBFactory.getInstance().find(query);
		if ( list.size() == 1 ) return list.get(0);
		return null;
	}
	
	/**
	 * @param exam
	 * @return true if exam is closed 
	 */
	public boolean isClosed(Exam exam){
		return findRepositoryEntryOfExam(exam).getStatusCode() == RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED;
	}
	
	/**
	 * @param exam
	 * @return true if exam is closed 
	 */
	public void close(Exam exam){
		findRepositoryEntryOfExam(exam).setStatusCode(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED);
	}
}