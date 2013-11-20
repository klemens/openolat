package de.unileipzig.xman.protocol.archived;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ArchivedProtocolManager extends BasicManager {
	@Autowired
	private DB dbInstance;

	public static ArchivedProtocolManager getInstance() {
		return CoreSpringFactory.getImpl(ArchivedProtocolManager.class);
	}

	/**
	 * Persists the given ArchivedProtocol in the database
	 * @throws EntityExistsException when the entry is already persisted
	 */
	public void save(ArchivedProtocol protocol) {
		dbInstance.getCurrentEntityManager().persist(protocol);
	}

	/**
	 * Deletes the given ArchivedProtocol from the database
	 * @param protocol
	 */
	public void delete(ArchivedProtocol protocol) {
		dbInstance.getCurrentEntityManager().remove(protocol);
	}

	/**
	 * Updates the entity from the database.
	 * Use this if you want to use an object from a earlier session
	 */
	public void reload(ArchivedProtocol protocol) {
		dbInstance.getCurrentEntityManager().refresh(protocol);
	}

	/**
	 * Returns a list of all ArchivedProtocol of the given student
	 * @param studentId The student's number (matrikel)
	 */
	public List<ArchivedProtocol> findAllByStudent(String studentId) {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadArchivedProtocolsByStudent", ArchivedProtocol.class)
			.setParameter("studentId", studentId)
			.getResultList();
	}

	/**
	 * Deletes all ArchivedProtocol of the given student
	 * @param studentId The student's number (matrikel)
	 * @return The number of deleted entities
	 */
	public int deleteAllByStudent(String studentId) {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("deleteArchivedProtocolsByStudent")
			.setParameter("studentId", studentId)
			.executeUpdate();
	}
}
