package de.htwk.autolat.BBautOLAT.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

import de.htwk.autolat.BBautOLAT.BBautOLATCourseNode;
import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.tools.Scoring.ScoreObject;
import de.htwk.autolat.tools.Scoring.ScoreObjectManagerImpl;

public class PointOverviewTableModel extends DefaultTableDataModel implements TableDataModel{
	/**
	 * List of tasks given from a learninggroup or a leaningarea
	 */
	private List<BBautOLATCourseNode> tasks;
	/**
	 * List of toplists associatet to a task
	 */
	private List<List<ScoreObject>> topLists; 
	/**
	 * List of configurations 
	 */
	private List<Configuration> configs;

	/**
	 * Used to prevent identity lookups on every getValueAt call
	 */
	private Map<Identity, Student> userCache;
	/**
	 * Constructor
	 * @param objects
	 * @param tasks
	 */
	public PointOverviewTableModel(long courseID, List objects, List<BBautOLATCourseNode> tasks) {
		super(objects);
		
		this.tasks = new ArrayList<BBautOLATCourseNode>();
		this.configs = new ArrayList<Configuration>();
		this.tasks = tasks;
		
		this.userCache = new HashMap<Identity, Student>();
		
		//create toplists
		topLists = new ArrayList<List<ScoreObject>>();
		for(BBautOLATCourseNode aTask : this.tasks) {
			this.configs.add(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, Long.valueOf(aTask.getIdent())));
			List<ScoreObject> taskTopList = ScoreObjectManagerImpl.getInstance().createCourseNodeToplist(courseID, Long.valueOf(aTask.getIdent()));
			topLists.add(taskTopList);
		}
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return tasks.size() + 4;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity user = (Identity) objects.get(row);
		
		for(int i = 0; i < tasks.size(); i++) {
			if(col == i+3) {
				//return getScore(user, tasks.get(i));
				return getPassed(user, tasks.get(i), i) ? "<span id=\"0" 
						+ getPrefix(getPureScore(user, tasks.get(i), i)) 
						+ getPureScore(user, tasks.get(i), i) 
						+ "\"class=\"o_passed\">"
						+ getPureScore(user, tasks.get(i), i)
						+ " </span>"
						: "<span id=\"0" 
						+ getPrefix(getPureScore(user, tasks.get(i), i)) 
						+ getPureScore(user, tasks.get(i), i) 
						+ "\"class=\"o_notpassed\">"
						+ getPureScore(user, tasks.get(i), i)
						+ " </span>";
			}
		}
		if(col == getColumnCount()) {
			int result = 0;
			//int resultTop = 0;
			int resultPassed = 0;
			for(int i = 0; i < tasks.size(); i++) {
				result += getPureScore(user, tasks.get(i), i);
				//resultTop += getScore(user, tasks.get(i), i);
				if(getPassed(user, tasks.get(i), i)) {
					resultPassed++;
				}
			}
			String prefix = getPrefix(result);
			
			
			return "<font id=\"0" 
						+ prefix 
						+ result
						+ "\">"
						+ result
						+ " / " + resultPassed + " </font>";
		}
		if(col == 0) {
			return user.getName();
		}
		if(col == 1) {
			return user.getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		}

		return "unknown";
		
		
		// TODO Auto-generated method stub
		
	}

	private String getPrefix(int value) {
		String summe = String.valueOf(value).trim();
		int length = summe.length();
		String returnValue = "";
		//System.out.println(summe +"::::LÄNGE:" + length);
		for(int i = 0; i < (5 - length); i++){
			returnValue += "0";
		}
		return returnValue;
	}

	private int getPureScore(Identity user, BBautOLATCourseNode bBautOLATCourseNode, int taskIndex) {
		Student stud = getStudent(user);
		Configuration conf = configs.get(taskIndex);
		TaskInstance instance = null;
		if(stud != null)
			instance = stud.getTaskInstanceByConfiguration(conf);
		
		if(instance == null) {
			return 0;
		}
		
		TaskResult result = instance.getResult();
		
		if(result == null) {
			return 0;
		}
		
		return (int)result.getMaxScore();
	}

	private boolean getPassed(Identity user, BBautOLATCourseNode bautOLATCourseNode, int taskIndex) {
		
		Student stud = getStudent(user);
		Configuration conf = configs.get(taskIndex);
		if(stud == null) {
			return false;
		}
 		
		TaskInstance instance = stud.getTaskInstanceByConfiguration(conf);
		
		if(instance == null) {
			return false;
		}
		
		TaskResult result = instance.getResult();
		
		if(result == null) {
			return false;
		}
		
		return result.getHasPassed();
		
	}

	private Student getStudent(Identity identity) {
		Student student = userCache.get(identity);

		if(student == null) {
			student = StudentManagerImpl.getInstance().getStudentByIdentity(identity);
			userCache.put(identity, student); // ok even if student == null
		}

		return student;
	}
}

