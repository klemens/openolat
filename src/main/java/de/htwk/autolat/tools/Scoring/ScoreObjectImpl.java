package de.htwk.autolat.tools.Scoring;

import java.util.Date;

import org.olat.core.id.Identity;

import de.htwk.autolat.Configuration.Configuration;

public class ScoreObjectImpl implements ScoreObject {

	private Configuration configuration;
	private Identity identity;
	private Date scoreDate;
	private double scoreSize;
	private int scorePoints;
	
	public ScoreObjectImpl() {
		super();
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	public Identity getIdentity() {
		return identity;
	}
	public void setScoreDate(Date scoreDate) {
		this.scoreDate = scoreDate;
	}
	public Date getScoreDate() {
		return scoreDate;
	}
	public void setScoreSize(double scoreSize) {
		this.scoreSize = scoreSize;
	}
	public double getScoreSize() {
		return scoreSize;
	}
	public void setScorePoints(int scorePoints) {
		this.scorePoints = scorePoints;
	}
	public int getScorePoints() {
		return scorePoints;
	}
	
}
