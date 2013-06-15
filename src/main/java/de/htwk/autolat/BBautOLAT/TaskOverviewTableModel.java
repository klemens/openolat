package de.htwk.autolat.BBautOLAT;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.NodeEvaluation;

import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.tools.Scoring.ScoreObject;
import de.htwk.autolat.tools.Scoring.ScoreObjectManagerImpl;
/**
 * 
 * Description:<br>
 * Tablemodel for the task overview
 * <br>
 * Columns
 * 0: students name 
 * 1: institute. ID
 * 2: instance Date
 * 3: nr. of solutions
 * 4: failed attempts
 * 5: passed yes/no
 * 6: points
 * 7: nr. of LivingInstances (each living instance is a new task)
 * 8: link to the task if exists
 * 9: link to the solution if exists
 * 10: copy task in testview if exists
 * 
 * 
 * <P>
 * Initial Date:  17.01.2010 <br>
 * @author Joerg
 */
public class TaskOverviewTableModel extends DefaultTableDataModel implements TableDataModel{

	private static final String PACKAGE = Util.getPackageName(TaskOverviewTableModel.class);
	private final int COLUMN_COUNT = 10;
	
	private PackageTranslator translator;
	
	private CourseEnvironment courseEnv;
	private NodeEvaluation ne;
	
	//private List<Student> students;
	private long courseNodeID;
	
	private List<ScoreObject> scoreObjects;
	private Long courseID;
	
	@SuppressWarnings("unchecked")
	public TaskOverviewTableModel(List objects, Locale locale, CourseEnvironment courseEnv, NodeEvaluation ne, long courseNodeID) {
		super(objects);
		
		courseID = courseEnv.getCourseResourceableId();
		this.scoreObjects = ScoreObjectManagerImpl.getInstance().createCourseNodeToplist(courseID, courseNodeID);
		
		this.setLocale(locale);
		this.translator = new PackageTranslator(PACKAGE, locale);
		this.courseNodeID = courseNodeID;		
		this.courseEnv = courseEnv;
		this.ne = ne;
		
		//students = new ArrayList<Student>();
		//if(objects != null) {
		//	for(int i = 0; i < objects.size(); i++) {
		//		students.add(StudentManagerImpl.getInstance().getStudentByIdentity((Identity) objects.get(i)));
		//	}
		//}
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity tempIdent = (Identity)objects.get(row);
		Student tempStud = StudentManagerImpl.getInstance().getStudentByIdentity(tempIdent);
		
		boolean hasTaskInstance = false;
		TaskInstance taskInstance = null;
		TaskSolution bestSolution = null;
		TaskResult taskResult = null;
		
		try {
			if(tempStud != null) {
				taskInstance = tempStud.getTaskInstanceByConfiguration(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, this.courseNodeID));
				hasTaskInstance = true;
				if(taskInstance == null) {
					hasTaskInstance = false;
				}
			}
		} catch(NullPointerException npe) {
			hasTaskInstance = false;
		}
		
		if(hasTaskInstance) {
			bestSolution = taskInstance.getBestSolution();
		}

		if(hasTaskInstance) {
			taskResult = taskInstance.getResult();
		}
		
		switch (col) {
			//Name
			case 0: 
				return ((Identity)objects.get(row)).getName();
			//Matr Nr.
			case 1:
				return ((Identity)objects.get(row)).getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
			//LivingInstance Date
				//view task	
			case 2:
				if(hasTaskInstance) {
					return translator.translate("label.tablemodel.taskoverview.viewtask");
				}
				return "N/A";
			//copy to test
			case 3:
				if(hasTaskInstance) {
					return translator.translate("label.tablemodel.taskoverview.test");
				}
				return "N/A";
			case 4:
				if(bestSolution != null) {
					return String.valueOf(bestSolution.getSolutionDate());
				}
				return "N/A";
			//Solutions	
			case 5:
				if(hasTaskInstance) {
					return taskInstance.getSolutionCounter();
				}
				return 0l;
			//tasks failed
			case 6:  
				if(hasTaskInstance) {
					return taskInstance.getFailedAttempts();
				}
				return 0l;
			//passed
			case 7:
				if((hasTaskInstance && taskResult != null) && taskResult.getHasPassed()) {
					return translator.translate("label.tablemodel.taskoverview.YES");
				}
				return translator.translate("label.tablemodel.taskoverview.NO");
			//scorepoints best solution
			case 8:
				if(taskResult != null) {
					return taskResult.getMaxScore();
				}
				return 0.0;
			//scorepoints
			case 9:
				if(tempIdent != null) {
					return getScorePoints(tempIdent);
				}
				return 0;
			//living instances
			case 10:
				if(hasTaskInstance) {
					return taskInstance.getLivingInstanceCounter();
				}
				return 0l;
			//view task	
			default:
				return "unknown";
		}
	}

	public TaskInstance getTaskInstanceAtRow(int rowid) {
		try {
			Student tempStud = StudentManagerImpl.getInstance().getStudentByIdentity((Identity)objects.get(rowid));
			return tempStud.getTaskInstanceByConfiguration(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, this.courseNodeID));
		} catch(NullPointerException npe) {
			return null;
		}
	}
		
	
	public TaskSolution getBestSolutionAtRow(int rowid) {
		try {
			Student tempStud = StudentManagerImpl.getInstance().getStudentByIdentity((Identity)objects.get(rowid));
			TaskInstance taskInstance = tempStud.getTaskInstanceByConfiguration(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, this.courseNodeID));
			if(taskInstance != null) {
				return taskInstance.getBestSolution();
			}
		} catch(NullPointerException npe) {
			return null;
		}
		return null;
	}
	
	public void setObjects(List objects) {
		this.objects = objects;
	}

	public Identity getIdentityByRow(int rowid) {
		Identity identity = (Identity)objects.get(rowid);
		return identity;
	}
	
	private int getScorePoints(Identity student) {
		for(ScoreObject aObject : scoreObjects) {
			if(aObject.getIdentity().equals(student)) {
				return aObject.getScorePoints();
			}
		}
		return 0;
	}

}
