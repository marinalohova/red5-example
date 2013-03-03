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

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IConnection;
import org.red5.server.api.persistence.IPersistable;
import org.red5.server.api.persistence.IPersistenceStore;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.statistics.IScopeStatistics;
import org.red5.webapps.admin.utils.Utils;
import org.slf4j.Logger;

/**
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Martijn van Beek (martijn.vanbeek@gmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class ScopeStatistics {
	
	private static Logger log = Red5LoggerFactory.getLogger(ScopeStatistics.class, "admin");

	private HashMap<Integer, HashMap<String, String>> apps;

	private AtomicInteger id = new AtomicInteger(0);

	public ScopeStatistics() {
	}

	public HashMap<Integer, HashMap<String, String>> getStats(IScope root) {
		apps = new HashMap<Integer, HashMap<String, String>>();
		if (root != null) {
    		IScopeStatistics stats = root.getStatistics();
    		if (stats != null) {
    			extractConnectionData(root);
    			addData("Persistence Data", "--");
    			IPersistenceStore data = root.getStore();
    			Collection<IPersistable> objects = data.getObjects();
    			Iterator<IPersistable> iter = objects.iterator();
    			while (iter.hasNext()) {
    				IPersistable name = iter.next();
    				addData("Name", name.getName());
    				addData("Type", name.getType());
    				addData("Path", name.getPath());
    				addData("Last modified", Utils.formatDate(name.getLastModified()));
    
    			}
    			addData("Scope Data", "--");
    			addData("Active sub scopes", stats.getActiveSubscopes());
    			addData("Total sub scopes", stats.getTotalSubscopes());
    			addData("Active clients", stats.getActiveClients());
    			addData("Total clients", stats.getTotalClients());
    			addData("Active connections", stats.getActiveConnections());
    			addData("Total connections", stats.getTotalConnections());
    			addData("Created", Utils.formatDate(stats.getCreationTime()));
    		} else {
    			log.warn("Stats were null for: {}", root);
    		}
		} else {
			log.warn("Scope was null, no stats");
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
				addData("Dropped messages", connection.getDroppedMessages());
				addData("Pending messages", connection.getPendingMessages());
				addData("Received messages", connection.getReadMessages());
				addData("Remote address", connection.getRemoteAddress() + ":" + connection.getRemotePort() + " (" + connection.getHost() + ")");
				addData("Path", connection.getPath());
			}
		}
	}
}