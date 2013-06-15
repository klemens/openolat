package de.htwk.autolat.Connector;

import java.util.List;

import de.htwk.autolat.Connector.types.*;

public interface AutolatConnector_0_1
{
	/**
	 * Query information about autOlat server.
	 * 
	 * The information contains the protocol version, server name and server version.
	 * 
	 * @return server info
	 * @throws AutolatConnectorException
	 */
	ServerInfo getServerInfo() throws AutolatConnectorException;
	
	/**
	 * Query task types supported by this autOlat server.
	 * 
	 * The returned information is a list of trees: Each item can either be a Task or a Category
	 * containing another list of subtrees.
	 * 
	 * @return supported tasks
	 * @throws AutolatConnectorException
	 */
	List<TaskTree> getTaskTypes() throws AutolatConnectorException;
	
	/**
	 * Query detailed information about a task.
	 * 
	 * The returned information consists of an example configuration for that task, with additional
	 * documentation describing the task.
	 * 
	 * @param task task name
	 * @return example configuration with documentation
	 * @throws AutolatConnectorException
	 */
	TaskDescription getTaskDescription(String task) throws AutolatConnectorException;

	/**
	 * Verify a task configuration.
	 * 
	 * If the configuration is valid, return a signed version of the configuration in the form
	 * Right(Signed(Pair(task, config), signature)).
	 * 
	 * Otherwise, return an explanation of the error in the form Left(description).
	 * 
	 * @param task    task name
	 * @param config  edited configuration
	 * @return        error description or signed task configuration
	 */
	Either<String, Signed<Pair<String, String>>> verifyTaskConfig(String task, String config)
	 	throws AutolatConnectorException;

	/**
	 * Get a task instance.
	 * 
	 * Given a signed task configuration, return an instance of the task. The instance will depend
	 * on the random seed.
	 * 
	 * While it is intended that the instance only depends on the configuration and seed, there is
	 * no guarantee of that fact, especially if the server was updated.
	 * 
	 * @param signedTaskConfig  signed task configuration
	 * @param seed              random seed
	 * @return                  signed task instance with description, and an example solution
	 * @throws AutolatConnectorException
	 */
	Triple<Signed<Pair<String, Instance>>, String, Documented<String>>
		getTaskInstance(Signed<Pair<String, String>> signedTaskConfig, String seed)
		throws AutolatConnectorException;

	/**
	 * Grade a solution.
	 * 
	 * If the solution is valid, returns a score with an explanation in the
	 * form Right(Documented(score, description)).
	 * 
	 * Otherwise, return an explanation of the error in the form Left(description).
	 * 
	 * @param signedTaskInstance  signed task instance
	 * @param solution            task solution  
	 * @return                    error description or score with explanation
	 * @throws AutolatConnectorException
	 */
	Either<String, Documented<Double>>
		gradeTaskSolution(Signed<Pair<String, Instance>> signedTaskInstance, String solution)
 		throws AutolatConnectorException;
}
