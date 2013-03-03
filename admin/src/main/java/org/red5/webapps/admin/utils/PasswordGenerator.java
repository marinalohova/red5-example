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

package org.red5.webapps.admin.utils;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

/**
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Daniel Rossi
 */
public class PasswordGenerator {
	
	private String password;

	private String salt = "secret";

	private Md5PasswordEncoder md5 = new Md5PasswordEncoder();

	public static void main(String args[]) {
		PasswordGenerator generate = new PasswordGenerator(args[0], args[1]);
		generate.run();
	}

	public PasswordGenerator(String password, String salt) {
		this.salt = salt;
		this.password = password;
	}

	public String getPassword() {
		return md5.encodePassword(this.password, this.salt).toString();
	}

	public void run() {
		System.out.println(md5.encodePassword(this.password, this.salt).toString());
	}

}
