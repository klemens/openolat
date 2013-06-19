package de.unileipzig.shibboleth;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.registration.RegistrationManager;
import org.olat.user.UserManager;

/**
 * Simple dispatcher that logs the user in (and creates it if not existent).
 * Tries to use as much properties as possible, but at least a username is required.
 * @author Klemens Schölhorn
 */
public class SimpleShibbolethDispatcher implements Dispatcher {
	
	/**
	 * Identifier as authentication provider
	 */
	public static final String PROVIDER_SSHIB = "SShib";
	
	/**
	 * Mapping of Shibboleth attribute names (keys) to openolat user properties (values).
	 * Use the following values:
	 * <dl>
	 * <dt>username <b>(required)</b></dt>
	 *   <dd>The unique username of the user</dd>
	 * <dt>firstname</dt>
	 *   <dd>The first name of the user</dd>
	 * <dt>lastname</dt>
	 *   <dd>The last name of the user</dd>
	 * <dt>email</dt>
	 *   <dd>The institutional email of the user</dd>
	 * <dt>identifier</dt>
	 *   <dd>The institutional identifier of the user</dd>
	 * <dt>subject</dt>
	 *   <dd>The institutional subject of the user</dd>
	 * </dl>
	 */
	private Properties mapping;
	
	/**
	 * Contains the actual user properties
	 */
	private Properties userAttributes;
	
	private OLog log;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		if(log == null)
			log = Tracing.createLoggerFor(this.getClass());
		if(userAttributes == null)
			userAttributes = new Properties();
		
		// parse http headers
		parseAttributes(request);
		
		// check if username present
		if(userAttributes.getProperty("username", "").isEmpty()) {
			log.error("no username was supplied by shibboleth, check your configuration");
			DispatcherAction.redirectToDefaultDispatcher(response);
			return;
		}
		String username = userAttributes.getProperty("username");
		
		// create the user request, send bad request for invalid uris
		// see other openolat dispatchers
		UserRequest ureq = null;
		try{
			ureq = new UserRequestImpl(uriPrefix, request, response);
		}catch(NumberFormatException nfe){
			log.error("Bad Request " + request.getPathInfo());
			DispatcherAction.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		// find shibboleth authentication
		Authentication auth = BaseSecurityManager.getInstance().findAuthenticationByAuthusername(username, PROVIDER_SSHIB);
		if (auth == null) {
			// check if username exists in system
			Identity identity = BaseSecurityManager.getInstance().findIdentityByName(username);
			
			if(identity == null) {
				// register user
				log.info("First login of user " + username + " using shibboleth");
				Identity newUser = registerUser(username);
				loginUser(newUser, ureq);
			} else {
				// migrate user to shibboleth authentication
				log.info("Migrating user " +  username + " to shibboleth auth and logging in");
				migrateUser(identity, username);
				loginUser(identity, ureq);
			}
		} else {
			// login the user the normal way
			log.info("user login via shibboleth");
			loginUser(auth.getIdentity(), ureq);
		}
		
		// redirect to home
		MediaResource mr = ureq.getDispatchResult().getResultingMediaResource();
		if (!(mr instanceof RedirectMediaResource)) {
			log.error("got wrong type of MediaResource");
			DispatcherAction.redirectToDefaultDispatcher(response);
			return;
		}
		
		RedirectMediaResource rmr = (RedirectMediaResource) mr;
		rmr.prepare(response);
	}
	
	/**
	 * Logs the user in and redirects on error case
	 */
	private boolean loginUser(Identity identity, UserRequest ureq) {
		// try to login
		int loginStatus = AuthHelper.doLogin(identity, PROVIDER_SSHIB, ureq);
		
		// redirect in error case
		if(loginStatus != AuthHelper.LOGIN_OK) {
			if(loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherAction.redirectToServiceNotAvailable(ureq.getHttpResp());
			} else {
				log.error("could not login user using shibboleth");
				DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp()); // login screen
			}
			
			return false;
		}
		
		// set user as active
		UserDeletionManager.getInstance().setIdentityAsActiv(identity);

		return true;
	}
	
	/**
	 * Registers the new user and fills in the supplied properties
	 */
	private Identity registerUser(String username) {
		BaseSecurity secMgr = BaseSecurityManager.getInstance();

		// if no names are present, user empty names
		String firstName = userAttributes.getProperty("firstname", "");
		String lastName = userAttributes.getProperty("lastname", "");
		// if no email is present, generate one from username
		String email = userAttributes.getProperty("email", username + "@studserv.uni-leipzig.de");
		
		// create user
		User user = UserManager.getInstance().createUser(firstName, lastName, email);
		
		// apply the user attributes of present
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, email);
		if(userAttributes.containsKey("identifier"))
			user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, userAttributes.getProperty("identifier"));
		if(userAttributes.containsKey("subject"))
			user.setProperty(UserConstants.STUDYSUBJECT, userAttributes.getProperty("subject"));
		
		// persist user
		Identity identity = secMgr.createAndPersistIdentityAndUser(username, user, PROVIDER_SSHIB, username, null);
		
		// add to group user
		secMgr.addIdentityToSecurityGroup(identity, secMgr.findSecurityGroupByName(Constants.GROUP_OLATUSERS));
		
		// emulate disclaimer confirmation
		RegistrationManager.getInstance().setHasConfirmedDislaimer(identity);

		return identity;
	}
	
	/**
	 * Migrates user to shibboleth authentication. It will still be possible to login using the old method
	 * @param identity
	 * @param username
	 */
	private void migrateUser(Identity identity, String username) {
		// create additional authentication method for user
		BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, PROVIDER_SSHIB, username, null);
		
		// TODO here we could update the user attributes with shibboleth attributes
	}
	
	/**
	 * Find the supplied username by iterating over http attributes
	 */
	private void parseAttributes(HttpServletRequest request) {
		for(Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
			String attributeName = e.nextElement();
			
			if(mapping.containsKey(attributeName)) {
				userAttributes.setProperty(mapping.getProperty(attributeName), request.getHeader(attributeName));
			}
		}
	}
}
