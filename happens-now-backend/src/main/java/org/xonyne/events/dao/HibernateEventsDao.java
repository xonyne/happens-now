package org.xonyne.events.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xonyne.events.dto.EventDto;
import org.xonyne.events.jsonmapper.dto.FacebookEvent;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.User;

@Repository
public class HibernateEventsDao implements EventsDao {

	private Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateEventsDao.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
//	@Autowired
//	private SessionFactory sessionFactory;

	@Override
	@Transactional
	public Event findEvent(Event event){
		logger.debug("find event with id:" + event.getEventId());
		try {
			return entityManager.find(Event.class, event.getEventId());
		} catch (RuntimeException re) {
			logger.error("error in find Event, "+re.getMessage(), re);
			throw re;
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void persistObject(Object object) {
		logger.debug("create " + object.getClass().getName() + " instance");
		try {
			entityManager.merge(object);
			entityManager.flush();
			logger.debug("persisted successful");
		} catch (RuntimeException re) {
			logger.error("persist " + object.getClass().getName() + " failed", re);
			throw re;
		}
	}
	
	@Override
	@Transactional
	public void merge(Event event) {
		persistObject(event);
	}
	
	@Override
	@Transactional
	public Event findOrPersist(Event event) {
		Event storedEvent = entityManager.find(Event.class, event.getEventId());
		if (storedEvent == null){
			persistObject(event);
			storedEvent = entityManager.find(Event.class, event.getEventId());
		}
		
		return event;
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

	@Override
	@Transactional
	public List<Event> findEvents(Date from, Date to) {
		Session session = entityManager.unwrap(Session.class);
		SessionFactory sessionFactory = session.getSessionFactory();		
		
		Query query = sessionFactory.openSession().createQuery("from Event e where e.startDateTime BETWEEN :start AND :end ");
		query.setParameter("start", from).setParameter("end", to);
		List<Event> list = query.list();
		
		return list;
	}
}
