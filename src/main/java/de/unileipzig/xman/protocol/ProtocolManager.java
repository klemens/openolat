package de.unileipzig.xman.protocol;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DBFactory;
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
		return DBFactory.getInstance().getCurrentEntityManager().find(ProtocolImpl.class, id);
	}
	
	/**
	 * 
	 * @param identity
	 * @param app
	 * @return true if there is a protocol with the given identity/exam combination, false else.
	 */
	public boolean isIdentitySubscribedToExam(Identity identity, Exam exam) {
		return !findAllProtocolsByIdentityAndExam(identity, exam).isEmpty();
	}

	public List<Protocol> findAllProtocolsByExam(Exam exam) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.exam = :exam";
		return DBFactory.getInstance().getCurrentEntityManager()
			.createQuery(query, Protocol.class)
			.setParameter("exam", exam)
			.getResultList();
	}

	public List<Protocol> findAllProtocolsByAppointment(Appointment app) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.appointment = :appointment";
		return DBFactory.getInstance().getCurrentEntityManager()
			.createQuery(query, Protocol.class)
			.setParameter("appointment", app)
			.getResultList();
	}
	
	/**
	 * 
	 * @param app
	 * @return
	 */
	public List<Protocol> findAllProtocolsByIdentityAndExam(Identity identity, Exam exam) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.identity = :identity and proto.exam = :exam";
		return DBFactory.getInstance().getCurrentEntityManager()
			.createQuery(query, Protocol.class)
			.setParameter("identity", identity)
			.setParameter("exam", exam)
			.getResultList();
	}
	
	/**
	 * finds the protocol by a given exam and an identity
	 * @param identity - the identity of the user
	 * @param exam the id of the exam
	 * @return the list of protocols which belong to the exam and the identity
	 */
	public Protocol findProtocolByIdentityAndExam(Identity identity, Exam exam) {
		// TODO: only used in unused calendar, remove?
		return findAllProtocolsByIdentityAndExam(identity, exam)
			.stream()
			.findFirst()
			.orElse(null);
	}
	
	
	public Protocol findProtocolByIdentityAndAppointment(Identity identity, Appointment app) {
		String query = "from de.unileipzig.xman.protocol.ProtocolImpl as proto where proto.identity = :identity and proto.appointment = :appointment";
		return DBFactory.getInstance().getCurrentEntityManager()
			.createQuery(query, Protocol.class)
			.setParameter("identity", identity)
			.setParameter("appointment", app)
			.getSingleResult();
	}
	
	/**
	 * Removes the given protocol by deleting it from the database.
	 * Don't forget to update the appointment, if it is a oral one. 
	 * @param proto - the protocol to be deleted
	 */
	public void deleteProtocol(Protocol proto) {
		
		if ( proto != null ) DBFactory.getInstance().deleteObject(proto);
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
