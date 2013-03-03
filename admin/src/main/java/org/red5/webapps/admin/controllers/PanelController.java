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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.red5.logging.Red5LoggerFactory;
import org.red5.webapps.admin.controllers.service.AdminUserDetails;
import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PanelController implements Controller {

	private static Logger log = Red5LoggerFactory.getLogger(PanelController.class, "admin");

	private static UserDetailsService userDetailsService;

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		log.debug("handle request");

		//if there arent any users then send to registration
		if (((JdbcUserDetailsManager) userDetailsService).userExists("admin")) {
			log.debug("Creating adminPanel");
			return new ModelAndView("panel");
		} else {
			//check for model
			log.debug("{}", ToStringBuilder.reflectionToString(request));
			if (request.getMethod().equalsIgnoreCase("POST")) {
				//no model then redirect...
				log.debug("Redirecting to register with user details");
				return new ModelAndView("register");
			} else {
				//no model then redirect...
				log.debug("Redirecting to register");
				AdminUserDetails userDetails = new AdminUserDetails();
				userDetails.setUsername("admin");
				return new ModelAndView("register", "userDetails", userDetails);
			}
		}
	}

	public ModelAndView doRequest(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView();
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		PanelController.userDetailsService = userDetailsService;
	}

}
