package de.htwk.autolat.Connector;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jdom.JDOMException;

import redstone.xmlrpc.XmlRpcFault;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.types.Documented;
import de.htwk.autolat.Connector.types.Either;
import de.htwk.autolat.Connector.types.Instance;
import de.htwk.autolat.Connector.types.Pair;
import de.htwk.autolat.Connector.types.ScoringOrder;
import de.htwk.autolat.Connector.types.Signed;
import de.htwk.autolat.Connector.types.Task;
import de.htwk.autolat.Connector.types.TaskDescription;
import de.htwk.autolat.Connector.types.TaskTree;
import de.htwk.autolat.Connector.types.Triple;
import de.htwk.autolat.Connector.xmlrpc.XmlRpcAutolatConnector_0_0;
import de.htwk.autolat.Connector.xmlrpc.XmlRpcAutolatConnector_0_1;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManagerImpl;
import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.ServerConnection.ServerConnectionManagerImpl;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskInstance.TaskInstanceManagerImpl;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.TaskSolution.TaskSolutionManagerImpl;
import de.htwk.autolat.TaskType.TaskType;
import de.htwk.autolat.TaskType.TaskTypeImpl;
import de.htwk.autolat.TaskType.TaskTypeManagerImpl;
import de.htwk.autolat.tools.XMLParser.AutotoolServer;
import de.htwk.autolat.tools.XMLParser.XMLParser;

public class BBautOLATConnector {
	
	//private static final BBautOLATConnector INSTANCE = new BBautOLATConnector();
	
	private XmlRpcAutolatConnector_0_1 connector;
	private AutotoolServer server;
	private Configuration conf;
	
	public BBautOLATConnector(Configuration conf) throws JDOMException, IOException, AutolatConnectorException {
		
		this.conf = conf;
		
		try {
			URL url = null;
			if(conf.getAutolatServer()!=null) {
				String serverName, serverVersion;
				XMLParser parser = new XMLParser();
				String[] temp = conf.getAutolatServer().split("<split>");
				serverName = temp[0].trim();
				serverVersion = temp[1].trim();
				server = parser.getServerListByNameAndVersion(serverName, serverVersion).get(0);
				url = server.getUrl();
			}
			connector = new XmlRpcAutolatConnector_0_1(url);
			//ServerConnectionManagerImpl.getInstance().setLastContact(conf.getServerConnection());
			//ServerConnectionManagerImpl.getInstance().setActive(conf.getServerConnection(), true);
		} catch (IndexOutOfBoundsException aioobe) {
			throw new AutolatConnectorException("server string broken");
		} catch (AutolatConnectorException e) {
			if(!handleConnectionProblem(e)) {
				throw new AutolatConnectorException("not able to connect to server");
			}
		} catch (JDOMException e) {
			throw new JDOMException("xml broken");
		} catch (IOException e) {
			throw new IOException("xml broken or not existent");
		} 
	}

	public List<TaskType> getTaskTypes() {
		
		List<TaskTree> typeList = null;
		List<String> result = new ArrayList<String>();
		List<TaskType> finalResult = new ArrayList<TaskType>();
		//try to receive the list from the server
		try{
			typeList = connector.getTaskTypes();
		} catch (Exception e) {
			//server problem possible: random reselect routine
			if(handleConnectionProblem(e)) return getTaskTypes();
			else return finalResult;
		}
		if(typeList!=null) {
			//iterate through the list and extract the tasks
			Iterator<TaskTree> typeIterator = typeList.iterator();
			while(typeIterator.hasNext()) {
				result = exploreTaskTree(typeIterator.next(), result);
			}
		}
		Iterator<String> resultIterator = result.iterator();
		while(resultIterator.hasNext()) {
			String temp = resultIterator.next();
			TaskType tempType;
			if(TaskTypeManagerImpl.getInstance().findTaskTypeByType(temp)==null) 
				tempType = TaskTypeManagerImpl.getInstance().createAndPersistTaskType(temp, TaskTypeImpl.SO_NONE);
			else
				tempType = TaskTypeManagerImpl.getInstance().findTaskTypeByType(temp);
			if(!finalResult.contains(tempType)) finalResult.add(tempType);
		}
		
		return finalResult;
	}
	
