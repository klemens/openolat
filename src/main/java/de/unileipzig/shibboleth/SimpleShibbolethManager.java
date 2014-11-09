package de.unileipzig.shibboleth;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.olat.core.helpers.Settings;
import org.springframework.beans.factory.annotation.Value;

public class SimpleShibbolethManager {
	@Value("${simpleShibboleth.dispatcherPath:/shib/}")
	private String dispatcherPath;

	@Value("${simpleShibboleth.sessionInitiatorPath:/Shibboleth.sso/Login}")
	private String sessionInitiatorPath;

	private List<IdentityProvider> identityProviders = new ArrayList<IdentityProvider>();

	protected SimpleShibbolethManager() {
		_beanInstance = this;
	}

	/**
	 * Return the /path/ at which the dispatcher lives
	 */
	public String getDispatcherPath() {
		return dispatcherPath;
	}

	/**
	 * Return a list of the names of all identity providers
	 */
	public List<String> getAvailableIdentityProviders() {
		List<String> result = new ArrayList<String>();
		for(IdentityProvider ip : identityProviders) {
			result.add(ip.name);
		}
		return result;
	}

	/**
	 * Create a url that contains the sp session initiator,
	 * the ip and the return url (build using the dispatcher path).
	 * This can be used to redirect the user to his login page.
	 */
	public String getLoginUrl(String identityProviderName) {
		String returnPath = Settings.getServerContextPathURI() + getDispatcherPath();
		String ipPath = null;
		for(IdentityProvider ip : identityProviders) {
			if(ip.name.equals(identityProviderName)) {
				ipPath = ip.url;
			}
		}
		if(ipPath == null) throw new IllegalArgumentException("given ip '" + identityProviderName + "' was not found");

		return UriBuilder.fromUri(sessionInitiatorPath)
						 .queryParam("entityID", ipPath)
						 .queryParam("target", returnPath).build().toString();
	}

	/**
	 * This is only needed because there is currently no way
	 * to inject the manager into the AuthenticationController,
	 * because the factory used to create it (AuthenticationProvider)
	 * does not support injection and is final (only private constructors).
	 */
	static SimpleShibbolethManager _beanInstance;
	public static SimpleShibbolethManager getInstance() {
		return _beanInstance;
	}

	/**
	 * These are the datastructures used for the different identity
	 * provides and their specific settings. Also used for xml
	 * serialization using xstream.
	 */
	public static class IdentityProvider {
		public String name;
		public String url;
		public String scope;
		public List<Attribute> attributes;
		public List<Affiliation> affiliations;
		public List<Migration> migrations;
	}
	public static class Attribute {
		Type type;
		public String name;
		public String prefix;
		public enum Type {
			USERNAME("username"),
			FIRST_NAME("firstName"),
			LAST_NAME("lastName"),
			EMAIL("email"),
			IDENTIFIER("identifier"),
			STUDY_SUBJECT("studySubject");
			private final String text;
			private Type(final String text) { this.text = text; }
			@Override public String toString() { return text; }
			public static Type fromString(String text) {
				for(Type t : Type.values()) if(t.toString().equals(text)) return t;
				return null;
			}
		}
	}
	public static class Affiliation {
		public Role role;
		public String emailTemplate;
		public enum Role {
			FACULTY("faculty"),
			STUDENT("student"),
			STAFF("staff"),
			ALUM("alum"),
			MEMBER("member"),
			AFFILIATE("affiliate"),
			EMPLOYEE("employee"),
			LIBRARY_WALK_IN("library-walk-in");
			private final String text;
			private Role(final String text) { this.text = text; }
			@Override public String toString() { return text; }
			public static Role fromString(String text) {
				for(Role r : Role.values()) if(r.toString().equals(text)) return r;
				return null;
			}
		}
	}
	public static class Migration {
		String pattern;
	}
}
