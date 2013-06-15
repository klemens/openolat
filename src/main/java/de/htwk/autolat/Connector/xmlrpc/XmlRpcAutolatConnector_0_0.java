package de.htwk.autolat.Connector.xmlrpc;

import java.net.URL;
import java.util.List;

import redstone.xmlrpc.XmlRpcFault;

import de.htwk.autolat.Connector.*;
import de.htwk.autolat.Connector.types.*;
import de.htwk.autolat.Connector.xmlrpc.parse.*;
import de.htwk.autolat.Connector.xmlrpc.serialize.*;

/**
 * XML-RPC based autolat connector.
 * 
 * @author Bertram Felgenhauer
 */
public class XmlRpcAutolatConnector_0_0 implements AutolatConnector_0_0
{
	private final AutolatXmlRpcClient conn;
	
	private void checkServerVersion(URL url) throws AutolatConnectorException
	{
		Version vers = getServerInfo().getServerVersion();
		if (vers.getMajor() != 0 || vers.getMinor() < 0) {
			throw makeAutolatConnectorException("Incompatible server version at '" + url + "'", null);
		}
	}
	
	/**
	 * @param url     Service URL
	 * @param timeout timeout for RPC requests, in milliseconds
	 * @throws AutolatConnectorException
	 */
	public XmlRpcAutolatConnector_0_0(URL url, int timeout) throws AutolatConnectorException
	{
		conn = new AutolatXmlRpcClient(url, true, timeout);
		checkServerVersion(url);
	}
	
	/**
	 * (the default timeout is 20s)
	 *
	 * @param url     Service URL
	 * @throws AutolatConnectorException
	 */
	public XmlRpcAutolatConnector_0_0(URL url) throws AutolatConnectorException
	{
		conn = new AutolatXmlRpcClient(url, true);
		checkServerVersion(url);
	}
	
	/**
	 * Set timeout per XML-RPC request
	 * 
	 * @param timeout - timeout in milliseconds
	 */
	public void setTimeout(int timeout)
	{
		conn.setTimeout(timeout);
	}
	
	public ServerInfo getServerInfo() throws AutolatConnectorException
	{
		try {
			Object[] args = {};
			Object result;
			// System.err.println(args);
			result = conn.invoke("get_server_info", args);
			// System.err.println(result);
			return ServerInfoParser.getInstance().parse(result);
		}
		catch (Exception e) {
			throw makeAutolatConnectorException("Error while processing 'get_server_info' request.", e);
		}
	}
	
	static private final Parser<List<TaskTree>> listTaskTreeParser =
		new ListParser<TaskTree>(TaskTreeParser.getInstance());
	
	public List<TaskTree> getTaskTypes() throws AutolatConnectorException
	{
		try {
			Object[] args = {};
			Object result;
			result = conn.invoke("get_task_types", args);
			// System.err.println(result);
			return listTaskTreeParser.parse(result);
		}
		catch (Exception e) {
			throw makeAutolatConnectorException("Error while processing 'get_task_types' request.", e);
		}
	}

	static private final Parser<Documented<String>> documentedConfigParser =
		new DocumentedParser<String>(StringParser.getInstance());

	public Documented<String> getTaskDescription(String task) throws AutolatConnectorException
	{
		try {
			Object[] args = {
				StringSerializer.getInstance().serialize(task)
			};
			Object result;
			// System.err.println(args[0]);
			result = conn.invoke("get_task_description", args);
			// System.err.println(result);
			return documentedConfigParser.parse(result);
		}
		catch (Exception e) {
			throw makeAutolatConnectorException("Error while processing 'get_task_description' request.", e);
		}
	}

	private static final Parser<Either<String, Signed<Pair<String, String>>>>
	verifyTaskConfigResultParser = new EitherParser<String, Signed<Pair<String, String>>>(
		StringParser.getInstance(),
		new SignedParser<Pair<String, String>>(new PairParser<String, String>(
			StringParser.getInstance(), StringParser.getInstance()
		)));

	public Either<String, Signed<Pair<String, String>>>
		verifyTaskConfig(String task, String config) throws AutolatConnectorException
	{
		try {
			Object[] args = {
				StringSerializer.getInstance().serialize(task),
				StringSerializer.getInstance().serialize(config)
			};
			// System.err.println(args[0]);
			// System.err.println(args[1]);
			Object result = conn.invoke("verify_task_config", args);
			// System.err.println(result);
			return verifyTaskConfigResultParser.parse(result);
		}
		catch (Exception e) {
			throw makeAutolatConnectorException("Error while processing 'verify_task_config' request.", e);
		}
	}

	private static final Parser<Triple<Signed<Pair<String, Instance>>, String, Documented<String>>>
		getTaskInstanceResultParser = new TripleParser<Signed<Pair<String, Instance>>, String, Documented<String>>(
			new SignedParser<Pair<String, Instance>>(new PairParser<String, Instance>(StringParser.getInstance(), InstanceParser.getInstance())),
			StringParser.getInstance(),
			new DocumentedParser<String>(StringParser.getInstance()));

	private static final Serializer<Signed<Pair<String, String>>>
		signedTaskConfigSerializer = new SignedSerializer<Pair<String, String>>(
			new PairSerializer<String, String>(
					StringSerializer.getInstance(),
					StringSerializer.getInstance()));
	
	public Triple<Signed<Pair<String, Instance>>, String, Documented<String>>
		getTaskInstance(Signed<Pair<String, String>> signedTaskConfig, String seed)
			throws AutolatConnectorException
	{
		try {
			Object[] args = {
				signedTaskConfigSerializer.serialize(signedTaskConfig),
				StringSerializer.getInstance().serialize(seed)
			};
			// System.err.println(args[0]);
			// System.err.println(args[1]);
			Object result = conn.invoke("get_task_instance", args);
			// System.err.println(result);
			return getTaskInstanceResultParser.parse(result);
		}
		catch (Exception e) {
			throw makeAutolatConnectorException("Error while processing 'get_task_instance' request.", e);
		}
	}

	private static final Serializer<Signed<Pair<String, Instance>>>
		signedTaskInstanceSerializer = new SignedSerializer<Pair<String, Instance>>(
			new PairSerializer<String, Instance>(
				StringSerializer.getInstance(),
				InstanceSerializer.getInstance()));

	private static final Parser<Either<String, Documented<Double>>>
		gradeTaskSolutionResultParser = new EitherParser<String, Documented<Double>>(
			StringParser.getInstance(),
			new DocumentedParser<Double>(DoubleParser.getInstance()));

	public Either<String, Documented<Double>>
		gradeTaskSolution(Signed<Pair<String, Instance>> signedTaskInstance, String solution)
			throws AutolatConnectorException
	{
		try {
			Object[] args = {
				signedTaskInstanceSerializer.serialize(signedTaskInstance),
				StringSerializer.getInstance().serialize(solution)
			};
			// System.err.println(args[0]);
			// System.err.println(args[1]);
			Object result = conn.invoke("grade_task_solution", args);
			// System.err.println(result);
			return gradeTaskSolutionResultParser.parse(result);
		}
		catch (Exception e) {
			throw makeAutolatConnectorException("Error while processing 'grade_task_solution' request.", e);
		}
	}

	private AutolatConnectorException makeAutolatConnectorException(String msg, Exception cause)
	{
		if (cause instanceof XmlRpcFault)
			return new AutolatConnectorRpcFault(msg, cause);
		return new AutolatConnectorException(msg, cause);
	}
}
