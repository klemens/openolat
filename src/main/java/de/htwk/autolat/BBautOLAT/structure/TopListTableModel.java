package de.htwk.autolat.BBautOLAT.structure;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.Identity;

import de.htwk.autolat.BBautOLAT.BBautOLATCourseNode;
import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.tools.Scoring.ScoreObject;
import de.htwk.autolat.tools.Scoring.ScoreObjectManagerImpl;
/**
 * 
 * Description:<br>
 * The top list table model shows a table of all autotool tasks belongs to the structure node. <br>
 * The reached score is highlighted red.
 * 
 * <P>
 * Initial Date:  18.05.2010 <br>
 * @author Joerg
 */
public class TopListTableModel extends DefaultTableDataModel implements TableDataModel {
	/**
	 * List ob autOlat Nodes
	 */
	private List<BBautOLATCourseNode> tasks;
	/**
	 * List of Toplist for each autolat Node
	 */
	private List<List<ScoreObject>> topLists;
	/**
	 * the user identity
	 */
	private Identity user;
	private long courseID;
	/**
	 * Constructor
	 * @param objects
	 * @param user
	 * @param tasks
	 */
	public TopListTableModel(long courseID, List objects, Identity user, List<BBautOLATCourseNode> tasks) {
		super(objects);

		this.tasks = new ArrayList<BBautOLATCourseNode>();
		this.tasks = tasks;
		this.user = user;
		this.courseID = courseID;
		
		//create toplists once
		topLists = new ArrayList<List<ScoreObject>>();
		for(BBautOLATCourseNode aTask : this.tasks) {
			List<ScoreObject> taskTopList = ScoreObjectManagerImpl.getInstance().createCourseNodeToplist(courseID, Long.valueOf(aTask.getIdent()));
			topLists.add(taskTopList);
		}
	}

	@Override
	public int getColumnCount() {
		return tasks.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity tempUser = (Identity)objects.get(row);
		
		for(int i = 0; i < tasks.size(); i++) {
			if( col == i) {
				int score = getScore(tempUser, tasks.get(i), i);
				String prefix = getPrefix(score);
				if(user.equals(tempUser)) {
					boolean passed = getPassed(tempUser, tasks.get(i), i);
					return passed ? "<span id=\"0" + prefix + score + "\" class=\"o_passed\">" + score  + "</span>"
							             : "<span id=\"0" + prefix + score + "\" class=\"o_notpassed\">" + score  + "</span>";
					// "<font id=\"0" + prefix + score + "\" color=\"#FF0000\">" + score  + "</font>";
				}
				else {
					return "<span id=\"0" + prefix + score + "\">" + score  + "</span>";
					//"<font id=\"0" + prefix + score + "\" color=\"#000000\">" + score  + "</font>";
				}
			}
		}
		
		if(col == getColumnCount()) {
			int sum = 0;
			for(int i = 0; i < tasks.size(); i++) {
				sum += getScore(tempUser, tasks.get(i), i);
			}
			String prefix = getPrefix(sum);
			if(user.equals(tempUser)) {
				return "<font id=\"0" + prefix + sum + "\">" + sum + "</font>";
				// "<font id=\"0" + prefix + sum + "\" color=\"#FF0000\">" + sum + "</font>";
			}
			else {
				return "<font id=\"0" + prefix + sum + "\">" + sum + "</font>";
				// "<font id=\"0" + prefix + sum + "\" color=\"#000000\">" + sum + "</font>";
			}
		}
		
		return "unknown";
	}
	private boolean getPassed(Identity tempUser, BBautOLATCourseNode bBautOLATCourseNode, int i) {
		Student stud = StudentManagerImpl.getInstance().getStudentByIdentity(user);
		Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, Long.valueOf(bBautOLATCourseNode.getIdent().trim()));
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

	private String getPrefix(int sum) {
		String summe = String.valueOf(sum).trim();
		int length = summe.length();
		String returnValue = "";
		//System.out.println(summe +"::::LÄNGE:" + length);
		for(int i = 0; i < (5 - length); i++){
			returnValue += "0";
		}
		return returnValue;
	}

	/**
	 * 
	 * @param tempUser
	 * @param bBautOLATCourseNode
	 * @param taskIndex
	 * @return the reached score 
	 */
	private int getScore(Identity tempUser, BBautOLATCourseNode bBautOLATCourseNode, int taskIndex) {
		List<ScoreObject> topList = topLists.get(taskIndex);
		for(ScoreObject aObject : topList) {
			if(aObject.getIdentity().equals(tempUser)) {
				return aObject.getScorePoints();
			}
		}
		
		return 0;
		
	}

}
