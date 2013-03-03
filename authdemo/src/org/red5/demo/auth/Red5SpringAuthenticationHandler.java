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

package org.red5.demo.auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationLifecycle;
import org.red5.server.api.IConnection;
import org.red5.server.exception.ClientRejectedException;
import org.red5.server.net.rtmp.RTMPConnection;
import org.red5.server.session.SessionManager;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * Provides Red5 specific authentication using an application listener.
 * 
 * This handler uses a basic challenge-response protocol:
<ul>  <li>Client requests a session</li>  <li>Server generates a unique, random ChallengeString (e.g. salt, guid) as well as a SessionID and sends both to client</li>  <li>Client gets UserID and Password from UI. Hashes the password once and call it PasswordHash.   Then combines PasswordHash with the random string received from server in step 2, and hashes them together again, call this ResponseString</li>  <li>Client sends the server UserID, ResponseString and SessionID</li>  <li>Server looks up users stored PasswordHash based on UserID, and the original ChallengeString based on SessionID.   Then computes the ResponseHash by hashing the PasswordHash and ChallengeString.   If its equal to the ResponseString sent by user, then authentication succeeds.</li>  </ul>
 * 
 * @author Paul Gregoire
 */
public class Red5SpringAuthenticationHandler extends ApplicationLifecycle implements ApplicationContextAware {

	private static Logger log = Red5LoggerFactory.getLogger(Red5SpringAuthenticationHandler.class, "authdemo");
	
	private ApplicationContext applicationContext;
	
	private static String rejectMissingAuth = "[ code=403 .need auth; authmod=red5 ]";

	private static String invalidAuthMod = "[ AccessManager.Reject ] : [ authmod=red5 ] : ?reason=invalid_authmod";

	private static String badAuth = "[ AccessManager.Reject ] : [ authmod=red5 ] : ?reason=badauth";

	private static String noSuchUser = "[ AccessManager.Reject ] : [ authmod=red5 ] : ?reason=nosuchuser";

	private Mac hmacSHA256;

	//salt to use for challenge string generation
	private String salt = "red5isawesomer";

	//map of challenge strings, keyed by session id
	private Map<String, String> sessionChallenges = new HashMap<String, String>();

	static {
		//get security provider
		Security.addProvider(new BouncyCastleProvider());
	}

	{
		try {
			hmacSHA256 = Mac.getInstance("HmacSHA256");
		} catch (SecurityException e) {
			log.error("Security exception when getting HMAC", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("HMAC SHA256 does not exist");
		}
	}
	
	public boolean appConnect(IConnection conn, Object[] params) {
		log.info("appConnect");
		// start with negative result
		boolean result = false;
		log.debug("Connection: {}", conn);
		log.debug("Params: {}", params);
		// start off with the status being bad authentication
		String status = badAuth;
		// get the connection parameters
		Map<String, Object> connectionParams = conn.getConnectParams();
		log.debug("Connection params: {}", connectionParams);
		if (!connectionParams.containsKey("queryString")) {
			//set as missing auth notification
			status = rejectMissingAuth;
		} else {
			//get the raw query string
			String rawQueryString = (String) connectionParams.get("queryString");
			try {
				//parse into a usable query string
				UrlQueryStringMap<String, String> queryString = UrlQueryStringMap.parse(rawQueryString);
				log.debug("Query string: {}", queryString);
				//get the values we want
				String userName = queryString.get("user");
				log.debug("User: {}", userName);
				// do a user lookup
				AggregatedUserDetailsService userDetailsService = (AggregatedUserDetailsService) applicationContext.getBean("aggregatedUserDetailsService");
				// this will throw an exception if the user cant be located by name
				UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
				// get the authentication "style"
				String authmod = queryString.get("authmod");
				log.debug("Authmod: {}", authmod);
				//make sure they requested red5 auth
				if ("red5".equals(authmod)) {
					String response = queryString.get("response");
					if (response != null) {
						response = queryString.get("response").replace(' ', '+');
					}
					log.debug("Response: {}", response);
					//try the querystring first
					String sessionId = queryString.get("sessionid");
					if (sessionId == null) {
						//get the session id - try conn next
						sessionId = ((RTMPConnection) conn).getSessionId();
						if (sessionId == null) {
							//use attribute
							if (conn.hasAttribute("sessionId")) {
								sessionId = conn.getStringAttribute("sessionId");
							} else {
								sessionId = SessionManager.getSessionId();
								conn.setAttribute("sessionId", sessionId);
							}
						}
					}
					log.debug("Session id: {}", sessionId);
					String challenge = null;
					if (response != null) {
						//look up challenge (gets and removes at the same time)
						challenge = sessionChallenges.remove(sessionId);
						// get the password
						String password = userDetails.getPassword();
						log.debug("Users password: {}", password);
						//generate response hash to compare
						String responseHash = calculateHMACSHA256(challenge, password);
						log.debug("Generated response: {}", responseHash);
						log.debug("Generated response: {}", response);
						//decode both hashes before we compare otherwise we will have issues like
						//4+5WioxdBLhx4qajIybxkBkynDsv7KxtNzqj4V/VbzU != 4+5WioxdBLhx4qajIybxkBkynDsv7KxtNzqj4V/VbzU=           			
						if (Arrays.areEqual(Base64.decodeBase64(responseHash.getBytes()), Base64.decodeBase64(response.getBytes()))) {
							// everything matches so now do the actual authentication
							// get the authentication manager
							ProviderManager authManager = (ProviderManager) applicationContext.getBean("authManager");
							UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName, password);
							Authentication auth = null;
							try {
								auth = authManager.authenticate(token);
								log.info("Authentication result: {}\ndetails: {}", auth.isAuthenticated(), auth);
								result = auth.isAuthenticated();
								// set the authenticated user into the context (thread-local)
								if (result) {
									SecurityContextHolder.getContext().setAuthentication(auth);									
								}
							} catch (Exception ex) {
								log.warn("Problem during auth attempt: {}", ex);
							} 							
						}
					} else if (authmod != null && userName != null) {
						// generate a challenge
						challenge = calculateHMACSHA256(salt, sessionId);
						// store the generated data
						sessionChallenges.put(sessionId, challenge);
						// set as rejected
						status = String.format("[ AccessManager.Reject ] : [ authmod=red5 ] : ?reason=needauth&user=%s&sessionid=%s&challenge=%s", userName, sessionId, challenge);
					}
					log.debug("Challenge: {}", challenge);
				} else {
					status = invalidAuthMod;
				}
			} catch (UsernameNotFoundException ex) {
				status = noSuchUser;				
			} catch (Exception e) {
				log.error("Error authenticating", e);
			}
		}
		//send the status object
		log.debug("Status: {}", status);
		if (!result) {
			throw new ClientRejectedException(status);
		}
		return result;
	}

	/**
	 * Generate an HMAC-SHA256 hash and return encoded with Base64.
	 * 
	 * @param key
	 * @param input
	 * @return
	 */
	private String calculateHMACSHA256(String key, String input) {
		byte[] output = null;
		try {
			hmacSHA256.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
			output = hmacSHA256.doFinal(input.getBytes());
		} catch (InvalidKeyException e) {
			log.error("Invalid key", e);
		}
		//String result = Base64.encodeBase64String(output);
		byte[] res = Base64.encodeBase64(output);
		String result = new String(res);
		//strip any cr/lf
		return result.replaceAll("(\r\n|\r|\n|\n\r)", "");
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
		
}
