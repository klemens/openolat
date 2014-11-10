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
import org.springframework.beans.factory.annotation.Autowired;

import de.unileipzig.shibboleth.SimpleShibbolethManager.Affiliation;
import de.unileipzig.shibboleth.SimpleShibbolethManager.Attribute;
import de.unileipzig.shibboleth.SimpleShibbolethManager.IdentityProvider;

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

	@Autowired
	private SimpleShibbolethManager manager;

	private OLog log;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		UserRequest ureq = new UserRequestImpl(uriPrefix, request, response);

		if(log == null)
			log = Tracing.createLoggerFor(this.getClass());
		
		if(!manager.isEnabled()) {
			log.error("shibboleth login attempted although not enabled");
			showError(ureq, "Shibboleth is not enabled.", null);
			return;
		}

		// first, we have to find the identity provider the user came from
		IdentityProvider identityProvider = manager.findIdentityProvider(request);
		if(identityProvider == null) {
			log.error("no matching identity provider could be found for this request");
			showError(ureq, "No identity provider is configured for your request.", null);
			return;
		}

		// next we have to check if the user's affiliation is allowed to log in
		String affiliationString = (String) request.getAttribute(identityProvider.affiliationKey);
		Affiliation affiliation = manager.findAffiliation(affiliationString, identityProvider);
		if(affiliation == null) {
			log.error("no matching affiliation found in " + identityProvider.scope + " for: " + affiliationString);
			showError(ureq, "You are not allowed to login with your affiliation.", "Your affiliation is: " + affiliationString);
			return;
		}
		
		// get shibboleth attributes from request
		Properties userAttributes = parseAttributes(request, identityProvider);
		
		// check if username present
		if(userAttributes.getProperty(Attribute.Type.USERNAME.toString(), "").isEmpty()) {
			log.error("no username was supplied by shibboleth, check your configuration");
			showError(ureq, "Your username was not transferred correctly, please try again!", null);
			return;
		}
		String username = userAttributes.getProperty(Attribute.Type.USERNAME.toString());
		
		// find shibboleth authentication
		Authentication auth = BaseSecurityManager.getInstance().findAuthenticationByAuthusername(username, PROVIDER_SSHIB);
		if(auth == null) {
			// check if username exists in system
			Identity identity = BaseSecurityManager.getInstance().findIdentityByName(username);
			
			if(identity == null) {
				// register user
				log.info("first login of user '" + username + "' using shibboleth");
				Identity newUser = registerUser(username, userAttributes, affiliation);
				if(!loginUser(newUser, ureq)) {
					showError(ureq, "Login failed", null);
					return;
				}
			} else {
				// try to find a migration path
				if(manager.canMigrate(username, identityProvider)) {
					log.info("migrating user '" +  username + "' to shibboleth auth and logging in");
					migrateUser(identity, username);
					if(!loginUser(identity, ureq)) {
						showError(ureq, "Login failed", null);
						return;
					}
				} else {
					log.error("existing user '" + username + "' but no migration path found");
					showError(ureq, "Your username already exists, but no valid migration path could be found.", "Username: " + username);
					return;
				}
			}
		} else {
			// login the user the normal way
			log.info("user '" + username + "' logged in via shibboleth");
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
	private Identity registerUser(String username, Properties userAttributes, Affiliation affiliation) {
		BaseSecurity secMgr = BaseSecurityManager.getInstance();

		// if no names are present, user empty names
		String firstName = userAttributes.getProperty(Attribute.Type.FIRST_NAME.toString(), "");
		String lastName = userAttributes.getProperty(Attribute.Type.LAST_NAME.toString(), "");
		String email = userAttributes.getProperty(Attribute.Type.EMAIL.toString(), "");
		// if no email is present, try to generate one from username
		if(email.isEmpty() && !affiliation.emailTemplate.isEmpty()) {
			email = affiliation.emailTemplate.replace("{" + Attribute.Type.FIRST_NAME + "}", firstName)
											 .replace("{" + Attribute.Type.LAST_NAME + "}", lastName)
											 .replace("{" + Attribute.Type.USERNAME + "}", username);
		}
		
		// create user
		User user = UserManager.getInstance().createUser(firstName, lastName, email);
		
		// apply the user attributes of present
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, email);
		if(userAttributes.containsKey(Attribute.Type.IDENTIFIER.toString())) {
			user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, userAttributes.getProperty(Attribute.Type.IDENTIFIER.toString()));
		}
		if(userAttributes.containsKey(Attribute.Type.STUDY_SUBJECT.toString())) {
			user.setProperty(UserConstants.STUDYSUBJECT, userAttributes.getProperty(Attribute.Type.STUDY_SUBJECT.toString()));
		}
		
		// persist user
		Identity identity = secMgr.createAndPersistIdentityAndUser(username, user, PROVIDER_SSHIB, username);
		
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
	}
	
	/**
	 * Find the supplied username (and other properties) form JavaEE request
	 */
	private Properties parseAttributes(HttpServletRequest request, IdentityProvider identityProvider) {
		Properties userAttributes = new Properties();

		for(Attribute attribute : identityProvider.attributes) {
			String value = (String) request.getAttribute(attribute.name);
			if(value != null) {
				userAttributes.setProperty(attribute.type.toString(), value);
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
}
