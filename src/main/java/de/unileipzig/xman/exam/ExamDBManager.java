package de.unileipzig.xman.exam;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.exam.Exam;

public class ExamDBManager {
	
	private static ExamDBManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(getClass());

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

	public void saveExam(Exam exam) {
		DBFactory.getInstance().saveObject(exam);
		log.info("New exam '" + exam.getName() + "' created");
	}

	/**
	 * Updates an existing exam.
	 * @param exam - the exam, which is to be updated
	 */
	public void updateExam(Exam exam) {
		
		DBFactory.getInstance().updateObject(exam);
		log.info("Exam with the id: " + exam.getKey() + "was updated");
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
			log.info("The exam with id " + deleteExam.getKey() + " was deleted.");
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
			log.info("No exam with id " + id + " could be found!");
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
		OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), exam.getResourceableTypeName());
		return RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
	}
	
	/**
	 * @param exam
	 * @return the RepositoryEntry key of the given exam
	 */
	public Long findRepositoryEntryKey(Exam exam) {
		OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), exam.getResourceableTypeName());
		return RepositoryManager.getInstance().lookupRepositoryEntryKey(ores, true);
	}

	/**
	 * @param exam
	 * @return true if exam is closed 
	 */
	public boolean isClosed(Exam exam){
		return RepositoryManager.getInstance().createRepositoryEntryStatus(findRepositoryEntryOfExam(exam).getStatusCode()).isClosed();
	}
	
	/**
	 * @param exam
	 * @return true if exam is closed 
	 */
	public void close(Exam exam){
		findRepositoryEntryOfExam(exam).setStatusCode(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED);
	}

	/**
	 * Checks if the subscription period is active
	 */
	public boolean canSubscribe(Exam exam) {
		Date now = new Date();
		exam = findExamByID(exam.getKey());
		return now.after(exam.getRegStartDate()) && now.before(exam.getRegEndDate());
	}

	/**
	 * Checks if the unsubscription period is active
	 */
	public boolean canUnsubscribe(Exam exam) {
		Date now = new Date();
		exam = findExamByID(exam.getKey());
		return now.after(exam.getRegStartDate()) && now.before(exam.getSignOffDate());
	}
}