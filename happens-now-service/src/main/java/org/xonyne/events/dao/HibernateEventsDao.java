package org.xonyne.events.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.Rating;

@Repository
public class HibernateEventsDao extends AbstractDao implements EventsDao {

	private Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateEventsDao.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
//	@Autowired
//	private SessionFactory sessionFactory;

	@Override
	@Transactional
	public Event findEvent(Event event){
		logger.debug("find event with id:" + event.getId());
		try {
			return entityManager.find(Event.class, event.getId());
		} catch (RuntimeException re) {
			logger.error("error in find Event, "+re.getMessage(), re);
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
	public void merge(Rating rating) {
		persistObject(rating);
	}
        
        @Override
	@Transactional
	public Rating findOrPersist(Rating rating) {
		SessionFactory sessionFactory = getSession();
		Session session = null;
		Rating storedRating = null;
		
		try{
			session = sessionFactory.openSession();
			Query query = session.createQuery("from Rating r where r.event = :event AND r.user = :user");
			query.setParameter("event", rating.getEvent().getId()).setParameter("user", rating.getUser().getId());
			storedRating = (Rating) query.uniqueResult();
                        if (storedRating == null) {
                            persistObject(rating);
			storedRating = entityManager.find(Rating.class, rating.getId());
		}
		}finally{
			if (session != null){
				session.close();
			}
		}
		
		return rating;
	}
	
	@Override
	@Transactional
	public Event findOrPersist(Event event) {
		Event storedEvent = entityManager.find(Event.class, event.getId());
		if (storedEvent == null){
			persistObject(event);
			storedEvent = entityManager.find(Event.class, event.getId());
		} else {
                    storedEvent.setIsStale(true);
                }
		
		return storedEvent;
	}

	@Override
	@Transactional
	public Place findOrPersist(Place place){
		Place storedPlace = entityManager.find(Place.class, place.getId());
		if (storedPlace == null){
			persistObject(place);
			storedPlace = entityManager.find(Place.class, place.getId());
		} else {
                    storedPlace.setIsStale(true);
                }
		
		return storedPlace;
	}

	@Override
	@Transactional
	public List<Event> findEvents(Date from, Date to) {
		SessionFactory sessionFactory = getSession();
		Session session = null;
		List<Event> result = null;
		
		try{
			session = sessionFactory.openSession();
			Query query = session.createQuery("from Event e where e.startDateTime BETWEEN :start AND :end ORDER BY e.startDateTime ");
			query.setParameter("start", from).setParameter("end", to);
			result = query.list();
		}finally{
			if (session != null){
				session.close();
			}
		}
		
		return result;
	}

    @Override
    public List<Event> findAll() {
        SessionFactory sessionFactory = getSession();
		Session session = null;
		List<Event> result = null;
		
		try{
			session = sessionFactory.openSession();
			Query query = session.createQuery("from Event e");
			result = query.list();
		}finally{
			if (session != null){
				session.close();
			}
		}
		
		return result;
    } 
}
