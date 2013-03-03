/*
 * RED5 Open Source Flash Server - http://code.google.com/p/red5/
 * 
 * Copyright 2006-2012 by respective authors (see below). All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.red5.demo.auth;

import java.util.Arrays;
import java.util.Collection;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.IApplication;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class Application extends MultiThreadedApplicationAdapter implements ApplicationContextAware {

	private static Logger log = Red5LoggerFactory.getLogger(Application.class, "authdemo");

	private ApplicationContext applicationContext;

	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		return super.connect(conn, scope, params);
	}

	@Override
	public void disconnect(IConnection conn, IScope scope) {
		super.disconnect(conn, scope);
	}

	@Override
	public boolean appStart(IScope app) {
		// authentication management
		ProviderManager authManager = (ProviderManager) applicationContext.getBean("authManager");
		log.info("Available auth providers: {}", authManager.getProviders().size());
		if (authManager.isEraseCredentialsAfterAuthentication()) {
			log.info("Provider set to erase creds, changing to NOT do this");
			authManager.setEraseCredentialsAfterAuthentication(false);
		}
		// add an authentication listener
		addListener((IApplication) applicationContext.getBean("authHandler"));
		// hit the super class
		return super.appStart(app);
	}

	@Override
	public void appStop(IScope app) {
		super.appStop(app);
	}

	public static boolean isAuthorized(String... roles) {
		// get the auth from the security context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			UserDetails deets = (UserDetails) auth.getPrincipal();
			log.debug("enabled: {}", deets.isEnabled());
			Collection<GrantedAuthority> granted = deets.getAuthorities();
			for (GrantedAuthority authority : granted) {
				if (Arrays.asList(roles).contains(authority.getAuthority())) {
					log.debug("Authorized");
					return true;
				}
			}
		}
		log.debug("Not Authorized. User has not been granted any of: {}", roles);
		return false;
	}

	public String helloAdmin() {
		if (isAuthorized("ROLE_ADMIN")) {
			return "Hello Admin!";
		}
		return "You are not authorized";
	}
	
	public String helloUser() {
		if (isAuthorized("ROLE_USER")) {
			return "Hello User, want to play a game?";
		}
		return "You are not authorized";		
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
