package org.xonyne.events.dao;

import java.util.Date;
import java.util.List;

import org.xonyne.events.dto.EventDto;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.User;

public interface EventsDao {

	User findOrPersist(User user);

	Place findOrPersist(Place place);

	Event findEvent(Event event);

	Event findOrPersist(Event event);

	void merge(Event event);

	List<Event> findEvents(Date from, Date to);
}
