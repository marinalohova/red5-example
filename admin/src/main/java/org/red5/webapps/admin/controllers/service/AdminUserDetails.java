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

package org.red5.webapps.admin.controllers.service;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AdminUserDetails implements UserDetails {

	private final static long serialVersionUID = 2801983490L;

	private Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(1);

	private int userid;

	private String username;

	private String password;

	private Boolean enabled;

	public AdminUserDetails() {
	}

	public AdminUserDetails(int userid) {
		this.userid = userid;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public void setUsername(String value) {
		username = value;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String value) {
		password = value;
	}

	public String getPassword() {
		return password;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setEnabled(Integer enabledInt) {
		this.enabled = (enabledInt == 1);
	}

	public void setEnabled(String enabledStr) {
		this.enabled = "enabled".equals(enabledStr);
	}

	public void setAuthorities(GrantedAuthority[] authorities) {
		this.authorities.clear();
		for (GrantedAuthority authority : authorities) {
			this.authorities.add(authority);
		}
	}

	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Returns a hash code value for the object.  This implementation computes
	 * a hash code value based on the id fields in this object.
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		return userid;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof AdminUserDetails)) {
			return false;
		}
		AdminUserDetails other = (AdminUserDetails) object;
		if (this.userid != other.userid) {
			return false;
		}
		return true;
	}

}