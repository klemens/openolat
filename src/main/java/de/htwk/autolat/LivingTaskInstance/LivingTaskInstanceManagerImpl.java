package de.htwk.autolat.LivingTaskInstance;

import java.util.Date;

import org.olat.core.commons.persistence.DBFactory;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.tools.XMLParser.OutputObject;
import de.htwk.autolat.tools.XMLParser.XMLParser;

/**
 * The Class LivingTaskInstanceManagerImpl implements the manager functions for a living task instance.
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class LivingTaskInstanceManagerImpl extends LivingTaskInstanceManager {
	
	/** The Constant INSTANCE. */
	private static final LivingTaskInstanceManagerImpl INSTANCE = new LivingTaskInstanceManagerImpl();
	
	/**
	 * Instantiates a new LivingTaskInstanceManagerImpl.
	 */
	private LivingTaskInstanceManagerImpl() {
		//nothing to do here
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManager#createLivingTaskInstance(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date)
	 */
	@Override
	public LivingTaskInstance createLivingTaskInstance(String signature, String taskText, String internalTaskText, String sampleSolution, String sampleDocumentation, Date creationDate) {
		LivingTaskInstance lti = new LivingTaskInstanceImpl();
		lti.setCreationDate(creationDate);
		lti.setSignature(signature);
		lti.setTaskText(taskText);
		lti.setInternalTaskText(internalTaskText);
		lti.setSampleSolution(sampleSolution);
		lti.setSampleDocumentation(sampleDocumentation);
		return lti;
	}
	
	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManager#createAndPersistLivingTaskInstance(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date)
	 */
	@Override
	public LivingTaskInstance createAndPersistLivingTaskInstance(String signature, String taskText, String internalTaskText, String sampleSolution, String sampleDocumentation, Date creationDate) {
		LivingTaskInstance lti = createLivingTaskInstance(signature, taskText, internalTaskText, sampleSolution, sampleDocumentation, creationDate);
		saveLivingTaskInstance(lti);
		return lti;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManager#deleteLivingTaskInstance(de.htwk.autolat.LivingTaskInstance.LivingTaskInstance)
	 */
	@Override
	public boolean deleteLivingTaskInstance(LivingTaskInstance livingTaskInstance) {
		try{
			DBFactory.getInstance().deleteObject(livingTaskInstance);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManager#loadLivingTaskInstanceByID(long)
	 */
	@Override
	public LivingTaskInstance loadLivingTaskInstanceByID(long ID) {
		return (LivingTaskInstance)DBFactory.getInstance().loadObject(LivingTaskInstanceImpl.class, ID);
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManager#saveLivingTaskInstance(de.htwk.autolat.LivingTaskInstance.LivingTaskInstance)
	 */
	@Override
	public void saveLivingTaskInstance(LivingTaskInstance livingTaskInstance) {
		DBFactory.getInstance().saveObject(livingTaskInstance);
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManager#updateLivingTaskInstance(de.htwk.autolat.LivingTaskInstance.LivingTaskInstance)
	 */
	@Override
	public void updateLivingTaskInstance(LivingTaskInstance livingTaskInstance) {
		DBFactory.getInstance().updateObject(livingTaskInstance);
	}
	
	/**
	 * Gets the single instance of LivingTaskInstanceManagerImpl.
	 * 
	 * @return single instance of LivingTaskInstanceManagerImpl
	 */
	public static LivingTaskInstanceManagerImpl getInstance() {
		return INSTANCE;
	}

	public String parseDocumentation(String xmlDocumentation) {
		try {
			XMLParser parser = new XMLParser();
			OutputObject parseResult = parser.parseString(xmlDocumentation);
			return parseResult.toString();
		} catch (Exception e) {
			return "invalid xml";
		}
	}
}
