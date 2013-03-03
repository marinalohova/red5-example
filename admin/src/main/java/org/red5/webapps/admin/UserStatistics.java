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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.util.ScopeUtils;
import org.red5.webapps.admin.utils.Utils;

/**
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Martijn van Beek (martijn.vanbeek@gmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class UserStatistics {
	
	private HashMap<Integer, HashMap<String, String>> apps;

	private AtomicInteger id = new AtomicInteger(0);

	public UserStatistics() {
	}

	public HashMap<Integer, HashMap<String, String>> getStats(String userid, IScope scope) {
		apps = new HashMap<Integer, HashMap<String, String>>();
		IScope root = ScopeUtils.findRoot(scope);
		Set<IClient> clients = root.getClients();
		Iterator<IClient> client = clients.iterator();
		extractConnectionData(root);
		addData("User attributes", "--");
		while (client.hasNext()) {
			IClient c = client.next();
			if (c.getId().equals(userid)) {
				Set<String> names = c.getAttributeNames();
				Iterator<String> itnames = names.iterator();
				while (itnames.hasNext()) {
					String key = itnames.next();
					addData(key, c.getAttribute(key));
				}
				addData("Created", Utils.formatDate(c.getCreationTime()));
			}
		}
		return apps;
	}

	protected void addData(String name, Object value) {
		HashMap<String, String> app = new HashMap<String, String>();
		app.put("name", name);
		app.put("value", value.toString());
		apps.put(id.getAndIncrement(), app);
	}

	protected void extractConnectionData(IScope root) {

		Collection<Set<IConnection>> conns = root.getConnections();

		for (Set<IConnection> set : conns) {
			for (IConnection connection : set) {
				addData("Scope statistics", "--");
				addData("Send bytes", Utils.formatBytes(connection.getWrittenBytes()));
				addData("Received bytes", Utils.formatBytes(connection.getReadBytes()));
				addData("Send messages", connection.getWrittenMessages());
				addData("Received messages", connection.getReadMessages());
				addData("Dropped messages", connection.getDroppedMessages());
				addData("Pending messages", connection.getPendingMessages());
				addData("Remote address", connection.getRemoteAddress() + ":" + connection.getRemotePort() + " (" + connection.getHost() + ")");
				addData("Path", connection.getPath());
			}
		}
	}
}