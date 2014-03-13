package de.htwk.autolat.tools.Scoring;

import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.TaskType.TaskTypeImpl;

public class ScoreObjectManagerImpl {

	private static final ScoreObjectManagerImpl INSTANCE = new ScoreObjectManagerImpl();
	
	private ScoreObjectManagerImpl() {
		//nothing to do here
	}
	
	public static ScoreObjectManagerImpl getInstance() {
		return INSTANCE;
	}
	
	public List<ScoreObject> createCourseNodeToplist(long courseID, long nodeID) {
		Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, nodeID);
		List<Integer> scorePoints = conf.getScorePoints();

		// Prepare query. Sort depending on desired scoring order.
		String scoringOrder = conf.getTaskConfiguration().getTaskType().getScoringOrder();
		String dbqOrder;
		if (scoringOrder == TaskTypeImpl.SO_INCREASING) {
			dbqOrder = "so.scoreSize ASC, so.scoreDate ASC";
		} else if (scoringOrder == TaskTypeImpl.SO_DECREASING) {
			dbqOrder = "so.scoreSize DESC, so.scoreDate ASC";
		} else {
			// TaskTypeImpl.SO_NONE
			dbqOrder = "so.scoreDate ASC";
		}
		DBQuery dbq = DBFactory.getInstance().createQuery("SELECT so FROM ScoreObjectImpl AS so WHERE so.configuration = :conf ORDER BY " + dbqOrder);
		dbq.setMaxResults(scorePoints.size());
		dbq.setLong("conf", conf.getKey());

		List<ScoreObject> result = (List<ScoreObject>) dbq.list();

		// Fill in the score points.
		for (int i = 0; i < result.size() && i < scorePoints.size(); i++)
			result.get(i).setScorePoints(scorePoints.get(i));

		return result;
	}
}
