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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.logging.Level;
import org.xonyne.events.model.City;
import org.xonyne.events.model.Rating;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoadEventsService {

    private final Logger loadEventsLogger = org.slf4j.LoggerFactory.getLogger("LoadEventService");
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(LoadEventsService.class);

    @Value("${loadEventsService.facebookEventsByLocationUrl}")
    private String facebookEventsByLocationUrl;
    @Value("${loadEventsService.facebookGraphApiUrl}")
    private String facebookGraphApiUrl;
    @Value("${loadEventsService.cityNames}")
    private String cityNames;
    @Value("${loadEventsService.cityLatitudes}")
    private String cityLatitudes;
    @Value("${loadEventsService.cityLongitudes}")
    private String cityLongitudes;
    @Value("${loadEventsService.distance}")
    private Integer distance;
    @Value("${loadEventsService.facebookApiAccessToken}")
    private String facebookApiAccessToken;
    @Value("${loadEventsService.tonightStartHour}")
    private Integer tonightStartHour;
    @Value("${loadEventsService.defaultCountry}")
    private String defaultCountry;
    @Value("${loadEventsService.defaultStreet}")
    private String defaultStreet;
    @Value("${loadEventsService.fetchTimePeriodInDays}")
    private Integer fetchTimePeriodInDays;
    @Value("${loadEventsService.delayBetweenGraphAPICallsInMs}")
    private Integer delayBetweenGraphAPICallsInMs;
    @Value("${loadEventsService.interestedRatingModifier}")
    private Integer interestedRatingModifier;
    @Value("${loadEventsService.attendingRatingModifier}")
    private Integer attendingRatingModifier;

    LocalDateTime startDate;
    LocalDateTime endDate;

    List<City> cityList;
    private String selectedCity;

    ObjectMapper objectMapper;

    @Autowired
    private EventsDao eventsDao;

    @Autowired
    private UsersDao usersDao;

    private final double LAT_100_METRES = 0.00089d;
    private final double LNG_100_METRES = 0.001275d;

    public LoadEventsService() {
        logger.info("LoadEventsService created successfully");
    }

    @PostConstruct
    public void init() {
        logger.info("LoadEventsService initialized");
        startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        endDate = LocalDateTime.of(LocalDate.now().plusDays(Long.valueOf(fetchTimePeriodInDays)), LocalTime.of(23, 59));
        objectMapper = new ObjectMapper();
        loadCities();
        this.selectedCity="";
    }

    private void loadCities() {
        String[] cities = this.cityNames.split(",", 0);
        String[] latitudes = this.cityLatitudes.split(",", 0);
        String[] longitudes = this.cityLongitudes.split(",", 0);
        this.cityList = new ArrayList<>();
        for (int i = 0; i < cities.length; i++) {
            this.cityList.add(new City(cities[i].trim(), latitudes[i].trim(), longitudes[i].trim()));
        }
    }

    public List<String> getCityNames() {
        List<String> cities = new ArrayList<>();
        this.cityList.forEach((c) -> {
            cities.add(c.getName());
        });
        return cities;
    }

    // --> load events every night 01:00 AM
    //@Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(fixedDelay = 3600000, initialDelay = 1000)
    public void loadEvents() {
        loadEventsLogger.debug("%%%%% %%%%% %%%%%  LOAD EVENTS METHOD CALLED %%%%% %%%%% %%%%%");
        JSONObject jsonData;
        FacebookEventResults facebookEvents;

        LoadEventsServiceStatistics eventStats = new LoadEventsServiceStatistics();
        for (City currentCity : this.cityList) {

            loadEventsLogger.info("***** ***** ***** START READ EVENTS FROM " + currentCity.getName() + " ***** ***** *****");
            for (int lng = -25; lng < 25; lng++) {
                double currentLongitude = Double.valueOf(currentCity.getLongitude()) + (lng * LNG_100_METRES);
                for (int lat = -25; lat < 25; lat++) {
                    double currentLatitude = Double.valueOf(currentCity.getLatitude()) + (lat * LAT_100_METRES);
                    try {
                        loadEventsLogger.debug("starting to load events");

                        String url = getEventsUrl(String.valueOf(currentLatitude), String.valueOf(currentLongitude));

                        if (loadEventsLogger.isDebugEnabled()) {
                            loadEventsLogger.debug("url:" + url);
                        }

                        jsonData = JsonReader.readJsonFromUrl(url);
                        eventStats.increaseGraphApiCalls(1);

                        if (loadEventsLogger.isDebugEnabled()) {
                            loadEventsLogger.debug("json data retreived from facebook ");
                        }

                        facebookEvents = objectMapper.readValue(jsonData.toString(), FacebookEventResults.class);
                        eventStats.increaseTotalEvents(facebookEvents.events.length);

                        if (loadEventsLogger.isDebugEnabled()) {
                            loadEventsLogger.debug("facebook events parsed");
                        }

                        loadEventsLogger.info("Latitude: " + currentLatitude);
                        loadEventsLogger.info("Longitude: " + currentLongitude);
                        loadEventsLogger.info("Events retreived: " + facebookEvents.events.length);

                        for (FacebookEvent facebookEvent : facebookEvents.events) {
                            try {
                                loadEventsLogger.debug("storing information of event id:" + facebookEvent.id);
                                Place place = findOrStorePlace(currentCity.getName(), facebookEvent);
                                if (!place.getIsStale()) {
                                    eventStats.increaseNewPlaces(1);
                                }
                                Event event = findOrStoreEvent(facebookEvent, place);
                                if (!event.getIsStale()) {
                                    eventStats.increaseNewEvents(1);
                                }

                                eventsDao.merge(event);
                                loadEventsLogger.debug(" event " + event + " stored successfully");

                            } catch (Exception e) {
                                logger.error("error in storing information of event id:" + facebookEvent.id + "," + e.getMessage(), e);
                            }

                        }
                    } catch (Exception ex) {
                        logger.error("error in load events," + ex.getMessage(), ex);
                    } finally {
                        try {
                            // maybe dangerous because of blockage of the service (login may be blocked)
                            Thread.sleep(delayBetweenGraphAPICallsInMs);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(LoadEventsService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            loadEventsLogger.info("***** ***** ***** END READ EVENTS FROM " + currentCity.getName() + " ***** ***** *****");
        }
        loadEventsLogger.info("%%%%% %%%%% %%%%% LOAD EVENTS SERVICE INVOCATION STATISTICS %%%%% %%%%% %%%%%");
        loadEventsLogger.info("Total events: " + eventStats.getTotalEvents());
        loadEventsLogger.info("Unique events: " + eventStats.getNewEvents());
    }

    // --> load users and ratings every night 05:00 AM
    //@Scheduled(cron = "0 0 5 * * ?")
    //@Scheduled(fixedDelay = 3600000, initialDelay = 1000)
    public void loadUsersAndRatings() {
        loadEventsLogger.debug("&&&&&&&&& &&&&&&&&& &&&&&&&&&  LOAD USERS AND RATINGS METHOD CALLED &&&&&&&&& &&&&&&&&& &&&&&&&&& ");
        LoadEventsServiceStatistics userAndRatingStats = new LoadEventsServiceStatistics();

        List<Event> allEvents = eventsDao.findAll();
        for (Event event : allEvents) {
            Set<User> storedInterestedUsers = findOrStoreInterestedUsers(event.getId());
            Set<User> storedAttendendingUsers = findOrStoreAttendingUsers(event.getId());
            event.setAttendingUsers(storedAttendendingUsers);
            event.setInterestedUsers(storedInterestedUsers);
            loadEventsLogger.debug(" add attending and interested users to event " + event);

            for (User user : storedInterestedUsers) {
                if (!user.getIsStale()) {
                    userAndRatingStats.increaseNewUsers(1);
                }
                Rating rating = new Rating();
                rating.setEvent(event);
                rating.setUser(user);
                rating = eventsDao.findOrPersist(rating);
                if (rating.getRating() < interestedRatingModifier) {
                    rating.setRating(interestedRatingModifier);
                    eventsDao.merge(rating);
                }
            }
            loadEventsLogger.debug(" interested ratings for event " + event + " stored successfully");

            for (User user : storedAttendendingUsers) {
                if (!user.getIsStale()) {
                    userAndRatingStats.increaseNewUsers(1);
                }
                Rating rating = new Rating();
                rating.setEvent(event);
                rating.setUser(user);
                rating = eventsDao.findOrPersist(rating);
                if (rating.getRating() < attendingRatingModifier) {
                    rating.setRating(attendingRatingModifier);
                    eventsDao.merge(rating);
                }
            }
            loadEventsLogger.debug(" attending ratings for event " + event + " stored successfully");
            try {
                // maybe dangerous because of blockage of the service (login may be blocked)
                Thread.sleep(delayBetweenGraphAPICallsInMs);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(LoadEventsService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        loadEventsLogger.debug("&&&&&&&&& &&&&&&&&& &&&&&&&&&  LOAD USERS AND RATINGS INVOCATION STATISTICS &&&&&&&&& &&&&&&&&& &&&&&&&&& ");
        loadEventsLogger.debug("Unique users: " + userAndRatingStats.getNewUsers());
    }

    private Set<User> findOrStoreAttendingUsers(Long eventId) {
        try {
            String url;
            loadEventsLogger.debug(" retreiving attending users for event:" + eventId);
            url = getAttendingUsersUrl(eventId);
            Set<User> attendingUsers = getUsers(this.objectMapper, url);
            Set<User> storedAttendedUsers = new HashSet<>();
            for (User user : attendingUsers) {
                User storedUser = usersDao.findOrPersist(user);
                storedAttendedUsers.add(storedUser);
            }
            return storedAttendedUsers;

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LoadEventsService.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Set<User> findOrStoreInterestedUsers(Long eventId) {
        try {
            String url;
            // load interested users
            loadEventsLogger.debug(" retreiving interested users for event:" + eventId);
            url = getInterestedUsersUrl(eventId);
            Set<User> interestedUsers = getUsers(this.objectMapper, url);
            Set<User> storedInterestedUsers = new HashSet<>();
            for (User user : interestedUsers) {
                User storedUser = usersDao.findOrPersist(user);
                storedInterestedUsers.add(storedUser);
            }
            return storedInterestedUsers;

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LoadEventsService.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Event findOrStoreEvent(FacebookEvent facebookEvent, Place place) {
        // create event
        Event event = new Event(facebookEvent.id, facebookEvent.name, facebookEvent.description,
                facebookEvent.startTime, facebookEvent.endTime, null, null, null, null, place);
        loadEventsLogger.debug(" persist event");
        event = eventsDao.findOrPersist(event);
        return event;
    }

    private Place findOrStorePlace(String currentCity, FacebookEvent facebookEvent) {
        // create place
        FacebookEventPlace facebookPlace = facebookEvent.place;
        FacebookEventLocation facebookLocation = facebookPlace.location;

        // some events have no city set. Prevent NullPointerException.
        if (facebookPlace.location.city == null) {
            facebookPlace.location.city = currentCity;
        }
        // some events have no country set. Prevent NullPointerException.
        if (facebookPlace.location.country == null) {
            facebookPlace.location.country = this.defaultCountry;
        }
        // some events have no street set. Prevent NullPointerException.
        if (facebookPlace.location.street == null) {
            facebookPlace.location.street = this.defaultStreet;
        }

        Place place = new Place(facebookPlace.id, facebookPlace.name,
                new Location(null, facebookLocation.city, facebookLocation.country,
                        facebookLocation.street, facebookLocation.zip, facebookLocation.latitude,
                        facebookLocation.longitude));
        place = eventsDao.findOrPersist(place);
        return place;
    }

    /**
     * Reads the list of users using the provided URL.
     */
    private Set<User> getUsers(ObjectMapper objectMapper, String url)
            throws IOException, JsonParseException, JsonMappingException {

        Set<User> result = new HashSet<>();
        while (url != null && !url.isEmpty()) {
            logger.debug("load users from url :" + url);

            JSONObject object = JsonReader.readJsonFromUrl(url);
            EventUsersResult facbookUsers = objectMapper.readValue(object.toString(), EventUsersResult.class
            );

            for (FacebookUser facebookUser : facbookUsers.data) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  user:" + facebookUser.name);
                }

                User user = new User(facebookUser.id, facebookUser.name, null, null);
                result.add(user);
            }

            // Supports Facebook cursor pagination
            url = facbookUsers.paging != null ? facbookUsers.paging.next : null;
        }

        return result;
    }

    public String getEventsUrl(String latitude, String longitude) {
        StringBuilder url = new StringBuilder(facebookEventsByLocationUrl);
        url
                .append("?lat=").append(latitude)
                .append("&lng=").append(longitude)
                .append("&distance=").append(distance)
                .append("&accessToken=").append(facebookApiAccessToken)
                .append("&since=").append(startDate.toEpochSecond(ZoneOffset.UTC))
                .append("&until=").append(endDate.toEpochSecond(ZoneOffset.UTC));
        return url.toString();
    }

    public String getInterestedUsersUrl(Long eventId) {
        StringBuilder url = new StringBuilder(facebookGraphApiUrl);
        url
                .append(eventId).append("/interested?")
                .append("&access_token=").append(facebookApiAccessToken);
        return url.toString();
    }

    public String getAttendingUsersUrl(Long eventId) {
        StringBuilder url = new StringBuilder(facebookGraphApiUrl);
        url
                .append(eventId).append("/attending?")
                .append("&access_token=").append(facebookApiAccessToken);
        return url.toString();
    }

    public List<EventDto> getTodayEvents() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        List<EventDto> result;
        if (this.selectedCity.isEmpty()) {
            result = findEvents(start.getTime(), end.getTime());
        } else {
            result = findEventsInCity(start.getTime(), end.getTime(), this.selectedCity);
        }
 

        return result;
    }

    public List<EventDto> getTonightEvents() {
        Calendar start = (Calendar) Calendar.getInstance().clone();
        start.set(Calendar.HOUR_OF_DAY, tonightStartHour);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        
        List<EventDto> result;
        if (this.selectedCity.isEmpty()) {
            result = findEvents(start.getTime(), end.getTime());
        } else {
            result = findEventsInCity(start.getTime(), end.getTime(), this.selectedCity);
        }

        return result;
    }

    /**
     * Weekend start : Saturday in current week Weekend end : Sunday in next
     * week
     * @return 
     */
    public List<EventDto> getWeekendEvents() {
        Calendar start = (Calendar) Calendar.getInstance().clone();
        start.set(Calendar.HOUR_OF_DAY, tonightStartHour);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.DAY_OF_WEEK, 6);
        start.set(Calendar.WEEK_OF_YEAR, start.get(Calendar.WEEK_OF_YEAR));

        Calendar end = (Calendar) Calendar.getInstance().clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.DAY_OF_WEEK, 1);
        end.set(Calendar.WEEK_OF_YEAR, start.get(Calendar.WEEK_OF_YEAR) + 1);

        if (logger.isDebugEnabled()) {
            logger.debug(" start/end dates of next weekend:" + start.getTime() + "," + end.getTime());
        }
        
        List<EventDto> result;
        if (this.selectedCity.isEmpty()) {
            result = findEvents(start.getTime(), end.getTime());
        } else {
            result = findEventsInCity(start.getTime(), end.getTime(), this.selectedCity);
        }
 
        return result;
    }

    public List<EventDto> getNextWeekendEvents() {
        Calendar start = (Calendar) Calendar.getInstance().clone();
        start.set(Calendar.HOUR_OF_DAY, tonightStartHour);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.DAY_OF_WEEK, 6);
        start.set(Calendar.WEEK_OF_YEAR, start.get(Calendar.WEEK_OF_YEAR) + 1);

        Calendar end = (Calendar) Calendar.getInstance().clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.DAY_OF_WEEK, 1);
        end.set(Calendar.WEEK_OF_YEAR, start.get(Calendar.WEEK_OF_YEAR) + 1);

        if (logger.isDebugEnabled()) {
            logger.debug(" start/end dates of next weekend:" + start.getTime() + "," + end.getTime());
        }

        List<EventDto> result;
        if (this.selectedCity.isEmpty()) {
            result = findEvents(start.getTime(), end.getTime());
        } else {
            result = findEventsInCity(start.getTime(), end.getTime(), this.selectedCity);
        }
 

        return result;
    }

    public List<EventDto> getEvents(Date start, Date end) {
        if (logger.isDebugEnabled()) {
            logger.debug(" get events between :" + start.getTime() + "," + end.getTime());
        }

        List<EventDto> result;
        if (this.selectedCity.isEmpty()) {
            result = findEvents(start, end);
        } else {
            result = findEventsInCity(start, end, this.selectedCity);
        }

        return result;
    }

    private List<EventDto> findEventsInCity(Date start, Date end, String city) {
        List<Event> events = eventsDao.findEventsInCity(start, end, city);
        List<EventDto> result = new ArrayList<>(events.size());

        for (Event event : events) {
            result.add(new EventDto(event.getId(), event.getTitle(), event.getStartDateTime(), event.getEndDateTime()));
        }

        logger.debug(" retreived result count:" + result.size());

        return result;
    }

    private List<EventDto> findEvents(Date start, Date end) {
        List<Event> events = eventsDao.findEvents(start, end);
        List<EventDto> result = new ArrayList<>(events.size());

        for (Event event : events) {
            result.add(new EventDto(event.getId(), event.getTitle(), event.getStartDateTime(), event.getEndDateTime()));
        }

        logger.debug(" retreived result count:" + result.size());

        return result;
    }

    public UserDto findUser(String userName, String password) {
        try {
            password = Utils.encodeAsMD5(password);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("error in find user");
        }

        User user = usersDao.find(userName, password);
        UserDto userDto = null;

        if (user != null) {
            userDto = new UserDto(user.getId(), user.getName(), user.getUserName());
        }

        return userDto;
    }

    public String getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
    }
}
