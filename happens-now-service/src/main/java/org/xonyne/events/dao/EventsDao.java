package org.xonyne.events.dao;

import java.util.Date;
import java.util.List;

import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.Rating;

public interface EventsDao {

	Place findOrPersist(Place place);
        
        Rating findOrPersist(Rating rating);

	Event findEvent(Event event);

	Event findOrPersist(Event event);

	void merge(Event event);
        
        void merge(Rating rating);

	List<Event> findEvents(Date from, Date to);
        
        List<Event> findAll();
}
