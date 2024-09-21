package com.straviso.ns.dispatchcontrollercore.security;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) {
		return new org.springframework.security.core.userdetails.User(username, "", new ArrayList<>());
	}
}
