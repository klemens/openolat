package de.unileipzig.xman.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

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
}
