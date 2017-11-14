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
import org.xonyne.events.jsonmapper.dto.FacebookEvent;
import org.xonyne.events.jsonmapper.dto.FacebookEventResults;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoadEventsService {

	private Logger logger = org.slf4j.LoggerFactory.getLogger("LoadEventService");

	@Value("${loadEventsService.facebookEventsByLocationUrl}")
	private String facebookEventsByLocationUrl;
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
				StringBuilder url = new StringBuilder(facebookEventsByLocationUrl);
				url.append("?lat=").append(latitude).append("&lng=").append(longitude).
				append("&distance=").append(distance).append("&accessToken=").append(facebookApiAccessToken);
				
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
				
				storeToDB(allEvents);
			}
		}catch(Exception ex){
			logger.error("error in load events,"+ex.getMessage(), ex);
		}
	}
	
	private void storeToDB(Set<FacebookEvent> allEvents){
		for(FacebookEvent event:allEvents){
			logger.debug(event.toString());
		}
	}
}
