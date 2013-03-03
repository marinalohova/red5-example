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

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;

public class Application extends ApplicationAdapter {

	private static Logger log = Red5LoggerFactory.getLogger(Application.class, "SystemUtils");

	/** {@inheritDoc} */
	@Override
	public boolean appStart(IScope app) {
		super.appStart(app);
		log.info("SystemUtils appStart");
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void appDisconnect(IConnection conn) {
		log.info("SystemUtils appDisconnect");
		super.appDisconnect(conn);
	}

	public String jvmMaxMemory(String size, Boolean txtByte) {
		return SystemUtils.jvmMaxMemory(size, txtByte);
	}

	public String jvmTotalMemory(String size, Boolean txtByte) {
		return SystemUtils.jvmTotalMemory(size, txtByte);
	}

	public String jvmFreeMemory(String size, Boolean txtByte) {
		return SystemUtils.jvmFreeMemory(size, txtByte);
	}

	public String jvmInUseMemory(String size, Boolean txtByte) {
		return SystemUtils.jvmInUseMemory(size, txtByte);
	}

	public String osCommittedVirtualMemory(String size, Boolean txtByte) {
		return SystemUtils.osCommittedVirtualMemory(size, txtByte);
	}

	public String osTotalPhysicalMemory(String size, Boolean txtByte) {
		return SystemUtils.osTotalPhysicalMemory(size, txtByte);
	}

	public String osFreePhysicalMemory(String size, Boolean txtByte) {
		return SystemUtils.osFreePhysicalMemory(size, txtByte);
	}

	public String osInUsePhysicalMemory(String size, Boolean txtByte) {
		return SystemUtils.osInUsePhysicalMemory(size, txtByte);
	}

	public String osTotalSwapSpace(String size, Boolean txtByte) {
		return SystemUtils.osTotalSwapSpace(size, txtByte);
	}

	public String osFreeSwapSpace(String size, Boolean txtByte) {
		return SystemUtils.osFreeSwapSpace(size, txtByte);
	}

	public String osInUseSwapSpace(String size, Boolean txtByte) {
		return SystemUtils.osInUseSwapSpace(size, txtByte);
	}

	public String osHDUsableSpace(String path, String size, Boolean txtByte) {
		return SystemUtils.osHDUsableSpace(path, size, txtByte);
	}

	public String osHDTotalSpace(String path, String size, Boolean txtByte) {
		return SystemUtils.osHDTotalSpace(path, size, txtByte);
	}

	public String osHDFreeSpace(String path, String size, Boolean txtByte) {
		return SystemUtils.osHDFreeSpace(path, size, txtByte);
	}

	public String osHDInUseSpace(String path, String size, Boolean txtByte) {
		return SystemUtils.osHDInUseSpace(path, size, txtByte);
	}

	public String convertByteSize(Long bytes, String size, Boolean txtByte) {
		return SystemUtils.convertByteSize(bytes, size, txtByte);
	}

}
