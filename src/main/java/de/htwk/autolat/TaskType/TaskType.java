package de.htwk.autolat.TaskType;

import org.olat.core.id.Persistable;

public interface TaskType extends Persistable {
	
	public void setType(String typeIdentifier);
	
	public String getType();
	
	public void setScoringOrder(String scoringOrder);

	public String getScoringOrder();
}
