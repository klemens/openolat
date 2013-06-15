package de.htwk.autolat.Student;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;

import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.ServerConnection.ServerConnectionManager;
import de.htwk.autolat.TaskInstance.TaskInstance;

public abstract class StudentManager {
	
	public abstract Student createStudent(List<TaskInstance> taskInstanceList, Identity identity);
	
	public abstract Student createAndPersistStudent(List<TaskInstance> taskInstanceList, Identity identity);
	
	public abstract Student loadStudentByID(long ID);
	
	public abstract Student getStudentByIdentity(Identity identity);
		
	public abstract void saveStudent(Student stud);
	
	public abstract void updateStudent(Student stud);
	
	public abstract boolean deleteStudent(Student stud);

}
