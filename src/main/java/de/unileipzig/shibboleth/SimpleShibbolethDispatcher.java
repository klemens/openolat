package de.unileipzig.shibboleth;

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
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.control.ChiefController;
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
	public static final String PROVIDER_SSHIB = "SplShib";
	
	/**
	 * Mapping of Shibboleth attribute names (keys) to openolat user properties (values).
	 * Use the following values: (set by spring)
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
	 * If login using shibboleth is enabled
	 */
	private boolean enabled;
	
	/**
	 * If migration of existing users is enabled
	 */
	private boolean migrate;
	
	/**
	 * The template used to generate an email from the username
	 * (using String.format with the username string being the only parameter)
	 */
	private String emailTemplate;
	
	private OLog log;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		UserRequest ureq = new UserRequestImpl(uriPrefix, request, response);

		if(log == null)
			log = Tracing.createLoggerFor(this.getClass());
		
		if(!enabled) {
			log.error("shibboleth login attempted although not enabled");
			showError(ureq, "Shibboleth is not enabled, please contact admin.", null);
			return;
		}
		
		// parse http headers
		Properties userAttributes = parseAttributes(request);
		
		// check if username present
		if(userAttributes.getProperty("username", "").isEmpty()) {
			log.error("shibboleth login failed: no username was supplied by shibboleth, check your configuration");
			showError(ureq, "Could not transfer username correctly, please try again!", null);
			return;
		}
		String username = userAttributes.getProperty("username");
		String affiliation = userAttributes.getProperty("affiliation", "unknown affiliation");
		
		// find shibboleth authentication
		Authentication auth = BaseSecurityManager.getInstance().findAuthenticationByAuthusername(username, PROVIDER_SSHIB);
		if(auth == null) {
			// check if username exists in system
			Identity identity = BaseSecurityManager.getInstance().findIdentityByName(username);
			
			if(identity == null) {
				// register user
				log.info("first login of user '" + username + "' (" + affiliation + ") using shibboleth");
				Identity newUser = registerUser(username, userAttributes);
				if(!loginUser(newUser, ureq)) {
					showError(ureq, "Login failed", null);
					return;
				}
			} else {
				// migrate user to shibboleth authentication
				if(migrate) {
					log.info("migrating user '" +  username + "' (" + affiliation + ") to shibboleth auth and logging in");
					migrateUser(identity, username);
					if(!loginUser(identity, ureq)) {
						showError(ureq, "Login failed", null);
						return;
					}
				} else {
					log.error("existing username '" + username + "' (" + affiliation + ") but migration to shibboleth not enabled");
					showError(ureq, "Your username already exists, but migration to shibboleth is disabled.", null);
					return;
				}
			}
		} else {
			// login the user the normal way
			log.info("user '" + username + "' (" + affiliation + ") logged in via shibboleth");
			if(!loginUser(auth.getIdentity(), ureq)) {
				showError(ureq, "Login failed", null);
				return;
			}
		}
		
		// redirect to home
		MediaResource mr = ureq.getDispatchResult().getResultingMediaResource();
		if (!(mr instanceof RedirectMediaResource)) {
			log.error("got wrong type of MediaResource");
			DispatcherModule.redirectToDefaultDispatcher(response);
			return;
		}
		
		RedirectMediaResource rmr = (RedirectMediaResource) mr;
		rmr.prepare(response);
	}
	
	/**
	 * Logs the user in
	 */
	private boolean loginUser(Identity identity, UserRequest ureq) {
		// try to login
		int loginStatus = AuthHelper.doLogin(identity, PROVIDER_SSHIB, ureq);

		// set user as active
		UserDeletionManager.getInstance().setIdentityAsActiv(identity);

		return loginStatus == AuthHelper.LOGIN_OK;
	}
	
	/**
	 * Registers the new user and fills in the supplied properties
	 */
	private Identity registerUser(String username, Properties userAttributes) {
		BaseSecurity secMgr = BaseSecurityManager.getInstance();

		// if no names are present, user empty names
		String firstName = userAttributes.getProperty("firstname", "");
		String lastName = userAttributes.getProperty("lastname", "");
		// if no email is present, generate one from username
		String email = userAttributes.getProperty("email", String.format(emailTemplate, username));
		
		// create user
		User user = UserManager.getInstance().createUser(firstName, lastName, email);
		
		// apply the user attributes of present
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, email);
		if(userAttributes.containsKey("identifier")) {
			user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, userAttributes.getProperty("identifier"));
		}
		if(userAttributes.containsKey("subject")) {
			user.setProperty(UserConstants.STUDYSUBJECT, userAttributes.getProperty("subject"));
		}
		
		// persist user
		Identity identity = secMgr.createAndPersistIdentityAndUser(username, null, user, PROVIDER_SSHIB, username);
		
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
		BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, PROVIDER_SSHIB, username, null, null);
		
		// TODO here we could update the user attributes with shibboleth attributes
	}
	
	/**
	 * Find the supplied username (and other properties) form JavaEE request
	 */
	private Properties parseAttributes(HttpServletRequest request) {
		Properties userAttributes = new Properties();

		for(String propShib : mapping.stringPropertyNames()) {
			String value = (String) request.getAttribute(propShib);
			
			if(value != null) {
				userAttributes.setProperty(mapping.getProperty(propShib), value);
			}
		}
		
		return userAttributes;
	}

	/**
	 * @see SimpleShibbolethErrorController#SimpleShibbolethErrorController(UserRequest, String, String)
	 */
	private void showError(UserRequest ureq, String message, String detail) {
		ChiefController controller = new SimpleShibbolethErrorController(ureq, message, detail);
		controller.getWindow().dispatchRequest(ureq, true);
	}

	/**
	 * Spring setters and getters
	 */
	public void setMapping(Properties mapping) {
		this.mapping = mapping;
	}
	public Properties getMapping() {
		return mapping;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean getEnabled() {
		return enabled;
	}
	public void setMigrate(boolean migrate) {
		this.migrate = migrate;
	}
	public boolean getMigrate() {
		return migrate;
	}
	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}
	public String getEmailTemplate() {
		return emailTemplate;
	}
}
