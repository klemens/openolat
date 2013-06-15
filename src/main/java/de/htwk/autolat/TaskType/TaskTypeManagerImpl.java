package de.htwk.autolat.TaskType;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;

public class TaskTypeManagerImpl extends TaskTypeManager {
	
	private static final TaskTypeManagerImpl INSTANCE = new TaskTypeManagerImpl();
	
	private TaskTypeManagerImpl() {
		//nothing to do here
	}

	@Override
	public TaskType createAndPersistTaskType(String taskType, String scoringOrder) {
		TaskType type = new TaskTypeImpl();
		type.setType(taskType);
		type.setScoringOrder(scoringOrder);
		saveTaskType(type);
		return type;
	}

	@Override
	public TaskType createTaskType(String taskType, String scoringOrder) {
		TaskType type = new TaskTypeImpl();
		type.setType(taskType);
		type.setScoringOrder(scoringOrder);
		return type;
	}

	@Override
	public boolean deleteTaskType(TaskType type) {
		try{
			DBFactory.getInstance().deleteObject(type);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

	@Override
	public TaskType findTaskTypeByType(String type) {
		String query = "SELECT tt FROM TaskTypeImpl AS tt WHERE tt.type LIKE :type";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		dbq.setString("type", type);
		List<TaskType> result = (List<TaskType>) dbq.list();
		return (result.size()>0 ? result.get(0) : null);
	}

	@Override
	public TaskType loadTaskTypeByID(long ID) {
		String query = "SELECT tt FROM TaskTypeImpl AS tt WHERE tt.key = :key";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		dbq.setLong("type", ID);
		List<TaskType> result = (List<TaskType>) dbq.list();
		return result.get(0);
	}

	@Override
	public void saveTaskType(TaskType type) {
		DBFactory.getInstance().saveObject(type);
	}

	@Override
	public void updateTaskType(TaskType type) {
		DBFactory.getInstance().updateObject(type);
	}
	
	@Override
	public void updateTaskTypeScoringOrder(TaskType taskType, String scoringOrder) {
		
		String oldOrder = taskType.getScoringOrder();
		if(!oldOrder.equals(scoringOrder)) {
			taskType.setScoringOrder(scoringOrder);
			updateTaskType(taskType);
		}
	}
	
	@Override
	public void saveOrUpdateTaskType(TaskType taskType) {
		
		if(taskType.getKey() == null) {
			saveTaskType(taskType);
		} else {
			updateTaskType(taskType);
		}
	}
	
	public static TaskTypeManagerImpl getInstance() {
		return INSTANCE;
	}

}
