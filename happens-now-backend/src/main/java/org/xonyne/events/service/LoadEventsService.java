package org.xonyne.events.service;

import java.util.HashSet;
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
import org.xonyne.events.dao.EventsDao;
import org.xonyne.events.jsonmapper.JsonReader;
import org.xonyne.events.jsonmapper.dto.EventUsersResult;
import org.xonyne.events.jsonmapper.dto.FacebookEvent;
import org.xonyne.events.jsonmapper.dto.FacebookEventResults;
import org.xonyne.events.jsonmapper.dto.User;

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

				for (int j = 0; j < facebookEvents.events.length; j++) {
					FacebookEvent currentEvent = facebookEvents.events[j];
					if (allEvents.contains(currentEvent)) {
						duplicates++;
					}else {
						allEvents.add(currentEvent);
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
				
				// load interested users
				// @TODO handle pagination
				for(FacebookEvent event:allEvents){
					logger.debug(" retreiving interested users for event:"+event.id);
					
					url = getInterestedUsersUrl(event.id);
					events = JsonReader.readJsonFromUrl(url.toString());
					EventUsersResult interestedUsers = objectMapper.readValue(events.toString(),EventUsersResult.class);
					for(User user:interestedUsers.data){
						logger.debug("  user:"+user.name);
					}
				}
				
				// load attending users
				// @TODO handle pagination
				for(FacebookEvent event:allEvents){
					logger.debug(" retreiving attending users for event:"+event.id);
					
					url = getInterestedUsersUrl(event.id);
					events = JsonReader.readJsonFromUrl(url.toString());
					EventUsersResult interestedUsers = objectMapper.readValue(events.toString(),EventUsersResult.class);
					for(User user:interestedUsers.data){
						logger.debug("  user:"+user.name);
					}
				}
				
				
				
				// @TODO Filter events that habe been stored in DB before
				storeToDB(allEvents);
			}
		}catch(Exception ex){
			logger.error("error in load events,"+ex.getMessage(), ex);
		}
	}

	private String getEventsUrl() {
		StringBuilder url = new StringBuilder(facebookEventsByLocationUrl);
		url.append("?lat=").append(latitude).append("&lng=").append(longitude).
		append("&distance=").append(distance).append("&accessToken=").append(facebookApiAccessToken);
		return url.toString();
	}
	
	private String getInterestedUsersUrl(String eventId) {
		StringBuilder url = new StringBuilder(facebookGraphApiUrl);
		url.append(eventId).append("/interested?").append("&access_token=").append(facebookApiAccessToken);
		return url.toString();
	}
	
	private String getAttendingUsersUrl(String eventId) {
		StringBuilder url = new StringBuilder(facebookGraphApiUrl);
		url.append(eventId).append("/attending?").append("&access_token=").append(facebookApiAccessToken);
		return url.toString();
	}
	
	
	
	private void storeToDB(Set<FacebookEvent> allEvents){
		for(FacebookEvent event:allEvents){
			logger.debug(event.toString());
		}
	}
}