	public List<TaskTree> getTaskTree() {
		
		List<TaskTree> result = new ArrayList<TaskTree>();
		
		try{
			result = connector.getTaskTypes();
		}
		catch (Exception e) {
			if(handleConnectionProblem(e)) return getTaskTree();
			else return result;
		}
		
		//invoke the saving of all task types in the db
		List<TaskType> taskTypes = getTaskTypes();
		return result;
	}
	
	private List<String> exploreTaskTree(TaskTree taskTree, List<String> result) {
		
		if(taskTree.isTask()) {
			Task taskObject = (Task) taskTree;
			if(!result.contains(taskObject.getTaskName())) result.add(taskObject.getTaskName());
		}
		else {
			List<TaskTree> categoryTree = taskTree.getCategory().getSubTrees();
			Iterator<TaskTree> taskIterator = categoryTree.iterator();
			while(taskIterator.hasNext()) {
				TaskTree curEntry = taskIterator.next();
				result = exploreTaskTree(curEntry, result);
			}
		}
		return result;
	}
	
	public String getTaskTypeHierarchyBreadcrumb(TaskType type) {
		
		List<TaskTree> taskTree = getTaskTree();
		String result = getTaskTypeHierarchyIfContained(taskTree, "", type);
		if(result.startsWith("/")) result = result.substring(1, result.length());
		return result;
	}
	
	/*
	 * Helper function for searching the given task type in the task tree and extracting the hierarchy in the tree
	 */
	private String getTaskTypeHierarchyIfContained(List<TaskTree> taskTree, String currentBreadcrumb, TaskType type) {
		
		String result = "";
		
		Iterator<TaskTree> taskTreeIt = taskTree.iterator();
		while(taskTreeIt.hasNext()) {
			TaskTree temp = taskTreeIt.next();
			if(temp.isTask()) {
				if(temp.getTask().getTaskName().equals(type.getType())) {
					result = currentBreadcrumb + "/" + temp.getTask().getTaskName();
					break;
				}
			}
			else {
				if(!getTaskTypeHierarchyIfContained(temp.getCategory().getSubTrees(), "", type).equals("")) {
					result = getTaskTypeHierarchyIfContained(temp.getCategory().getSubTrees(), 
							currentBreadcrumb + "/" + temp.getCategory().getCategoryName(), type);
					break;
				}
			}
		}
		
		return result;
	}
	
	public TaskConfiguration getTaskConfiguration(TaskType taskType) {
		
		TaskDescription result = new TaskDescription(new Documented<String>("", "") , null);
		TaskConfiguration taskConfiguration = null;
		
		try{
			result = connector.getTaskDescription(taskType.getType());
		} catch (Exception e) {
			//server problem possible: random reselect routine
			if(handleConnectionProblem(e)) return getTaskConfiguration(taskType);
			else return taskConfiguration;
		}
		if(result!=null) {
			taskConfiguration = TaskConfigurationManagerImpl.getInstance().createTaskConfiguration(new ArrayList<TaskInstance>(), 
					result.getTaskSampleConfig().getContents(), result.getTaskSampleConfig().getDocumentation(), "", "", "", "", false, taskType);
			verifyTaskConfiguration(taskConfiguration);
			
			//update the task type scoring order finally
			String newOrder = TaskTypeImpl.SO_NONE;
			if(result.getTaskScoringOrder().isDecreasing()) newOrder = TaskTypeImpl.SO_DECREASING;
			if(result.getTaskScoringOrder().isIncreasing()) newOrder = TaskTypeImpl.SO_INCREASING;
			TaskTypeManagerImpl.getInstance().updateTaskTypeScoringOrder(taskType, newOrder);
		}
		return taskConfiguration;
	}
	
	public boolean verifyTaskConfiguration (TaskConfiguration taskConfiguration) {
		
		Either<String, Signed<Pair<String, String>>> result = null;
		
		try {
			result = connector.verifyTaskConfig(taskConfiguration.getTaskType().getType(), taskConfiguration.getConfigurationText());
		} catch (Exception e) {
			//server problem possible: random reselect routine
			if(handleConnectionProblem(e)) return verifyTaskConfiguration(taskConfiguration);
			else return false;
		}
		
		if(result!=null) {
			String left = result.getLeft();
			Signed<Pair<String,String>> right = result.getRight();
			if(left!=null) {
				taskConfiguration.setLastError(left);
				return false;
			}
			else {
				taskConfiguration.setSignature(right.getSignature());
				return true;
			}
		} else return false;
		
	}
	
