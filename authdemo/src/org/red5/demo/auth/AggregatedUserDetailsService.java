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

import java.util.Map;
import java.util.Map.Entry;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Provides user details when requested by a users name. Any and all UserService implementations are queried.
 * 
 * @author Paul Gregoire (paul@infrared5.com)
 */
public class AggregatedUserDetailsService implements UserDetailsService, ApplicationContextAware {

	private static Logger log = Red5LoggerFactory.getLogger(AggregatedUserDetailsService.class, "authdemo");

	private ApplicationContext applicationContext;

	/**
	 * Retrieves a user record containing the user's credentials and access. 
	 */
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		log.debug("loadUserByUsername: {}", userName);
		UserDetails userDetails = null;
		// get all the user details services
		Map<String, UserDetailsService> userDetailsServiceMap = applicationContext.getBeansOfType(UserDetailsService.class);
		log.debug("User details services: {}", userDetailsServiceMap);
		// loop through the map for any service providing a lookup, ensure that we skip 'this' class
		for (Entry<String, UserDetailsService> entry : userDetailsServiceMap.entrySet()) {
			UserDetailsService val = entry.getValue();
			if (val instanceof AggregatedUserDetailsService) {
				continue;
			}
			UserDetails tmp = val.loadUserByUsername(userName);
			if (tmp != null) {
				log.debug("User details found in {}", entry.getKey());
				userDetails = tmp;
				break;
			}
		}
		if (userDetails == null) {
			throw new UsernameNotFoundException("User " + userName + " not found");
		}
		return userDetails;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}