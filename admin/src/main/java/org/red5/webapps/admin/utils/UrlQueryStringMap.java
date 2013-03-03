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

import java.util.HashMap;

/**
 * Simple query string to map converter.
 * 
 * @author Paul Gregoire
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("serial")
public final class UrlQueryStringMap<K, V> extends HashMap<K, V> {
	
	public UrlQueryStringMap() {
		super();
	}
	
	/**
	 * Parse a given query string and return an instance of this class.
	 * 
	 * @param queryString
	 * @return
	 */
	public static UrlQueryStringMap<String, String> parse(String queryString) {
		UrlQueryStringMap<String, String> map = new UrlQueryStringMap<String, String>();
		//
		String tmp = queryString;
		//check if we start with '?' or not
		if (queryString.charAt(0) != '?') {
			tmp = queryString.split("\\?")[1];
		} else if (queryString.charAt(0) == '?') {
			tmp = queryString.substring(1);
		}
		//now break up into key/value blocks
		String[] kvs = tmp.split("&");
		//take each key/value block and break into its key value parts
		for (String kv : kvs) {
			String[] split = kv.split("=");
			map.put(split[0], split[1]);
		}
		return map;
	}
	
}