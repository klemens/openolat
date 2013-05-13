package de.unileipzig.xman.exam.controllers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailTemplateForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.resource.OLATResourceManager;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.admin.mail.form.MailForm;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentTableModel;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.controller.ESFCreateController;
import de.unileipzig.xman.esf.controller.ESFEditController;
import de.unileipzig.xman.esf.form.ESFCommentCreateAndEditForm;
import de.unileipzig.xman.esf.table.ESFTableModel;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.forms.ChooseExamAttrForm;
import de.unileipzig.xman.exam.forms.EditMarkForm;
import de.unileipzig.xman.exam.forms.ExamDetailsForm;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import de.unileipzig.xman.protocol.tables.ProtocolTableModel;

/**
 * The Controller for the details, asked when writing into an Exam
 * 
 * @author robert seidler
 *
 */
public class ExamDetailsController extends MainLayoutBasicController implements MainLayoutController {


	private VelocityContainer vcAttr;
	private ExamDetailsForm examDetailsForm;
	private Translator translator;
	private String clawback;
	private String examType;
	private TableEvent te;
	
	
	/**
	 * Create the Controller for the Details Form
	 * @param ureq
	 * @param wControl
	 */
	public ExamDetailsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		translator = Util.createPackageTranslator(Exam.class, ureq.getLocale());

		setTranslator(translator);

		vcAttr = new VelocityContainer("vcAttr", Util.getPackageVelocityRoot(Exam.class)
				+ "/chooseAttr.html", translator, this);

		examDetailsForm = new ExamDetailsForm(ureq, wControl);
		examDetailsForm.addControllerListener(this);

		vcAttr.put("chooseExamAttrForm", examDetailsForm.getInitialComponent());

		this.putInitialPanel(vcAttr);
		
	}
	
	public String getClawback(){
		return clawback;	
	}
	
	public String getChooseExamType(){
		return examType;
	}
	

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// TODO Auto-generated method stub
		if(source == examDetailsForm){			
			if(event == Form.EVNT_VALIDATION_OK){
				clawback = examDetailsForm.getChooseClawback();
				examType = examDetailsForm.getExamTypeSwitchElem();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}

	public TableEvent getTe() {
		return te;
	}

	public void setTe(TableEvent te) {
		this.te = te;
	}
	
	
}