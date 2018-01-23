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
import java.util.Arrays;
import java.util.logging.Level;
import org.springframework.scheduling.annotation.Scheduled;
import org.xonyne.events.model.City;
import org.xonyne.events.model.Rating;
import org.xonyne.events.service.util.LoadEventsServiceStatistics;

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
    @Value("${loadEventsService.square100mStepsFromCenter}")
    private Integer square100mStepsFromCenter;
    @Value("${loadEventsService.lat100m}")
    private Double lat_100_METRES;
    @Value("${loadEventsService.lng100m}")
    private Double lng_100_METRES;

    LocalDateTime startDate;
    LocalDateTime endDate;

    List<City> cityList;
    private String selectedCity;

    ObjectMapper objectMapper;

    @Autowired
    private EventsDao eventsDao;

    @Autowired
    private UsersDao usersDao;

    private final String FB_BASE_URL = "https://www.facebook.com/";

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
        this.selectedCity = "";
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
    @Scheduled(fixedDelay = 3600000, initialDelay = 1000)
    public void loadEvents() {
        loadEventsLogger.debug("%%%%% %%%%% %%%%%  LOAD EVENTS METHOD CALLED %%%%% %%%%% %%%%%");
        JSONObject jsonData;
        FacebookEventResults facebookEvents;

        LoadEventsServiceStatistics eventStats = new LoadEventsServiceStatistics();
        int totalSteps = (square100mStepsFromCenter * 2 * square100mStepsFromCenter * 2) * this.cityList.size();
        int currentStep = 0;
        long totalTimeAllCycles = 0;
        long startTimeOfCycle = 0;
        double currentLongitude;
        double currentLatitude;
        Set<FacebookEvent> eventsOfLastCall = new HashSet<>();
        boolean lastCallOver = false;
        LocalTime currentDay;
        
        for (City currentCity : this.cityList) {
            loadEventsLogger.info("***** ***** ***** START READ EVENTS FROM " + currentCity.getName() + " ***** ***** *****");
            for (int lng = -square100mStepsFromCenter; lng < square100mStepsFromCenter; lng++) {
                currentLongitude = Double.valueOf(currentCity.getLongitude()) + (lng * lng_100_METRES);
                for (int lat = -square100mStepsFromCenter; lat < square100mStepsFromCenter; lat++) {
                    currentLatitude = Double.valueOf(currentCity.getLatitude()) + (lat * lat_100_METRES);
                    try {
                        startTimeOfCycle = System.currentTimeMillis();
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
                        currentStep++;
                        loadEventsLogger.info("---");
                        loadEventsLogger.info("Step: " + currentStep + "/" + totalSteps);
                        LocalTime timeOfDay = LocalTime.ofSecondOfDay((((totalSteps - currentStep) * delayBetweenGraphAPICallsInMs) + ((totalSteps - currentStep) * (totalTimeAllCycles / currentStep))) / 1000);
                        loadEventsLogger.info("Time left: " + timeOfDay.toString());
                        loadEventsLogger.info("Location: https://www.google.com/maps/search/?api=1&query=" + currentLatitude + "," + currentLongitude);
                        loadEventsLogger.info("Events retreived: " + facebookEvents.events.length);

                        for (FacebookEvent facebookEvent : facebookEvents.events) {
                            try {

                                loadEventsLogger.debug("storing place of event " + facebookEvent);
                                Place place = findOrStorePlace(facebookEvent.place);
                                if (!place.getIsStale()) {
                                    eventStats.increaseNewPlaces(1);
                                }

                                loadEventsLogger.debug("storing event " + facebookEvent);
                                Event event = findOrStoreEvent(facebookEvent, place);
                                if (!event.getIsStale()) {
                                    eventStats.increaseNewEvents(1);
                                    eventStats.increaseTotalNewEvents(1);
                                }

                                if (eventsOfLastCall.contains(facebookEvent)) {
                                    eventStats.increaseDuplicateEvents(1);
                                }

                            } catch (Exception e) {
                                logger.error("error in storing information of event id:" + facebookEvent.id + "," + e.getMessage(), e);
                            }

                        }
                        loadEventsLogger.info("Duplicate events: " + eventStats.getDuplicateEvents());
                        loadEventsLogger.info("New events: " + eventStats.getNewEvents());
                        eventStats.resetDuplicateEvents();
                        eventStats.resetNewEvents();

                        eventsOfLastCall.clear();
                        eventsOfLastCall.addAll(Arrays.asList(facebookEvents.events));

                        totalTimeAllCycles += System.currentTimeMillis() - startTimeOfCycle;
                    } catch (Exception ex) {
                        logger.error("error in load events," + ex.getMessage(), ex);
                    } finally {
                        try {
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
        loadEventsLogger.info("Total new events: " + eventStats.getTotalNewEvents());
    }

    public void loadUsersAndParticipation(Event event) {
        loadEventsLogger.info("&&&&&&&&& &&&&&&&&& &&&&&&&&&  LOAD USERS AND PARTICIPATION METHOD CALLED &&&&&&&&& &&&&&&&&& &&&&&&&&& ");
        LoadEventsServiceStatistics userStats = new LoadEventsServiceStatistics();

        loadEventsLogger.debug(" retreiving interested users for event: " + event);
        Set<User> storedInterestedUsers = new HashSet<>();
        try {
            String interestedUsersURL = getInterestedUsersUrl(event.getId());
            Set<User> interestedUsers = getUsers(interestedUsersURL);
            for (User user : interestedUsers) {
                User storedUser = usersDao.findOrPersist(user);
                storedInterestedUsers.add(storedUser);

                if (!storedUser.getIsStale()) {
                    userStats.increaseNewUsers(1);
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LoadEventsService.class.getName()).log(Level.SEVERE, null, ex);
        }

        loadEventsLogger.debug(" retreiving attending users for event: " + event);
        Set<User> storedAttendingUsers = new HashSet<>();
        try {
            String attendingUsersURL = getAttendingUsersUrl(event.getId());
            Set<User> attendingUsers = getUsers(attendingUsersURL);
            for (User user : attendingUsers) {
                User storedUser = usersDao.findOrPersist(user);
                storedAttendingUsers.add(storedUser);

                if (!storedUser.getIsStale()) {
                    userStats.increaseNewUsers(1);
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LoadEventsService.class.getName()).log(Level.SEVERE, null, ex);
        }

        loadEventsLogger.debug(" adding attending and interested users for event " + event);
        event.setInterestedUsers(storedInterestedUsers);
        event.setAttendingUsers(storedAttendingUsers);
        eventsDao.merge(event);

        loadEventsLogger.info("&&&&&&&&& &&&&&&&&& &&&&&&&&&  LOAD USERS AND PARTICIPATION INVOCATION STATISTICS &&&&&&&&& &&&&&&&&& &&&&&&&&& ");
        loadEventsLogger.info("Total new users: " + userStats.getNewUsers());
    }

    private Event findOrStoreEvent(FacebookEvent facebookEvent, Place place) {
        // create event
        Event event = new Event(facebookEvent.id, facebookEvent.name, facebookEvent.description,
                facebookEvent.startTime, facebookEvent.endTime, this.FB_BASE_URL + facebookEvent.id, null, null, null, place, null);
        loadEventsLogger.debug(" persist event");
        event = eventsDao.findOrPersist(event);
        return event;
    }

    private Place findOrStorePlace(FacebookEventPlace facebookPlace) {

        // some events have no city set. Prevent NullPointerException.
        if (facebookPlace.location.city == null) {
            facebookPlace.location.city = this.selectedCity;
        }
        // some events have no zip set. Prevent NullPointerException.
        if (facebookPlace.location.zip == null) {
            facebookPlace.location.zip = "0000";
        }
        // some events have no country set. Prevent NullPointerException.
        if (facebookPlace.location.country == null) {
            facebookPlace.location.country = this.defaultCountry;
        }
        // some events have no street set. Prevent NullPointerException.
        if (facebookPlace.location.street == null) {
            facebookPlace.location.street = this.defaultStreet;
        }

        FacebookEventLocation facebookLocation = facebookPlace.location;
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
    private Set<User> getUsers(String url)
            throws IOException, JsonParseException, JsonMappingException {

        Set<User> result = new HashSet<>();
        while (url != null && !url.isEmpty()) {
            logger.debug("load users from url :" + url);

            JSONObject object = JsonReader.readJsonFromUrl(url);
            EventUsersResult facbookUsers = this.objectMapper.readValue(object.toString(), EventUsersResult.class
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

        List<EventDto> result = findEvents(start.getTime(), end.getTime());
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

        List<EventDto> result = findEvents(start.getTime(), end.getTime());
        return result;
    }

    /**
     * Weekend start : Saturday in current week Weekend end : Sunday in next
     * week
     *
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

        List<EventDto> result = findEvents(start.getTime(), end.getTime());
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

        List<EventDto> result = findEvents(start.getTime(), end.getTime());
        return result;
    }

    public List<EventDto> getEvents(Date start, Date end) {
        if (logger.isDebugEnabled()) {
            logger.debug(" get events between :" + start.getTime() + "," + end.getTime());
        }

        List<EventDto> result = findEvents(start, end);
        return result;
    }

    private List<EventDto> findEvents(Date start, Date end) {
        List<Event> events;
        if (this.selectedCity == null || this.selectedCity.isEmpty()) {
            events = eventsDao.findEvents(start, end);
        } else {
            events = eventsDao.findEventsInCity(start, end, this.selectedCity);
        }

        List<EventDto> result = new ArrayList<>(events.size());
        events.forEach((event) -> {
            Set<Long> interestedUsers = new HashSet<>();
            event.getInterestedUsers().forEach((user) -> {
                interestedUsers.add(user.getId());
            });
            Set<Long> attendingUsers = new HashSet<>();
            event.getAttendingUsers().forEach((user) -> {
                attendingUsers.add(user.getId());
            });
            result.add(new EventDto(event.getId(), event.getTitle(), event.getStartDateTime(), event.getEndDateTime(), interestedUsers, attendingUsers, event.getUrl()));
        });

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

    public void setAttending(Long userId, Long eventId, Boolean attending) {
        Event event = eventsDao.find(eventId);
        User user = usersDao.find(userId);
        if (attending) {
            event.getAttendingUsers().add(user);
        } else {
            event.getAttendingUsers().remove(user);
        }
        eventsDao.merge(event);
    }

    public void setInterested(Long userId, Long eventId, Boolean attending) {
        Event event = eventsDao.find(eventId);
        User user = usersDao.find(userId);
        if (attending) {
            event.getInterestedUsers().add(user);
        } else {
            event.getInterestedUsers().remove(user);
        }
        eventsDao.merge(event);
    }
}
