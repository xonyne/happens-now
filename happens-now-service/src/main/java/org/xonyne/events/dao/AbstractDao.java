package org.xonyne.events.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class AbstractDao {

	private Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractDao.class);
	
	@PersistenceContext
	private EntityManager entityManager;

	public AbstractDao() {
		super();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Object persistObject(Object object) {
		logger.debug("create " + object.getClass().getName() + " instance");
		Object o;
                try {
			o = entityManager.merge(object);
			entityManager.flush();
                       
			logger.debug("persisted successful");
		} catch (RuntimeException re) {
			logger.error("persist " + object.getClass().getName() + " failed", re);
			throw re;
		}
                return o;
	}
	
	/**
	 * Returns Hibernate Session
	 */
	protected SessionFactory getSession() {
		Session session = entityManager.unwrap(Session.class);
		SessionFactory sessionFactory = session.getSessionFactory();
		
		return sessionFactory;
	}
}