package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:16 PM
 */
public class User {

	private Long id;
	private String name;
	private String userName;
	private String password;

	public User(){

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
}