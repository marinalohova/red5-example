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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.red5.logging.Red5LoggerFactory;
import org.red5.webapps.admin.UserDatabase;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;


/**
 * Simple DAO for manipulation of the user database.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class UserDAO {

	private static Logger log = Red5LoggerFactory.getLogger(UserDAO.class, "admin");
	
	public static boolean addUser(String username, String hashedPassword) {
		boolean result = false;
				
		Connection conn = null;
		PreparedStatement stmt = null;
		try {          
			conn = UserDatabase.getConnection();
			//make a statement
			stmt = conn.prepareStatement("INSERT INTO APPUSER (username, password, enabled) VALUES (?, ?, 'enabled')");
			stmt.setString(1, username);
			stmt.setString(2, hashedPassword);
			log.debug("Add user: {}", stmt.execute());			
			//add role
			stmt = conn.prepareStatement("INSERT INTO APPROLE (username, authority) VALUES (?, 'ROLE_SUPERVISOR')");
			stmt.setString(1, username);
			log.debug("Add role: {}", stmt.execute());			
			//
			result = true;
		} catch (Exception e) {
			log.error("Error connecting to db", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				UserDatabase.recycle(conn);
			}
		}
		return result;
	}
	
	public static AdminUserDetails getUser(String username) {
		AdminUserDetails details = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = UserDatabase.getConnection();
			//make a statement
			stmt = conn.prepareStatement("SELECT * FROM APPUSER WHERE username = ?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				log.debug("User found");			
				details = new AdminUserDetails();
				details.setEnabled("enabled".equals(rs.getString("enabled")));
				details.setPassword(rs.getString("password"));
				details.setUserid(rs.getInt("userid"));
				details.setUsername(rs.getString("username"));
				//
				rs.close();
				//get role				
				stmt = conn.prepareStatement("SELECT authority FROM APPROLE WHERE username = ?");
				stmt.setString(1, username);
				rs = stmt.executeQuery();
				if (rs.next()) {
	            	GrantedAuthority[] authorities = new GrantedAuthority[1];
	            	authorities[0] = new GrantedAuthorityImpl(rs.getString("authority"));
	            	details.setAuthorities(authorities);
	            	//
	            	//if (daoAuthenticationProvider != null) {
    	            	//User usr = new User(username, details.getPassword(), true, true, true, true, authorities);
    	            	//daoAuthenticationProvider.getUserCache().putUserInCache(usr);					
	            	//}
				}			
			}
			rs.close();
		} catch (Exception e) {
			log.error("Error connecting to db", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				UserDatabase.recycle(conn);
			}
		}
		return details;
	}
	
}
