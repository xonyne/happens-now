package org.xonyne.events.dao;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xonyne.events.jsonmapper.dto.FacebookEvent;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.User;

@Repository
public class HibernateEventsDao implements EventsDao {

	private Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateEventsDao.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void persistObject(Object object) {
		logger.debug("create " + object.getClass().getName() + " instance");
		try {
			entityManager.persist(object);
			entityManager.flush();
			logger.debug("persisted successful");
		} catch (RuntimeException re) {
			logger.error("persist " + object.getClass().getName() + " failed", re);
			throw re;
		}
	}
	
	@Override
	@Transactional
	public void persist(Event event) {
		persistObject(event);
	}
	
	@Override
	@Transactional
	public User findOrPersist(User user) {
		User storedUser = entityManager.find(User.class, user.getId());
		if (storedUser == null){
			persistObject(user);
			storedUser = entityManager.find(User.class, user.getId());
		}
		
		return storedUser;
	}
	
	@Override
	@Transactional
	public Place findOrPersist(Place place){
		Place storedPlace = entityManager.find(Place.class, place.getId());
		if (storedPlace == null){
			persistObject(place);
			storedPlace = entityManager.find(Place.class, place.getId());
		}
		
		return storedPlace;
		
	}

}
