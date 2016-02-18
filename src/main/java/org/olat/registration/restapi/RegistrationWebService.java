/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.registration.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getLocale;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.registration.RegistrationController;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.TemporaryKey;
import org.olat.user.UserManager;


/**
 * 
 * Description:<br>
 * Web service to trigger the registration process
 * 
 * <P>
 * Initial Date:  14 juil. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("registration")
public class RegistrationWebService {
	
	private OLog log = Tracing.createLoggerFor(RegistrationWebService.class);

	private static final String SEPARATOR = "____________________________________________________________________\n";
	
	/**
	 * Register with the specified email
   * @response.representation.200.doc Registration successful
   * @response.representation.304.doc Already registered, HTTP-Header location set to redirect
   * @param email The email address
   * @param request The HTTP Request
	 * @return
	 */
	@POST
	public Response registerPost(@FormParam("email") String email, @Context HttpServletRequest request) {
		return register(email, request);
	}
	
	/**
	 * Register with the specified email
   * @response.representation.200.doc Registration successful
   * @response.representation.304.doc Already registered, HTTP-Header location set to redirect
   * @param email The email address
   * @param request The HTTP Request
	 * @return
	 */
	@PUT
	public Response register(@QueryParam("email") String email, @Context HttpServletRequest request) {
		if (!CoreSpringFactory.getImpl(RegistrationModule.class).isSelfRegistrationEnabled()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		ResponseBuilder response;
		Locale locale = getLocale(request);
		Translator translator = getTranslator(locale);
		
		MailManager mailM = CoreSpringFactory.getImpl(MailManager.class);
		UserManager userManager = UserManager.getInstance();
		RegistrationManager rm = CoreSpringFactory.getImpl(RegistrationManager.class);
		
		boolean foundUser = userManager.userExist(email);
		// get remote address

		String serverpath = Settings.getServerContextPathURI();
		if (foundUser) {
			//redirect
			URI redirectUri = UriBuilder.fromUri(Settings.getServerContextPathURI()).build();
			response = Response.ok().status(Status.NOT_MODIFIED).location(redirectUri);
		} else {
			String ip = request.getRemoteAddr();
			TemporaryKey tk = rm.loadTemporaryKeyByEmail(email);
			if (tk == null) {
				tk = rm.createTemporaryKeyByEmail(email, ip, RegistrationManager.REGISTRATION);
			}
			String today = DateFormat.getDateInstance(DateFormat.LONG, locale).format(new Date());
			String[] bodyAttrs = new String[] {
					serverpath,
					tk.getRegistrationKey(),
					I18nManager.getInstance().getLocaleKey(locale)
			};
			String[] whereFromAttrs = new String [] { serverpath, today, ip };
			String body = translator.translate("reg.body", bodyAttrs) + SEPARATOR + translator.translate("reg.wherefrom", whereFromAttrs);
			try {
				MailBundle bundle = new MailBundle();
				bundle.setTo(email);
				bundle.setContent(translator.translate("reg.subject"), body);
				MailerResult result = mailM.sendExternMessage(bundle, null, true);
				if (result.isSuccessful()) {
					response = Response.ok();
				} else {
					response = Response.serverError().status(Status.INTERNAL_SERVER_ERROR);
				}
			} catch (Exception e) {
				response = Response.serverError().status(Status.INTERNAL_SERVER_ERROR);
				log.error("", e);
			}
		}
		
		return response.build();
	}
	
	private Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(RegistrationController.class, locale);
	}

}
