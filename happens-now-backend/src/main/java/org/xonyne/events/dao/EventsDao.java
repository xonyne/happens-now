package org.xonyne.events.dao;

import java.util.Set;

import org.xonyne.events.jsonmapper.dto.FacebookEvent;

public interface EventsDao {

	void persist(Set<FacebookEvent> allEvents);
}
