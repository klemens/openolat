package de.htwk.autolat.TaskType;

import org.olat.core.commons.persistence.PersistentObject;

public class TaskTypeImpl extends PersistentObject implements TaskType {
	
	public static final String SO_NONE = "n";
	public static final String SO_DECREASING = "d";
	public static final String SO_INCREASING = "i";
	
	private String type;
	private String scoringOrder;

	public String getType() {
		return type;
	}

	public void setType(String typeIdentifier) {
		this.type = typeIdentifier;
	}

	public void setScoringOrder(String scoringOrder) {
		this.scoringOrder = SO_NONE;
		if(scoringOrder.equals(SO_INCREASING))
			this.scoringOrder = SO_INCREASING;
		if(scoringOrder.equals(SO_DECREASING))
			this.scoringOrder = SO_DECREASING;			
	}

	public String getScoringOrder() {
		return scoringOrder;
	}

}
