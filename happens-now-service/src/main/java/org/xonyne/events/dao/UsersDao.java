package org.xonyne.events.dao;

import org.xonyne.events.model.User;

public interface UsersDao {

	User findOrPersist(User user);
	
	User find(String userName, String password);
        
        User find(Long userId);
}
