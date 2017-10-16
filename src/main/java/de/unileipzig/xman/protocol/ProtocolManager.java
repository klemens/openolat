package de.unileipzig.xman.protocol;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.opensaml.artifact.InvalidArgumentException;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;

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
	 * finds an Protocol by the given id
	 * @param id the id of the Protocol
	 * @return the specified Protocol or null if there is no Protocol with this id
	 */
	public Protocol findProtocolByID(Long id) {
		return DBFactory.getInstance().loadObject(ProtocolImpl.class, id);
	}
	
	/**
	 * 
	 * @param identity
	 * @param app
	 * @return true if there is a protocol with the given identity/appointment combination, false else.
	 */
	public boolean isIdentitySubscribedToAppointment(Identity identity, Appointment appointment) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.appointment = " + appointment.getKey() + " and proto.identity = " + identity.getKey();
		return !findAllProtocolsByQuery(query).isEmpty();
	}
	
	/**
	 * 
	 * @param identity
	 * @param app
	 * @return true if there is a protocol with the given identity/exam combination, false else.
	 */
	public boolean isIdentitySubscribedToExam(Identity identity, Exam exam) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.exam = " + exam.getKey() + " and proto.identity = " + identity.getKey();
		return !findAllProtocolsByQuery(query).isEmpty();
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
	 * 
	 * @param app
	 * @return
	 */
	public List<Protocol> findAllProtocolsByIdentityAndExam(Identity identity, Exam exam) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.exam = " + exam.getKey() + " and proto.identity = " + identity.getKey();
		return findAllProtocolsByQuery(query);
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
	
	
	public Protocol findProtocolByIdentityAndAppointment(Identity identity, Appointment app) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.identity = " + identity.getKey() + " and proto.appointment = " + app.getKey();
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

	private Protocol createNewProtocol(Appointment appointment, ElectronicStudentFile esf, boolean earmark, String comment) {
		if(appointment.getOccupied()) {
			throw new InvalidArgumentException("appointment is already occupied");
		}

		Protocol proto = ProtocolManager.getInstance().createProtocol();
		proto.setIdentity(esf.getIdentity());
		proto.setEarmarked(earmark);
		proto.setExam(appointment.getExam());
		proto.setComments(comment);
		// we save the studypath in the protocol, because it may change at any time
		proto.setStudyPath(esf.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, null));
		proto.setAppointment(appointment);

		ProtocolManager.getInstance().saveProtocol(proto);
		esf.addProtocol(proto);

		if(appointment.getExam().getIsOral()) {
			appointment.setOccupied(true);
		}

		return proto;
	}

	/**
	 * Register student for given appointment and send confirmation mail
	 *
	 * @param appointment The appointment to register the student for
	 * @param esf The electronic student file of the student
	 * @param comment The comment for the protocol, can be an empty string
	 */
	public void registerStudent(Appointment appointment, ElectronicStudentFile esf, String comment) {
		Protocol proto = createNewProtocol(appointment, esf, false, comment);

		BusinessControlFactory bcf = BusinessControlFactory.getInstance();
		Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(esf.getIdentity().getUser().getPreferences().getLanguage()));
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale());

		// {0}: exam name, {1} exam link, {2}: date, {3}: place, {4}: duration, {5}: exam type (adjective)
		String[] params = new String[] {
			proto.getExam().getName(),
			bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(proto.getExam())), true),
			dateFormat.format(appointment.getDate()),
			appointment.getPlace(),
			String.valueOf(appointment.getDuration()),
			userTranslator.translate(proto.getExam().getIsOral() ? "oral2" : "written2"),
		};

		String subject = userTranslator.translate("Mail.register.subject", params);
		String body = userTranslator.translate("Mail.register.body", params);
		MailManager.getInstance().sendEmail(subject, body, esf.getIdentity());
	}

	/**
	 * Earmark student for given appointment, send confirmation mail, and add esf comment
	 *
	 * @param appointment The appointment to earmark the student for
	 * @param esf The electronic student file of the student
	 * @param comment The comment for the protocol, can be an empty string
	 */
	public void earmarkStudent(Appointment appointment, ElectronicStudentFile esf, String comment) {
		Protocol proto = createNewProtocol(appointment, esf, true, comment);

		BusinessControlFactory bcf = BusinessControlFactory.getInstance();
		Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(esf.getIdentity().getUser().getPreferences().getLanguage()));
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale());

		// {0}: exam name, {1} exam link, {2}: date, {3}: place, {4}: duration, {5}: exam type (adjective)
		String[] params = new String[] {
			proto.getExam().getName(),
			bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(proto.getExam())), true),
			dateFormat.format(appointment.getDate()),
			appointment.getPlace(),
			String.valueOf(appointment.getDuration()),
			userTranslator.translate(proto.getExam().getIsOral() ? "oral2" : "written2"),
		};

		String subject = userTranslator.translate("Mail.earmark.subject", params);
		String body = userTranslator.translate("Mail.earmark.body", params);
		MailManager.getInstance().sendEmail(subject, body, esf.getIdentity());

		String esfComment = userTranslator.translate("Mail.earmark.comment", params);
		CommentManager.getInstance().createCommentInEsf(esf, esfComment, esf.getIdentity());
	}
}
