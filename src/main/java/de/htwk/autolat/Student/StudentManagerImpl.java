package de.htwk.autolat.Student;

import java.util.List;

import org.hibernate.dialect.DB2390Dialect;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;

import de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;

public class StudentManagerImpl extends StudentManager {
	
	private static final StudentManagerImpl INSTANCE = new StudentManagerImpl();
	
	private StudentManagerImpl() {
		//nothing to do here
	}

	@Override
	public Student createStudent(List<TaskInstance> taskInstanceList, Identity identity) {
		Student stud = new StudentImpl();
		stud.setIdentity(identity);
		stud.setTaskInstanceList(taskInstanceList);
		return stud;
	}
	
	@Override
	public Student createAndPersistStudent(List<TaskInstance> taskInstanceList, Identity identity) {
		Student stud = createStudent(taskInstanceList, identity);
		saveStudent(stud);
		return stud;
	}
	
	@Override
	public Student getStudentByIdentity(Identity identity) {
		String query = "SELECT stud FROM StudentImpl AS stud WHERE stud.identity = :key";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		dbq.setLong("key", identity.getKey());
		List<Student> result = (List<Student>) dbq.list();
		return (result.size() == 0) ? null : result.get(0);
	}

	@Override
	public boolean deleteStudent(Student stud) {
		try {
			DBFactory.getInstance().deleteObject(stud);
			return true;
		}catch (Exception e) {
			return false;
		}
	}

	@Override
	public Student loadStudentByID(long ID) {
		return (Student)DBFactory.getInstance().loadObject(StudentImpl.class, ID);
	}
	
	public void deleteLivingTaskInstanceByTaskInstance(TaskInstance taskInstance) {
		if( (taskInstance != null) && taskInstance.getLivingTaskInstance() != null)
		LivingTaskInstanceManagerImpl.getInstance().deleteLivingTaskInstance(taskInstance.getLivingTaskInstance());
	}

	@Override
	public void saveStudent(Student stud) {
		DBFactory.getInstance().saveObject(stud);
	}

	@Override
	public void updateStudent(Student stud) {
		DBFactory.getInstance().updateObject(stud);
	}
	
	public static StudentManagerImpl getInstance() {
		return INSTANCE;
	}

}
