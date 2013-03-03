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

package org.red5.webapps.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

/**
 * Database setup for admin.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class UserDatabase {

	private static Logger log = Red5LoggerFactory.getLogger(UserDatabase.class, "admin");		
	
	private static String driverClass = "org.h2.Driver";

	private static String jdbcUrl = "jdbc:h2:admin";

	/**
	 * Returns a connection to the db.
	 * 
	 * @return connection
	 */
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName(driverClass);
			conn = DriverManager.getConnection(jdbcUrl);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * Clean up connection.
	 * 
	 * @param conn
	 */
	public static void recycle(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
	}

	/**
	 * Init the db
	 */
	public void init() {
		Connection conn = getConnection();
		try {
			Statement st = conn.createStatement();
			if (st.execute("CREATE TABLE IF NOT EXISTS APPUSER(userid INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(16), password VARCHAR(36), enabled VARCHAR(7) NOT NULL)")) {
				log.debug("Create user table executed");
			}
			if (st.execute("CREATE TABLE IF NOT EXISTS APPROLE(username VARCHAR(16) NOT NULL PRIMARY KEY, authority VARCHAR(16) NOT NULL)")) {
				log.debug("Create role table executed");
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			recycle(conn);
		}
	}	

	public void shutdown() {
	}

	/**
	 * @param driverClass the driverClass to set
	 */
	public void setDriverClass(String driverClass) {
		UserDatabase.driverClass = driverClass;
	}

	/**
	 * @param jdbcUrl the jdbcUrl to set
	 */
	public void setJdbcUrl(String jdbcUrl) {
		UserDatabase.jdbcUrl = jdbcUrl;
	}
	
}