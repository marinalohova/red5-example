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

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Martijn van Beek (martijn.vanbeek@gmail.com)
 * @author Daniel Rossi
 */
public class Utils {

	public static String formatDate(long d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(d);
		SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		return date.format(calendar.getTime());
	}

	public static String formatBytes(long d) {
		String out = d + "";
		if (d < 1024) {
			out = d + " Bytes";
		} else if (d > 1024) {
			out = (d / 1024) + " KB";
		} else if (d > 104858) {
			out = (d / 1000000) + " MB";
		}
		return out;
	}
}