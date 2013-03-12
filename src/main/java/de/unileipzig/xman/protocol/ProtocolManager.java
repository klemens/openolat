package de.unileipzig.xman.protocol;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;

public class ProtocolManager {
	
	/**
	 * for singleton
	 */
	private static ProtocolManager INSTANCE = new ProtocolManager();
	
	/**
	 * for singleton
	 */
	private ProtocolManager() {
		// singleton
	}
	
	/**
	 * for singleton
	 * @return the one and only instance of this manager
	 */
	public static ProtocolManager getInstance() {
		return INSTANCE;
	}
	
	/* ---------------- protocol ------------------- */
	
	/**
	 * creates a new Protocol
	 * @return a new Protocol
	 */
	public Protocol createProtocol() {
		
		return new ProtocolImpl();
	}
	
	/**
	 * persists a given protocol
	 * @param proto, which is to be persisted
	 */
	public void saveProtocol(Protocol proto) {
		
		DBFactory.getInstance().saveObject(proto);
		Tracing.createLoggerFor(ProtocolManager.class).info("New protocol with the id " + proto.getKey() + " for the identity " + proto.getIdentity().getName() +  " key: " +proto.getIdentity().getKey() +" was created.");
	}
	
	/**
	 * Updates an existing proto
	 * @param app - the appointment, which is to be updated
	 */
	public void updateProtocol(Protocol proto) {

		DBFactory.getInstance().updateObject(proto);
	}
	
	/**
	 * 
	 * @param exam
	 * @return
	 */
	public List<Protocol> findAllProtocolsByExam(Exam exam) {
		
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.exam = " + exam.getKey();
		return this.findAllProtocolsByQuery(query);
	}
	
	/**
	 * 
	 * @param exam
	 * @param earmarked
	 * @return
	 */
	public List<Protocol> findAllProtocolsByExam(Exam exam, boolean earmarked) {
		
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.exam = " + exam.getKey() + " and proto.earmarked = " + earmarked;
		return this.findAllProtocolsByQuery(query);
	}
	
	/**
	 * 
	 * @param app
	 * @return
	 */
	public List<Protocol> findAllProtocolsByAppointment(Appointment app) {
		
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.appointment = " + app.getKey();
		return this.findAllProtocolsByQuery(query);
	}
	
	/**
	 * finds all protocols by a given examId
	 * @param examId the id of the exam
	 * @return the list of protocols which belong to the examId
	 */
	private List<Protocol> findAllProtocolsByQuery(String query) {
		
		List<Protocol> protocols = new Vector<Protocol>();
		List protoList = DBFactory.getInstance().find(query);
		for( Object o : protoList ) {
			Protocol prot = (Protocol) o;
			protocols.add(prot);
			
		}
		return protocols;
	}
	
	/**
	 * finds all protocols by a given identity
	 * @param identity - identity of the user
	 * @return the list of protocols which belong to the given identity
	 */
	public List<Protocol> findAllProtocolsByIdentity(Identity identity) {
		
		List<Protocol> protocols = new Vector<Protocol>();
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.identity = :identity";
		
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setEntity("identity", identity);
		List searchList = dbquery.list();
		
		for( Object o : searchList ) {
			Protocol prot = (Protocol) o;
			protocols.add(prot);
			
		}
		return protocols;
		
	}
	
	/**
	 * finds the protocol by a given exam and an identity
	 * @param identity - the identity of the user
	 * @param exam the id of the exam
	 * @return the list of protocols which belong to the exam and the identity
	 */
	public Protocol findProtocolByIdentityAndExam(Identity identity, Exam exam) {
		
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.identity = " + identity.getKey() + " and proto.exam = " + exam.getKey();
		List protoList = DBFactory.getInstance().find(query);
		if ( protoList.size() != 0 ) return (Protocol)protoList.get(0);
		else return null;
	}
	
	/**
	 * Removes the given protocol by deleting it from the database.
	 * Don't forget to update the appointment, if it is a oral one. 
	 * @param proto - the protocol to be deleted
	 */
	public void deleteProtocol(Protocol proto) {
		
		if ( proto != null ) DBFactory.getInstance().deleteObject(proto);
	}
	

	/**
	 * changes the earmarked status of a protocol loaded from the db
	 * @param protoId the id of the protocol
	 * @param earmarked the required status of earmarked
	 */
	public void setProtocolEarmarkedFeature(Long protoId, boolean earmarked) {
		
		String protoQuery = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.key = " + protoId;
		List searchList = DBFactory.getInstance().find(protoQuery);
		DBFactory.getInstance().commit();
		if ( searchList.size() == 1 ) {
			((Protocol) searchList.get(0)).setEarmarked(earmarked);
			ProtocolManager.getInstance().updateProtocol(((Protocol) searchList.get(0)));
		}
	}

}
