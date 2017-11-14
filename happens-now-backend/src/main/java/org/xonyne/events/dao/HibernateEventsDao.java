package org.xonyne.events.dao;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xonyne.events.jsonmapper.dto.FacebookEvent;

@Repository
public class HibernateEventsDao implements EventsDao {

	private Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateEventsDao.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
	public void persist(Object object) {
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
	public void persist(Set<FacebookEvent> allEvents) {
		
	}
}
