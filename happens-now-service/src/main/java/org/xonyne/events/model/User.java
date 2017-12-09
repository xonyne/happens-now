package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:16 PM
 */
@Entity
@Table(name="users")
//@SequenceGenerator(name="user_userId_seq", sequenceName="user_userId_seq", initialValue = 1, allocationSize = 1)
public class User {

//	@GeneratedValue(generator="user_userId_seq")
	@Id
	@Column(name="user_id")
	private Long id;
	private String name;
	private String userName;
	private String password;
        
        @Transient
        private boolean isStale;

	public User(){

	}

	public User(Long id, String name, String userName,
			String password) {
		super();
		this.id = id;
		this.name = name;
		this.userName = userName;
		this.password = password;
                this.isStale = false;
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
        
        public boolean getIsStale() {
		return this.isStale;
	}

	public void setIsStale(boolean isStale) {
		this.isStale = isStale;
	}

}