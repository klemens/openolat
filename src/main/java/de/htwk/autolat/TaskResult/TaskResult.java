package de.htwk.autolat.TaskResult;

import java.util.Date;

import org.olat.core.id.Persistable;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskSolution.TaskSolution;

public interface TaskResult extends Persistable {
	
	public void setHasPassed(boolean passed);
	public boolean getHasPassed();
	
	public void setMaxScoreSolutionText(String solutionText);
	public String getMaxScoreSolutionText();
	
	public void setMaxScoreTaskText(String taskText);
	public String getMaxScoreTaskText();
	
	public void setMaxScore(double score);
	public double getMaxScore();
	
	public void setMaxScoreDate(Date maxScoreDate);
	public Date getMaxScoreDate();
}
