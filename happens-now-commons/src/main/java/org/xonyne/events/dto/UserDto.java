package org.xonyne.events.dto;

import java.io.Serializable;

public class UserDto implements Serializable{
	private static final long serialVersionUID = -3583149474651268499L;
	
	private Long id;
	private String name;
	private String userName;

	public UserDto() {
		super();
	}

	public UserDto(Long id, String name, String userName) {
		super();
		this.id = id;
		this.name = name;
		this.userName = userName;
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
	
}
