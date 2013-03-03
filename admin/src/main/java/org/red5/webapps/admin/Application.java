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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.util.ScopeUtils;
import org.red5.webapps.admin.handler.Red5AuthenticationHandler;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.authentication.ProviderManager;

/**
 * Admin Panel for Red5 Server
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Martijn van Beek (martijn.vanbeek@gmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class Application extends ApplicationAdapter implements ApplicationContextAware {

	// provide the logger context for other parts of the app
	private static Logger log = Red5LoggerFactory.getLogger(Application.class, "admin");

	private static ApplicationContext applicationContext;

	@SuppressWarnings("unused")
	private static ResourceBundleMessageSource messageSource;

	// used to insert test routines / data
	@SuppressWarnings("unused")
	private static boolean debug = false;

	// useful for debugging with clients that use our CRAM auth
	private static boolean enforceAuthentication = true;	
	
	private HashMap<Integer, String> scopes;

	private int scope_id = 0;

	@Override
	public boolean appStart(IScope app) {
		log.info("Admin application started");
		// authentication check
		ProviderManager authManager = (ProviderManager) applicationContext.getBean("authenticationManager");
		log.info("Available auth providers: {}", authManager.getProviders().size());
		if (authManager.isEraseCredentialsAfterAuthentication()) {
			log.info("Provider set to erase creds, changing to NOT do this");
			authManager.setEraseCredentialsAfterAuthentication(false);
		}
		// add an authentication listener
		if (enforceAuthentication) {
			addListener(new Red5AuthenticationHandler(applicationContext));
		}		
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		this.scope = scope;
		return true;
	}

	/**
	 * Get all running applications
	 * 
	 * @return HashMap containing all applications
	 */
	public HashMap<Integer, Object> getApplications() {
		IScope root = ScopeUtils.findRoot(scope);
		Set<String> names = root.getScopeNames();
		HashMap<Integer, Object> apps = new HashMap<Integer, Object>();
		int id = 0;
		for (String name : names) {
			int size = getConnections(name).size();
			HashMap<String, String> app = new HashMap<String, String>();
			app.put("name", name);
			app.put("clients", size + "");
			apps.put(id, app);
			id++;
		}
		return apps;
	}

	/**
	 * Get Application statistics.
	 * 
	 * @param scopeName
	 * @return HashMap with the statistics
	 */
	public HashMap<Integer, HashMap<String, String>> getStatistics(String scopeName) {
		ScopeStatistics scopestats = new ScopeStatistics();
		return scopestats.getStats(getScope(scopeName));
	}

	/**
	 * Get Client statistics
	 * 
	 * @param userid
	 * @return HashMap with the statistics
	 */
	public HashMap<Integer, HashMap<String, String>> getUserStatistics(String userid) {
		UserStatistics userstats = new UserStatistics();
		return userstats.getStats(userid, scope);
	}

	/**
	 * Get all the scopes
	 * 
	 * @param scopeName
	 * @return HashMap containing all the scopes
	 */
	public HashMap<Integer, String> getScopes(String scopeName) {
		IScope root = ScopeUtils.findRoot(scope);
		IScope scopeObj = root.getScope(scopeName);
		scopes = new HashMap<Integer, String>();
		try {
			getRooms(scopeObj, 0);
		} catch (NullPointerException npe) {
			log.debug(npe.toString());
		}
		log.debug("Scopes: {}", scopes);
		return scopes;
	}

	/**
	 * Get all the scopes
	 * 
	 * @param root
	 *            the scope to from
	 * @param depth
	 *            scope depth
	 */
	public void getRooms(IScope root, int depth) {
		Set<String> names = root.getScopeNames();
		String indent = "";
		for (int i = 0; i < depth; i++) {
			indent += " ";
		}
		for (String name : names) {
			try {
				IScope parent = root.getScope(name);
				// parent
				getRooms(parent, depth + 1);
				scopes.put(scope_id, indent + name);
				scope_id++;
				log.debug("Found scope: {}", name);
			} catch (NullPointerException npe) {
				log.debug(npe.toString());
			}
		}
	}

	/**
	 * Get all the connections (clients)
	 * 
	 * @param scopeName
	 * @return HashMap with all clients in the given scope
	 */
	public HashMap<Integer, String> getConnections(String scopeName) {
		HashMap<Integer, String> connections = new HashMap<Integer, String>();
		IScope root = getScope(scopeName);
		if (root != null) {
			Set<IClient> clients = root.getClients();
			Iterator<IClient> client = clients.iterator();
			int id = 0;
			while (client.hasNext()) {
				IClient c = client.next();
				String user = c.getId();
				connections.put(id, user);
				id++;
			}
		}
		return connections;
	}

	/**
	 * Kill a client
	 * 
	 * @param userid
	 */
	public void killUser(String userid) {
		IScope root = ScopeUtils.findRoot(scope);
		Set<IClient> clients = root.getClients();
		Iterator<IClient> client = clients.iterator();
		while (client.hasNext()) {
			IClient c = client.next();
			if (c.getId().equals(userid)) {
				c.disconnect();
			}
		}
	}

	/**
	 * Get an scope by name
	 * 
	 * @param scopeName
	 * @return IScope the requested scope
	 */
	private IScope getScope(String scopeName) {
		IScope root = ScopeUtils.findRoot(scope);
		return getScopes(root, scopeName);
	}

	/**
	 * Search through all the scopes in the given scope to a scope with the
	 * given name
	 * 
	 * @param root
	 * @param scopeName
	 * @return IScope the requested scope
	 */
	private IScope getScopes(IScope root, String scopeName) {
		// log.info("Found scope "+root.getName());
		if (root.getName().equals(scopeName)) {
			return root;
		} else {
			if (root instanceof IScope) {
    			Set<String> names = root.getScopeNames();
    			for (String name : names) {
    				try {
    					IScope parent = root.getScope(name);
    					IScope scope = getScopes(parent, scopeName);
    					if (scope != null) {
    						return scope;
    					}
    				} catch (NullPointerException npe) {
    					log.debug(npe.toString());
    				}
    			}
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void disconnect(IConnection conn, IScope scope) {
		// Get the previously stored username
		String rid = conn.getClient().getId();
		// Unregister user
		log.info("Client with id {} disconnected.", rid);
		super.disconnect(conn, scope);
	}

	/**
	 * Get the root scope
	 * 
	 * @return IScope
	 */
	public IScope getScope() {
		return scope;
	}
	
	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		Application.debug = debug;
	}

	/**
	 * @param enforceAuthentication the enforceAuthentication to set
	 */
	public void setEnforceAuthentication(boolean enforceAuthentication) {
		Application.enforceAuthentication = enforceAuthentication;
	}

	/**
	 * @param applicationContext
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		Application.applicationContext = applicationContext;
		log.trace("setApplicationContext {}", Application.applicationContext);
	}
	
}