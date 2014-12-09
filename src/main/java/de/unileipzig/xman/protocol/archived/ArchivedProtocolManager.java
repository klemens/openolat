package de.unileipzig.xman.protocol.archived;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class ArchivedProtocolManager extends BasicManager {
	@Autowired
	private DB dbInstance;

	private OLog log = Tracing.createLoggerFor(getClass());

	@XStreamAlias("archivedProtocols")
	private static class ArchivedProtocolList {
		@XStreamImplicit(itemFieldName = "protocol")
		public List<ArchivedProtocol> protocols;
	}

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
	 * Returns a list of all ArchivedProtocol
	 */
	public List<ArchivedProtocol> findAll() {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadArchivedProtocols", ArchivedProtocol.class)
			.getResultList();
	}

	public int countAll() {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("countArchivedProtocols", Number.class)
			.getSingleResult()
			.intValue();
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

	/**
	 * Archives the given exam by creating a ArchivedProtocol for each protocol.
	 * This should be used together with closing the exam.
	 * @param exam The exam to archive the protocols of
	 */
	public void archiveProtocols(Exam exam) {
		for(Protocol protocol : ProtocolManager.getInstance().findAllProtocolsByExam(exam)) {
			Appointment appointment = protocol.getAppointment();

			ArchivedProtocol archivedProtocol = new ArchivedProtocol();
			archivedProtocol.setIdentifier(protocol.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
			archivedProtocol.setName(exam.getName());
			archivedProtocol.setDate(appointment.getDate());
			archivedProtocol.setLocation(appointment.getPlace() != null ? appointment.getPlace() : "");
			archivedProtocol.setComment(protocol.getComments() != null ? protocol.getComments() : "");
			archivedProtocol.setResult(protocol.getGrade() != null ? protocol.getGrade() : "");
			archivedProtocol.setStudyPath(protocol.getStudyPath() != null ? protocol.getStudyPath() : "");

			save(archivedProtocol);
		}
	}

	/**
	 * Exports all {@link ArchivedProtocol} as a formatted xml file including a
	 * xml declaration to the given OutputStream.
	 * @return true if the export was successful, false otherwise
	 */
	public boolean exportAll(OutputStream out) {
		ArchivedProtocolList protocols = new ArchivedProtocolList();
		protocols.protocols = findAll();

		try {
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

			getXStream().toXML(protocols, writer);
		} catch(UnsupportedEncodingException e) {
			// this should not happen
			throw new RuntimeException(e);
		} catch(IOException e) {
			log.error("export failed", e);
			return false;
		} catch(XStreamException e) {
			log.error("error while creating xml export", e);
			return false;
		}

		return true;
	}

	/**
	 * Imports all {@link ArchivedProtocol} from the xml file that is read from
	 * the given InputStream.
	 * @see #exportAll(OutputStream)
	 * @return the number of imported archived protocols, -1 on error
	 */
	public int importAll(InputStream in) {
		ArchivedProtocolList protocols;

		try {
			Reader reader = new InputStreamReader(in, "UTF-8");

			protocols = (ArchivedProtocolList) getXStream().fromXML(reader);
		} catch(UnsupportedEncodingException e) {
			// this should not happen
			throw new RuntimeException(e);
		} catch(XStreamException e) {
			log.error("error while reading xml import", e);
			return -1;
		}

		for(ArchivedProtocol protocol : protocols.protocols) {
			save(protocol);
		}

		return protocols.protocols.size();
	}

	/**
	 * This creates an XStream instance which can be used to create
	 * and parse xml files based on {@link ArchivedProtocolList}.
	 */
	private XStream getXStream() {
		XStream xml = new XStream();

		xml.processAnnotations(ArchivedProtocolList.class);
		xml.processAnnotations(ArchivedProtocol.class);

		// use our own formatter for the date, because the actual implementation
		// of the date may change and so would the resulting string which would
		// prevent old xml exports from being imported
		xml.registerConverter(new SingleValueConverter() {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			{ formatter.setTimeZone(TimeZone.getTimeZone("UTC")); }

			@Override
			public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
				return Date.class.isAssignableFrom(type);
			}
			@Override
			public String toString(Object object) {
				return formatter.format((Date) object);
			}
			@Override
			public Object fromString(String string) {
				try {
					return formatter.parse(string);
				} catch(ParseException e) {
					throw new ConversionException("cannot parse '" + string + "' as date", e);
				}
			}
		});
		// remove the class attribute from the date field:
		// this is safe, because we provided our own converter
		xml.aliasSystemAttribute(null, "class");

		return xml;
	}
}
