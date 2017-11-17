package org.xonyne.events.dao;

import org.xonyne.events.jsonmapper.dto.FacebookEvent;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.User;

public interface EventsDao {

	void persist(Event event);

	User findOrPersist(User user);

	Place findOrPersist(Place place);
}
