package de.htwk.autolat.tools.Scoring;

import java.util.Date;

import org.olat.core.id.Identity;

import de.htwk.autolat.Configuration.Configuration;

public interface ScoreObject {

	public void setConfiguration(Configuration configuration);
	public Configuration getConfiguration();
	public void setIdentity(Identity identity);
	public Identity getIdentity();
	public void setScoreDate(Date scoreDate);
	public Date getScoreDate();
	public void setScoreSize(double scoreSize);
	public double getScoreSize();
	public void setScorePoints(int scorePoints);
	public int getScorePoints();
}
