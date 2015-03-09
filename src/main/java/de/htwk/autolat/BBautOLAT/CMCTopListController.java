package de.htwk.autolat.BBautOLAT;

import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;

import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.tools.Scoring.ScoreObject;
import de.htwk.autolat.tools.Scoring.ScoreObjectManagerImpl;

public class CMCTopListController extends BasicController
{
	private VelocityContainer mainVC;
	private TableController topListTable;
	
	/**
	 * Instantiates a new CMC top list controller. The top list will display a table
	 * with all scores that were achieved for a given task (which will be
	 * identified by its courseID). Only the table row for the user with a given
	 * identity (typically the user that accesses the top list) will be labeled due
	 * to privacy protection. This controller is called by the showTopList button in the
	 * TaskInstanceRunController.
	 *
	 * @param ureq the ureq
	 * @param wControl the w control
	 * @param userID the user ID that will be labeled
	 * @param courseNodeID the course id
	 */
	protected CMCTopListController(UserRequest ureq, WindowControl wControl, Identity userID, long courseID, long courseNodeID)
	{
		super(ureq, wControl);
				
		List<ScoreObject> scores = ScoreObjectManagerImpl.getInstance().createCourseNodeToplist(courseID, courseNodeID);
		TableGuiConfiguration tgc = new TableGuiConfiguration();
    tgc.setPreferencesOffered(true, "TopListTable");
        
    topListTable = new TableController(tgc, ureq, getWindowControl(), getTranslator());    

    DefaultColumnDescriptor column;
    column = new DefaultColumnDescriptor("label.table.toplist.score", 0, null, ureq.getLocale());
    column.setEscapeHtml(EscapeMode.none);
    topListTable.addColumnDescriptor(column);
    column = new DefaultColumnDescriptor("label.table.toplist.size", 1, null, ureq.getLocale());
    column.setEscapeHtml(EscapeMode.none);
    topListTable.addColumnDescriptor(column);
    column = new DefaultColumnDescriptor("label.table.toplist.date", 2, null, ureq.getLocale());
    column.setEscapeHtml(EscapeMode.none);
    topListTable.addColumnDescriptor(column);
        
    topListTable.setSortColumn(0, false);
    
    TableDataModel model = new TopListTableModel(scores, userID);
    topListTable.setTableDataModel(model);
		
		mainVC = createVelocityContainer("CMCTopListController");		
		createOutput(ureq);
		putInitialPanel(mainVC);
	}
	
	private void createOutput(UserRequest ureq)
	{
		mainVC.put("topListTable", topListTable.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest arg0, Component arg1, Event arg2) {
		// TODO Auto-generated method stub
		
	}
	

}
