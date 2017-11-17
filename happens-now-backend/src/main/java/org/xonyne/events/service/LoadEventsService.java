package org.xonyne.events.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xonyne.events.dao.EventsDao;
import org.xonyne.events.jsonmapper.JsonReader;
import org.xonyne.events.jsonmapper.dto.EventUsersResult;
import org.xonyne.events.jsonmapper.dto.FacebookEvent;
import org.xonyne.events.jsonmapper.dto.FacebookEventLocation;
import org.xonyne.events.jsonmapper.dto.FacebookEventPlace;
import org.xonyne.events.jsonmapper.dto.FacebookEventResults;
import org.xonyne.events.jsonmapper.dto.FacebookUser;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Location;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.User;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoadEventsService {

	private Logger logger = org.slf4j.LoggerFactory.getLogger("LoadEventService");

	@Value("${loadEventsService.facebookEventsByLocationUrl}")
	private String facebookEventsByLocationUrl;
	@Value("${loadEventsService.facebookGraphApiUrl}")
	private String facebookGraphApiUrl;
	@Value("${loadEventsService.latitude}")
	private Double latitude;
	@Value("${loadEventsService.longitude}")
	private Double longitude;
	@Value("${loadEventsService.distance}")
	private Integer distance;
	@Value("${loadEventsService.facebookApiAccessToken}")
	private String facebookApiAccessToken;

	@Autowired
	private EventsDao eventsDao;
	
	public LoadEventsService() {
		logger.info("created successfully");
	}
	
	@PostConstruct
	public void init(){
		logger.info("init");
	}
	
	@Scheduled(fixedDelayString = "${loadEventsService.delayInMilliseconds}")
	public void loadEvents(){
		try{
			logger.debug("start load events");

			Set<FacebookEvent> allEvents = new HashSet<FacebookEvent>();
			FacebookEventResults facebookEvents;
			JSONObject events;
			ObjectMapper objectMapper;
			int duplicates=0;

			for (int i = 1; i <= 10; i++) {
				String url = getEventsUrl();
				
				if (logger.isDebugEnabled()){
					logger.debug("url:" + url.toString());
				}

				objectMapper = new ObjectMapper();
				events = JsonReader.readJsonFromUrl(url.toString());
				
				if (logger.isDebugEnabled()){
					logger.debug("events retreived");
				}

				facebookEvents = objectMapper.readValue(events.toString(),FacebookEventResults.class);

				if (logger.isDebugEnabled()){
					logger.debug("events parsed");
				}

				for (FacebookEvent facebookEvent: facebookEvents.events) {
					if (allEvents.contains(facebookEvent)) {
						duplicates++;
					}else {
						
						FacebookEventPlace facebookPlace = facebookEvent.place;
						FacebookEventLocation facebookLocation = facebookPlace.location;
						Place place = new Place(facebookPlace.id, facebookPlace.name, 
								new Location(null, facebookLocation.city, facebookLocation.country,
								facebookLocation.street, facebookLocation.zip, facebookLocation.latitude, 
								facebookLocation.longitude));
						place = eventsDao.findOrPersist(place);
						
						// load interested users
						logger.debug(" retreiving interested users for event:"+facebookEvent.id);
						url = getInterestedUsersUrl(facebookEvent.id);
						Set<User> interestedUsers = getUsers(objectMapper, url);
						Set<User> storedInterestedUsers = new HashSet<User>();
						
						for(User user:interestedUsers){
							User storedUser = eventsDao.findOrPersist(user);
							storedInterestedUsers.add(storedUser);
						}
					
						logger.debug(" retreiving attending users for event:"+facebookEvent.id);
						url = getAttendingUsersUrl(facebookEvent.id);
						Set<User> attendingUsers = getUsers(objectMapper, url);
						Set<User> storedAttendedUsers = new HashSet<User>();
						
						for(User user:attendingUsers){
							User storedUser = eventsDao.findOrPersist(user);
							storedAttendedUsers.add(storedUser);
						}
						
						Event event = new Event(facebookEvent.id, facebookEvent.name, facebookEvent.description, 
								facebookEvent.start_time, facebookEvent.end_time,null, null, interestedUsers, attendingUsers, place);
						eventsDao.persist(event);
					}
				}

				if (logger.isDebugEnabled()){
					logger.debug("events added to set");
				}

				logger.debug("-----------");
				logger.debug("Coordinates: https://www.google.ch/maps/@" + String.valueOf(latitude+ "," + String.valueOf(longitude)));
				logger.debug("Events in this run: " + facebookEvents.events.length);
				logger.debug("Duplicates in this run: " + duplicates);
				logger.debug("Events in total: " + allEvents.size());
			}
		}catch(Exception ex){
			logger.error("error in load events,"+ex.getMessage(), ex);
		}
	}

	private Set<User> getUsers(ObjectMapper objectMapper, String url)
			throws IOException, JsonParseException, JsonMappingException {
		
		Set<User> result = new HashSet<User>();
		JSONObject object = JsonReader.readJsonFromUrl(url.toString());
		EventUsersResult facbookUsers = objectMapper.readValue(object.toString(),EventUsersResult.class);
		
		for(FacebookUser facebookUser:facbookUsers.data){
			if (logger.isDebugEnabled()){
				logger.debug("  user:"+facebookUser.name);
			}
			
			User user = new User(facebookUser.id, facebookUser.name, null, null);
			result.add(user);
		}
		
		return result;
	}
	
	private String getEventsUrl() {
		StringBuilder url = new StringBuilder(facebookEventsByLocationUrl);
		url.append("?lat=").append(latitude).append("&lng=").append(longitude).
		append("&distance=").append(distance).append("&accessToken=").append(facebookApiAccessToken);
		return url.toString();
	}
	
	private String getInterestedUsersUrl(Long eventId) {
		StringBuilder url = new StringBuilder(facebookGraphApiUrl);
		url.append(eventId).append("/interested?").append("&access_token=").append(facebookApiAccessToken);
		return url.toString();
	}
	
	private String getAttendingUsersUrl(Long eventId) {
		StringBuilder url = new StringBuilder(facebookGraphApiUrl);
		url.append(eventId).append("/attending?").append("&access_token=").append(facebookApiAccessToken);
		return url.toString();
	}
	
}
