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

package org.red5.components;

import org.red5.server.api.scope.IScope;
import org.red5.server.api.so.ISharedObject;
import org.red5.server.api.so.ISharedObjectService;
import org.red5.server.util.ScopeUtils;

/**
 * Class that keeps a list of client names in a SharedObject.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Joachim Bauch (jojo@struktur.de)
 */
public class ClientManager {

	/** Stores the name of the SharedObject to use. */
	private String name;

	/** Should the SharedObject be persistent? */
	private boolean persistent;

	/**
	 * Create a new instance of the client manager.
	 * 
	 * @param name
	 *            name of the shared object to use
	 * @param persistent
	 *            should the shared object be persistent
	 */
	public ClientManager(String name, boolean persistent) {
		this.name = name;
		this.persistent = persistent;
	}

	/**
	 * Return the shared object to use for the given scope.
	 * 
	 * @param scope
	 *            the scope to return the shared object for
	 * @return the shared object to use
	 */
	private ISharedObject getSharedObject(IScope scope) {
		ISharedObjectService service = (ISharedObjectService) ScopeUtils.getScopeService(scope, ISharedObjectService.class, false);
		return service.getSharedObject(scope, name, persistent);
	}

	/**
	 * A new client connected. This adds the username to the shared object of
	 * the passed scope.
	 * 
	 * @param scope
	 *            scope the client connected to
	 * @param username
	 *            name of the user that connected
	 * @param uid
	 *            the unique id of the user that connected
	 */
	public void addClient(IScope scope, String username, String uid) {
		ISharedObject so = getSharedObject(scope);
		so.setAttribute(uid, username);
	}

	/**
	 * A client disconnected. This removes the username from the shared object
	 * of the passed scope.
	 * 
	 * @param scope
	 *            scope the client disconnected from
	 * @param uid
	 *            unique id of the user that disconnected
	 * @return the username of the disconnected user
	 */
	public String removeClient(IScope scope, String uid) {
		ISharedObject so = getSharedObject(scope);
		if (!so.hasAttribute(uid)) {
			// SharedObject is empty. This happens when the last client
			// disconnects.
			return null;
		}

		String username = so.getStringAttribute(uid);
		so.removeAttribute(uid);
		return username;
	}

}
