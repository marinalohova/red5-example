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

package org.red5.webapps.admin.controllers.service;

import org.apache.commons.lang3.StringUtils;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class UserDetailsValidator implements Validator {

	private static Logger log = Red5LoggerFactory.getLogger(UserDetailsValidator.class, "admin");

	private int minLength = 4;

	@SuppressWarnings("rawtypes")
	public boolean supports(Class clazz) {
		return AdminUserDetails.class.equals(clazz);
	}

	public void validate(Object obj, Errors errors) {
		log.debug("validate");
		AdminUserDetails ud = (AdminUserDetails) obj;
		if (ud == null) {
			log.debug("User details were null");
			errors.rejectValue("username", "error.not-specified", null, "Value required.");
		} else {
			log.debug("User details were null");
			if (StringUtils.isEmpty(ud.getUsername())) {
				errors.rejectValue("username", "error.missing-username", new Object[] {}, "Username Required.");
			}
			if (StringUtils.isEmpty(ud.getPassword())) {
				errors.rejectValue("password", "error.missing-password", new Object[] {}, "Password Required.");
			} else if (ud.getPassword().length() < minLength) {
				errors.rejectValue("password", "error.too-low", new Object[] { new Integer(minLength) }, "Password Length Is Too Small.");
			}
		}
	}

	public void setMinLength(int i) {
		minLength = i;
	}

	public int getMinLength() {
		return minLength;
	}
}