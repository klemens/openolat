package de.htwk.autolat.TaskSolution;

import java.util.Date;

import org.olat.core.id.Persistable;

import de.htwk.autolat.TaskInstance.TaskInstance;

public interface TaskSolution extends Persistable {
	
	public void setSolutionText(String text);
	public String getSolutionText();
	
	public void setScore(double score);
	public double getScore();
	
	public void setEvaluationText(String text);
	public String getEvaluationText();
	
	public void setSolutionDate(Date date);
	public Date getSolutionDate();
	
	public TaskInstance getTaskInstance();
	public void setTaskInstance(TaskInstance taskInstance);

}
