package de.htwk.autolat.TaskModule;

import java.util.*;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;

public class TaskModuleManagerImpl extends TaskModuleManager {
	
	private static final String PACKAGE = Util.getPackageName(TaskModuleManagerImpl.class);
	
	private static final TaskModuleManagerImpl INSTANCE = new TaskModuleManagerImpl();
	
	private TaskModuleManagerImpl() {
		//nothing to do here
	}

	@Override
	public TaskModule createTaskModule(long duration, Date endDate, long maxCount, TaskModule nextModule, Configuration conf) {
		TaskModule module = new TaskModuleImpl();
		module.setConfiguration(conf);
		module.setMaxCount(maxCount);
		module.setNextModule(nextModule);
		module.setTaskDuration(duration);
		module.setTaskEndDate(endDate);
		return module;
	}

	@Override
	public boolean deleteTaskModule(TaskModule module) {
		try {
			DBFactory.getInstance().deleteObject(module);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

	@Override
	public List findTaskModule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskModule loadTaskModuleByID(long ID) {
		return (TaskModule)DBFactory.getInstance().loadObject(TaskModuleImpl.class, ID);
	}

	@Override
	public void saveTaskModule(TaskModule module) {
		DBFactory.getInstance().saveObject(module);
	}

	@Override
	public void updateTaskModule(TaskModule module) {
		DBFactory.getInstance().updateObject(module);
	}

	@Override
	public TaskModule createAndPersistTaskModule(long duration, Date endDate, long maxCount, TaskModule nextModule, Configuration conf) {
		TaskModule taskModule = createTaskModule(duration, endDate, maxCount, nextModule, conf);
		saveTaskModule(taskModule);
		return taskModule;
	}
	
	@Override
	public String getDurationValue(long duration) {
		
		String result = "";
		long rest = duration/1000;
		
		result+= String.valueOf(rest / 86400) + ":";
		rest = rest % 86400;
		
		result+= String.valueOf(rest / 3600) + ":";
		rest = rest % 3600;

		result+= String.valueOf(rest / 60) + ":";
		rest = rest % 60;
		
		result+= String.valueOf(rest);
		return result;
		
	}
	
	@Override
	public String getDurationValueWithLabels(long duration, Locale locale) {
		
		Translator translator = new PackageTranslator(PACKAGE, locale);
		
		String result = "";
		long rest = duration/1000;
		
		result+= String.valueOf(rest / 86400) + " " + translator.translate("label.manager.taskmodule.duration_d") + ", ";
		rest = rest % 86400;
		
		result+= String.valueOf(rest / 3600) + " " + translator.translate("label.manager.taskmodule.duration_h") + ", ";
		rest = rest % 3600;

		result+= String.valueOf(rest / 60) + " " + translator.translate("label.manager.taskmodule.duration_m") + ", ";
		rest = rest % 60;
		
		result+= String.valueOf(rest) + " " + translator.translate("label.manager.taskmodule.duration_s");
		return result;
		
	}
	
	@Override
	public long parseDurationValue(String value) throws Exception {
		
		String[] tempValue = value.split(":");
		long result = Long.valueOf(tempValue[0]) * 86400 +
									Long.valueOf(tempValue[1]) * 3600 +
									Long.valueOf(tempValue[2]) * 60 +
									Long.valueOf(tempValue[3]);
		result*=1000;
		return result;
	}
	
	public static TaskModuleManagerImpl getInstance() {
		return INSTANCE;
	}

	@Override
	public TaskModule getNextTaskModule(long courseID, long courseNodeID, TaskModule previousModule) {
		
		Configuration configuration = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		List<TaskModule> taskModules = configuration.getTaskPlan();
		
		if(previousModule==null) {
			 if(taskModules.size()==0) return null;
			 else return taskModules.get(0);
		}
		else {
			if(previousModule.getNextModule()==null) {
				Iterator<TaskModule> iterator = taskModules.iterator();
				while(iterator.hasNext()) {
					TaskModule tempModule = iterator.next();
					if(tempModule.getKey().equals(previousModule.getKey())) {
						return (iterator.hasNext() ? iterator.next() : previousModule);
					}
				}
				//this should never be reached
				return previousModule;
			}
			else return previousModule.getNextModule();
		}
		
	}
	
	@Override 
	public boolean evaluateTaskModuleConditions (TaskModule taskModule, long counter, Date creationDate) {
		
		if(taskModule==null) return false;
		
		boolean result = false;
		
		if(taskModule.getTaskEndDate()!=null) {
			if(new Date().after(taskModule.getTaskEndDate())) result = true;
		}
		
		if(taskModule.getTaskDuration()!=0) {
			if(new Date().getTime() - creationDate.getTime() >= taskModule.getTaskDuration()) result = true;
		}
		
		if(taskModule.getMaxCount()!=0) {
			if(counter >= taskModule.getMaxCount()) result = true;
		}
		
		return result;
	}
	
	@Override
	public Date getDurationEndDate (TaskModule taskModule, Date creationDate) {
		
		if(taskModule.getTaskDuration()!=0) {
			return new Date(creationDate.getTime()+taskModule.getTaskDuration());
		}
		else return null;
	}
	
	@Override
	public Date getTaskModuleEndDate (TaskModule taskModule, Date creationDate) {
		
		Date durationEndDate = this.getDurationEndDate(taskModule, creationDate);
		Date endDate = null;
		
		if(durationEndDate!=null) {
			if(taskModule.getTaskEndDate()!=null) {
				endDate = (durationEndDate.before(taskModule.getTaskEndDate()) ? durationEndDate : taskModule.getTaskEndDate());
			}
			else endDate = durationEndDate;
		}
		else {
			if(taskModule.getTaskEndDate()!=null) endDate = taskModule.getTaskEndDate();
		}
		
		return endDate;
	}
	
}
