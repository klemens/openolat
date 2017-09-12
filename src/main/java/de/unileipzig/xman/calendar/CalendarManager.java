package de.unileipzig.xman.calendar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.impl.NekoHTMLFilter;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;

/**
 * 
 * @author gerb
 *
 */
public class CalendarManager {
	
	/**
	 * for singleton
	 */
	private static CalendarManager INSTANCE = new CalendarManager();

	/**
	 * for singleton
	 */
	private CalendarManager() {
		// singleton
	}

	/**
	 * for singleton
	 * 
	 * @return the one and only instance of this manager
	 */
	public static CalendarManager getInstance() {
		return INSTANCE;
	}

	/**
	 * creates a calendar event for a student which has subscribed to an exam
	 * @param exam the exam 
	 * @param identity the identity of the user
	 * @param res the OLATResourceable
	 */
	public void createKalendarEventForExam(Exam exam, Identity identity, OLATResourceable res) {
		org.olat.commons.calendar.CalendarManager calManager = CoreSpringFactory.getImpl(org.olat.commons.calendar.CalendarManager.class);
		
		Protocol proto = ProtocolManager.getInstance().findProtocolByIdentityAndExam(identity, exam);
		
		if ( proto.getAppointment() != null ) {
		
			KalendarEvent kEvent = new KalendarEvent(exam.getKey().toString(), 
					ExamDBManager.getInstance().getExamName(exam), proto.getAppointment().getDate(), proto.getAppointment().getDuration());
			
			kEvent.setLocation(proto.getAppointment().getPlace());
			kEvent.setEnd(new Date(proto.getAppointment().getDate().getTime() + (proto.getAppointment().getDuration() * 60 * 1000)));
			
			Kalendar kal = calManager.getPersonalCalendar(identity).getKalendar();
			
			calManager.addEventTo(kal, kEvent);
				
			// TODO TESTCASE: CalendarManagerFactory.getInstance().getCalendarManager().persistCalendar(kal);
		}
	}
	
	/**
	 * deletes the kalendar event if the user has unsubscribed from an exam
	 * @param Exam - the exam
	 * @param Identity - the identity of the user
	 */
	public void deleteKalendarEventForExam(Exam exam, Identity identity){
		org.olat.commons.calendar.CalendarManager calManager = CoreSpringFactory.getImpl(org.olat.commons.calendar.CalendarManager.class);
		
		Kalendar kal = calManager.getPersonalCalendar(identity).getKalendar();
		if ( kal != null) {

			Object[] tmpArray = kal.getEvents().toArray();
			
			if ( tmpArray != null ) {
				for ( int i = 0; i < tmpArray.length ; i++ ) {
					if ( ((KalendarEvent)tmpArray[i]).getID().equals(exam.getKey().toString()) ) {
						
						// TODO Testcase
						calManager.removeEventFrom(kal, (KalendarEvent)tmpArray[i]);
					}
				}
			}
		}	
	}

	public static void exportProtocolsIcal(Exam exam, OutputStream ostream, Locale locale) throws IOException {
		Translator translator = Util.createPackageTranslator(Exam.class, locale);

		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//OpenOLAT//xman//DE"));
		calendar.getProperties().add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);

		List<Protocol> protocols = ProtocolManager.getInstance().findAllProtocolsByExam(exam);
		for(Protocol protocol : protocols) {
			String name = protocol.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + protocol.getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
			String matrikel = protocol.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);

			DateTime start = new DateTime(protocol.getAppointment().getDate());
			start.setUtc(true);
			Date end = Date.from(start.toInstant().plusSeconds(protocol.getAppointment().getDuration() * 60));

			String summary = translator.translate("Exam") + " " + exam.getName() + ": " + name + " (" + matrikel + ")";
			String comment = new NekoHTMLFilter().filter(protocol.getComments());
			Description description = new Description(comment + " (" + protocol.getStudyPath() + ")");
			Location location = new Location(protocol.getAppointment().getPlace());

			String uniqueId = WebappHelper.getInstanceId() + ":xman-ics-export:" + exam.getKey() + ":" + protocol.getAppointment().getKey();
			Uid hashedUniqueId = new Uid(DigestUtils.sha256Hex(uniqueId));

			VEvent event = new VEvent(start, new Dur(start, end), summary);
			event.getProperties().add(description);
			event.getProperties().add(location);
			event.getProperties().add(hashedUniqueId);

			calendar.getComponents().add(event);
		}

		CalendarOutputter outputter = new CalendarOutputter();
		try {
			outputter.output(calendar, ostream);
		} catch (ValidationException e) {
			throw new RuntimeException("Error while creating the ical for export", e);
		}
	}
}