	public LivingTaskInstance getLivingTaskInstance(TaskConfiguration taskConfiguration, String seed) {
		
		Signed<Pair<String,String>> taskConfigurationAsPair = new Signed<Pair<String, String>>(new Pair<String, String> (taskConfiguration.getTaskType().getType(), taskConfiguration.getConfigurationText()), taskConfiguration.getSignature());
		Triple<Signed<Pair<String, Instance>>, String, Documented<String>> result = null;
		
		try{
			
			result = connector.getTaskInstance(taskConfigurationAsPair, seed);
			
		} catch (Exception e) {
			e.printStackTrace();
			//server problem possible: random reselect routine
			if(handleConnectionProblem(e)) return getLivingTaskInstance(taskConfiguration, seed);
			else return null;
		}
		
		if(result!=null) {
			LivingTaskInstance instance = LivingTaskInstanceManagerImpl.getInstance().createLivingTaskInstance(result.getFirst().getSignature(), result.getSecond(), result.getFirst().getContents().getSecond().getContents(), result.getThird().getContents(), result.getThird().getDocumentation(), new Date());
			return instance;
		}
		else return null;
	}
	
	
	public TaskSolution gradeTaskSolution (LivingTaskInstance livingTaskInstance, String taskSolution) throws AutolatConnectorException {
		
		Signed<Pair<String, Instance>> instanceAsPair = new Signed<Pair<String, Instance>>(
				//task type for the whole object
				new Pair<String, Instance> (conf.getTaskConfiguration().getTaskType().getType(), 
				//instance with task type and internal task text
				new Instance(conf.getTaskConfiguration().getTaskType().getType(), livingTaskInstance.getInternalTaskText())), 
				//signature for the living instance
				livingTaskInstance.getSignature());
		
		// This may throw a AutolatConnectorException
		Either<String, Documented<Double>> result = connector.gradeTaskSolution(instanceAsPair, taskSolution);
		
		String documentation = "";
		double score = 0;
		
		if(result.getLeft()!=null) {
			documentation = result.getLeft();
		}
		else {
			documentation = result.getRight().getDocumentation();
			score = result.getRight().getContents();
		}
		
		TaskSolution resultSolution = TaskSolutionManagerImpl.getInstance().createTaskSolution(taskSolution, documentation, score, new Date());
		return resultSolution;
	}
	
	private boolean handleConnectionProblem(Exception exceptionToHandle) {
		exceptionToHandle.printStackTrace();
		
		//try all alternative URLs
		
		for(int i = 0; i < server.getAlternativURL().size(); i++) {
			try {
				connector = new XmlRpcAutolatConnector_0_1(server.getAlternativURL().get(i));
				return true;
			} catch (AutolatConnectorException e) {
				//nothing to do 
			}
		}
		
		//if(conf.getServerConnection()!=null) 
		//	ServerConnectionManagerImpl.getInstance().setActive(conf.getServerConnection(), false);
		//boolean success = false;
		//ServerConnection altConnection = null;
		//AutolatConnectorException ace = null;
		
		/*if(!(ace instanceof AutolatConnectorRpcFault)) {
			do {
				altConnection = ServerConnectionManagerImpl.getInstance().getRandomServerConnection();
				if(altConnection!=null) {
					try {
						URL url = altConnection.getUrl();
						connector = new XmlRpcAutolatConnector_0_1(url);
						//save the new connection to the database for ensuring connectivity
						conf.setServerConnection(altConnection);
						ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
						ServerConnectionManagerImpl.getInstance().setLastContact(conf.getServerConnection());
						success = true;
					} catch (Exception e) {
						ServerConnectionManagerImpl.getInstance().setActive(altConnection, false);
						success = false;
					}
				}
			} while((altConnection!=null) && !success);
		}
		return success;
		*/
		
		return false;
	}
	
}
