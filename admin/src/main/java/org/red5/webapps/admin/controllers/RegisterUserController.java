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

package org.red5.webapps.admin.controllers;

import javax.servlet.ServletException;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

@SuppressWarnings("deprecation")
public class RegisterUserController extends SimpleFormController {

	private static Logger log = Red5LoggerFactory.getLogger(RegisterUserController.class, "admin");

	private static DaoAuthenticationProvider daoAuthenticationProvider;

	private static UserDetailsService userDetailsService;

	public ModelAndView onSubmit(Object command) throws ServletException {
		log.debug("onSubmit {}", command);
		UserDetails userDetails = (UserDetails) command;
		String username = userDetails.getUsername();
		String password = userDetails.getPassword();
		log.debug("User details: username={} password={}", username, password);
		try {
			// register user here
			if (!((JdbcUserDetailsManager) userDetailsService).userExists(username)) {
				GrantedAuthority[] auths = new GrantedAuthority[1];
				auths[0] = new GrantedAuthorityImpl("ROLE_SUPERVISOR");
				User usr = new User(username, password, true, true, true, true, auths);
				((JdbcUserDetailsManager) userDetailsService).createUser(usr);
				if (((JdbcUserDetailsManager) userDetailsService).userExists(username)) {
					//setup security user stuff and add them to the current "cache" and current user map	
					daoAuthenticationProvider.getUserCache().putUserInCache(usr);
				} else {
					log.warn("User registration failed for: {}", username);
				}
			} else {
				log.warn("User {} already exists", username);
			}
		} catch (Exception e) {
			log.error("Error during registration", e);
		}
		return new ModelAndView(new RedirectView(getSuccessView()));
	}

	public void setDaoAuthenticationProvider(DaoAuthenticationProvider value) {
		RegisterUserController.daoAuthenticationProvider = value;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		RegisterUserController.userDetailsService = userDetailsService;
	}

}