package de.unileipzig.xman.schedule;

import org.olat.core.commons.scheduler.JobWithDB;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * This class gets called by the quartz scheduler every 24 hours.
 * It's bean is defined in serviceconfig.org.olat.core.commons.
 * scheduler._spring. You can adjust the execution time with 
 * cronjob syntax in the xml file.
 * 
 * @author gerb
 */
public class ESFMailJob { //extends JobWithDB {

	/**
	 * Every 24 hours is checked if there are more than zero electronic student
	 * files with the status "non-validated". If so every user with the role
	 * exam manager gets an email with the number of those esfs.
	 * A log entry is also created.
	 * 
	 * @see org.olat.core.commons.scheduler.JobWithDB#executeWithDB(org.quartz.JobExecutionContext)
	 */
	public void executeWithDB(JobExecutionContext arg0)
			throws JobExecutionException {
		
//		OLog log = Tracing.createLoggerFor(this.getClass());
//		
//		int count = ElectronicStudentFileManager.getInstance().getNumberOfEsfWithNonDefaultStudyPath();
//		
//		// send only emails if there are esf's to validate
//		if ( count > 0 ) {
//
//			PermissionOnResourceable[] permissions = {new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_EXAMADMIN)};
//			List<Identity> identityList = ManagerFactory.getManager().getIdentitiesByPowerSearch(null, null, true, null, permissions, null, null, null, null);
//			
//			// every exam admin should get an email
//			for (Identity identity : identityList ) {
//				
//				Translator translator = Util.createPackageTranslator(ESFMailJob.class, new Locale(identity.getUser().getPreferences().getLanguage()));
//			
//				MailManager.getInstance().sendEmail(
//						translator.translate("ESFMailJob.informExamOffice.subject"), 
//						translator.translate("ESFMailJob.informExamOffice.body", 
//								new String[] { 
//									// name of the identity
//									identity.getName(),
//									// amount of files to be validated
//									new Integer(count).toString(),
//								} ) , 
//						identity
//				);
//				log.info("An email with " + count + " ESF to validate was sent to " + identity.getName() + "!" );
//			}
//		}
	}
}
