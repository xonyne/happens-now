package org.xonyne.events.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.xonyne.commons.Utils;
import org.xonyne.events.dao.EventsDao;
import org.xonyne.events.dao.UsersDao;
import org.xonyne.events.dto.EventDto;
import org.xonyne.events.dto.UserDto;
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

	private Logger loadEventsLogger = org.slf4j.LoggerFactory.getLogger("LoadEventService");
	private Logger logger = org.slf4j.LoggerFactory.getLogger(LoadEventsService.class);

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
	
	@Value("${loadEventsService.tonightStartHour}")
	private Integer tonightStartHour;


	@Autowired
	private EventsDao eventsDao;

	@Autowired
	private UsersDao usersDao;

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
			loadEventsLogger.debug("start load events");

			Set<FacebookEvent> allEvents = new HashSet<FacebookEvent>();
			FacebookEventResults facebookEvents;
			JSONObject events;
			ObjectMapper objectMapper;
			int duplicates=0;

			String url = getEventsUrl();

			if (loadEventsLogger.isDebugEnabled()){
				loadEventsLogger.debug("url:" + url.toString());
			}

			objectMapper = new ObjectMapper();
			events = JsonReader.readJsonFromUrl(url);

			if (loadEventsLogger.isDebugEnabled()){
				loadEventsLogger.debug("events retreived");
			}

			facebookEvents = objectMapper.readValue(events.toString(),FacebookEventResults.class);

			if (loadEventsLogger.isDebugEnabled()){
				loadEventsLogger.debug("events parsed");
			}

			for (FacebookEvent facebookEvent: facebookEvents.events) {
				try{
					loadEventsLogger.debug("storing information of event id:" + facebookEvent.id);

					if (allEvents.contains(facebookEvent)) {
						duplicates++;
						loadEventsLogger.debug(" duplicated event, id:" + facebookEvent.id);
					}else {
						FacebookEventPlace facebookPlace = facebookEvent.place;
						FacebookEventLocation facebookLocation = facebookPlace.location;
						Place place = new Place(facebookPlace.id, facebookPlace.name, 
								new Location(null, facebookLocation.city, facebookLocation.country,
										facebookLocation.street, facebookLocation.zip, facebookLocation.latitude, 
										facebookLocation.longitude));
						place = eventsDao.findOrPersist(place);

						// load interested users
						loadEventsLogger.debug(" retreiving interested users for event:"+facebookEvent.id);
						url = getInterestedUsersUrl(facebookEvent.id);
						Set<User> interestedUsers = getUsers(objectMapper, url);
						Set<User> storedInterestedUsers = new HashSet<User>();

						for(User user:interestedUsers){
							User storedUser = usersDao.findOrPersist(user);
							storedInterestedUsers.add(storedUser);
						}

						loadEventsLogger.debug(" retreiving attending users for event:"+facebookEvent.id);
						url = getAttendingUsersUrl(facebookEvent.id);
						Set<User> attendingUsers = getUsers(objectMapper, url);
						Set<User> storedAttendedUsers = new HashSet<User>();

						for(User user:attendingUsers){
							User storedUser = usersDao.findOrPersist(user);
							storedAttendedUsers.add(storedUser);
						}

						Event event = new Event(facebookEvent.id, facebookEvent.name, facebookEvent.description, 
								facebookEvent.startTime, facebookEvent.endTime,null, null, null, null, place);

						loadEventsLogger.debug(" persist event");
						event = eventsDao.findOrPersist(event);
						event.setAttendingUsers(storedAttendedUsers);
						event.setInterestedUsers(storedInterestedUsers);
						loadEventsLogger.debug(" add attended and interested users");
						eventsDao.merge(event);
						loadEventsLogger.debug(" event stored successfully");
					}
				}catch (Exception e) {
					logger.error("error in storing information of event id:" + facebookEvent.id + "," + e.getMessage(), e);
				}

				loadEventsLogger.debug("-----------");
				loadEventsLogger.debug("Coordinates: https://www.google.ch/maps/@" + String.valueOf(latitude+ "," + String.valueOf(longitude)));
				loadEventsLogger.debug("Events in this run: " + facebookEvents.events.length);
				loadEventsLogger.debug("Duplicates in this run: " + duplicates);
				loadEventsLogger.debug("Events in total: " + allEvents.size());
			}
		}catch(Exception ex){
			logger.error("error in load events,"+ex.getMessage(), ex);
		}
	}

	/**
	 * Reads the list of users using the provided URL. 
	 */
	private Set<User> getUsers(ObjectMapper objectMapper, String url)
			throws IOException, JsonParseException, JsonMappingException {

		Set<User> result = new HashSet<User>();
		while(url != null && !url.isEmpty()){
			logger.debug("load users from url :" + url);
			
			JSONObject object = JsonReader.readJsonFromUrl(url);
			EventUsersResult facbookUsers = objectMapper.readValue(object.toString(),EventUsersResult.class);

			for(FacebookUser facebookUser:facbookUsers.data){
				if (logger.isDebugEnabled()){
					logger.debug("  user:"+facebookUser.name);
				}

				User user = new User(facebookUser.id, facebookUser.name, null, null);
				result.add(user);
			}

			// Supports Facebook cursor pagination
			url = facbookUsers.paging != null? facbookUsers.paging.next : null;
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

	public List<EventDto> getTodayEvents(){
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);

		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);

		List<EventDto> result = findEvents(start.getTime(), end.getTime());

		return result;
	}

	public List<EventDto> getTonightEvents(){
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, tonightStartHour);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);

		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);

		List<EventDto> result = findEvents(start.getTime(), end.getTime());

		return result;
	}
	
	public List<EventDto> getWeekendEvents() {
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

		List<EventDto> result = findEvents(start.getTime(), end.getTime());

		return result;
	}

	public List<EventDto> getNextWeekendEvents() {
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		int daysToStartNextWeekend = Calendar.SUNDAY - start.get(Calendar.DAY_OF_WEEK) + Calendar.SATURDAY;
		start.add(Calendar.DAY_OF_MONTH, daysToStartNextWeekend);

		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		int daysToEndNextWeekend = Calendar.SUNDAY - start.get(Calendar.DAY_OF_WEEK) + Calendar.SUNDAY;
		start.add(Calendar.DAY_OF_MONTH, daysToEndNextWeekend);

		if (logger.isDebugEnabled()){
			logger.debug(" start/end dates of next weekend:" + start.getTime() +","+ end.getTime());
		}

		List<EventDto> result = findEvents(start.getTime(), end.getTime());

		return result;
	}

	public List<EventDto> getEvents(Date start, Date end) {
		if (logger.isDebugEnabled()){
			logger.debug(" get events between :" + start.getTime() +","+ end.getTime());
		}

		List<EventDto> result = findEvents(start, end);

		return result;
	}
	
	private List<EventDto> findEvents(Date start, Date end) {
		List<Event> events = eventsDao.findEvents(start, end);
		List<EventDto> result = new ArrayList<EventDto>(events.size());

		for(Event event:events){
			result.add(new EventDto(event.getEventId(), event.getTitle(), event.getStartDateTime(), event.getEndDateTime()));
		}

		logger.debug(" retreived result count:" + result.size());

		return result;
	}	

	public UserDto findUser(String userName, String password){
		try {
			password = Utils.encodeAsMD5(password);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("error in find user");
		}
		
		User user = usersDao.find(userName, password);
		UserDto userDto =  null;
		
		if (user!= null){
			userDto = new UserDto(user.getId(), user.getName(), user.getUserName());
		}
		
		return userDto; 
	}
}
