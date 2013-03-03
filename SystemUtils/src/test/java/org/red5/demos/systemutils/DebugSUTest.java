package org.red5.demos.systemutils;

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

import java.io.File;
import java.lang.reflect.Method;

public class DebugSUTest {

	/**
	 * 
	 * 	Debug Test for SystemUtils
	 * 	-------------------------------
	 * 	System.getProperty("_____");
	 * 	===============================
	 * 	os.name						:Operating System Name
	 * 	os.arch						: x86/x64
	 * 	java.specification.version	: Java Version (Required 1.5 or 1.6 and higher)
	 * 	-------------------------------
	 * 	Runtime.getRuntime()._____  (Java Virtual Machine Memory)
	 * 	===============================
	 * 	maxMemory()					: Maximum limitation
	 * 	totalMemory()				: Total can be used
	 * 	freeMemory()				: Availability
	 * 	totalMemory()-freeMemory()	: In Use
	 * 	availableProcessors()		: Total Processors available
	 * 	-------------------------------
	 *  getOperatingSystemMXBean()	(Actual Operating System RAM)
	 *  ===============================
	 *  osCommittedVirtualMemory()	: Virtual Memory
	 *  osFreePhysicalMemory()		: Available Physical Memory
	 *  osFreeSwapSpace()			: Available Swap Space
	 *  osTotalPhysicalMemory()		: Total Physical Memory
	 *  osTotalSwapSpace()			: Total Swap Space
	 *  osInUsePhysicalMemory()		: In Use Physical Memory
	 *  osInUseSwapSpace()			: In Use Swap Space
	 *  -------------------------------
	 *  File						(Actual Harddrive Info: Supported for JRE 1.6)
	 *  ===============================
	 *	osHDUsableSpace()			: Usable Space
	 *	osHDTotalSpace()			: Total Space
	 *	osHDFreeSpace()				: Available Space
	 *	osHDInUseSpace()			: In Use Space
	 *  -------------------------------
	 */
	public static int sleeper = 0;

	public static void main(String args[]) throws Exception {

		//Testing all functions except convertByteSize
		//Test Begin 

		//Set timeout loop to avoid consume CPU Usage
		sleeper = 10;

		System.out.println("SystemUtils Test Begins");
		System.out.println("-----------------------");
		System.out.println("SystemUtils Version: " + SystemUtils.VERSION);
		System.out.println("JRE Version: " + SystemUtils.jvmVersion);
		System.out.println("-----------------------");
		System.out.println("Operating System: " + SystemUtils.osName);
		System.out.println("CPU Architecture: " + SystemUtils.osArch);
		System.out.println("CPU Processor(s): " + SystemUtils.osProcessorX);
		System.out.println("-----------------------");
		/*/
		//Test Memory Functions
		part1SU();
		//*/
		/*/
		//Test Harddrive Space Functions
		part2SU();
		//*/
		System.out.println("SystemUtils Test Ends");
		//End of Test
	}

	//SystemUtils Part # Tests
	protected static void part1SU() throws Exception {
		System.out.println("------Part 1 Start------");
		String[] functions = { "jvmMaxMemory", "jvmTotalMemory", "jvmFreeMemory", "jvmInUseMemory", "osCommittedVirtualMemory", "osTotalPhysicalMemory", "osFreePhysicalMemory",
				"osInUsePhysicalMemory", "osTotalSwapSpace", "osFreeSwapSpace", "osInUseSwapSpace" };
		String[] byteSize = { null, "AuTo", "AUTO", "B", "KB", "Mb", "GB", "tB", "PB", "EB", "something" };
		String[] booleans = { null, "true", "false", "something" };
		for (int i = 0; i < functions.length; i++) {
			for (int ii = 0; ii < byteSize.length; ii++) {
				for (int iii = 0; iii < booleans.length; iii++) {
					Thread.sleep(sleeper);
					Class<SystemUtils> cls = SystemUtils.class;
					Class<?> partypes[] = new Class[2];
					partypes[0] = String.class;
					partypes[1] = Boolean.class;
					Method meth = cls.getDeclaredMethod(functions[i], partypes);
					Object arglist[] = new Object[2];
					arglist[0] = byteSize[ii];
					if (booleans[iii] == null) {
						arglist[1] = null;
					} else {
						arglist[1] = Boolean.parseBoolean(booleans[iii]);
					}
					System.out.println("Function: " + functions[i] + "; byteSize: " + byteSize[ii] + "; booleans: " + booleans[iii] + "; result: " + meth.invoke(cls, arglist));
				}
			}
		}
		System.out.println("-------Part 1 End-------");
	}

	protected static void part2SU() throws Exception {
		System.out.println("------Part 2 Start------");
		System.out.println("This part only will work with 1.6");
		System.out.println("jvmVersion: " + SystemUtils.jvmVersion);
		String path = "test";
		path += File.pathSeparator + "invalid";
		path += File.pathSeparator + "something";
		File[] f = File.listRoots();
		for (int i = 0; i < f.length; i++) {
			if (f[i].getTotalSpace() != 0) {
				path += File.pathSeparator + f[i].getPath();
			}
		}
		String[] paths = path.split(File.pathSeparator);
		String[] functions = { "osHDUsableSpace", "osHDTotalSpace", "osHDFreeSpace", "osHDInUseSpace" };
		String[] byteSize = { null, "auto", "AUTO", "B", "kb", "mB", "GB", "Tb", "PB", "EB", "demo" };
		String[] booleans = { null, "true", "false", "test" };
		for (int i = 0; i < functions.length; i++) {
			for (int ii = 0; ii < paths.length; ii++) {
				for (int iii = 0; iii < byteSize.length; iii++) {
					for (int iiii = 0; iiii < booleans.length; iiii++) {
						Thread.sleep(sleeper);
						Class<SystemUtils> cls = SystemUtils.class;
						Class<?> partypes[] = new Class[3];
						partypes[0] = String.class;
						partypes[1] = String.class;
						partypes[2] = Boolean.class;
						Method meth = cls.getDeclaredMethod(functions[i], partypes);
						Object arglist[] = new Object[3];
						arglist[0] = paths[ii];
						arglist[1] = byteSize[iii];
						if (booleans[iiii] == null) {
							arglist[2] = null;
						} else {
							arglist[2] = Boolean.parseBoolean(booleans[iiii]);
						}
						System.out.println("Function: " + functions[i] + "; paths: " + paths[ii] + "; byteSize: " + byteSize[iii] + "; booleans: " + booleans[iiii] + "; result: "
								+ meth.invoke(cls, arglist));
					}
				}
			}
		}
		System.out.println("-------Part 2 End-------");
	}
}
