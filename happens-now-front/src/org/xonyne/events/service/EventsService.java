package org.xonyne.events.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xonyne.events.dto.EventDto;
import org.xonyne.events.dto.UserDto;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EventsService {

    @Value("${loadEventsService.backendServiceUrl}")
    private String backendServiceUrl;

    public UserDto login(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);

        UserDto user = restTemplate.getForObject(backendServiceUrl + "/events/authenticate?username={username}&password={password}", UserDto.class, parameters);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute("user", user);

        return user;
    }

    public List<EventDto> todayEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = convertEvents(restTemplate.getForObject(backendServiceUrl + "/events/today", List.class));
        determineCurrentUserRating(events);
        return events;
    }

    public List<EventDto> tonightEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = convertEvents(restTemplate.getForObject(backendServiceUrl + "/events/tonight", List.class));
        determineCurrentUserRating(events);
        return events;
    }

    public List<EventDto> weekendEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = convertEvents(restTemplate.getForObject(backendServiceUrl + "/events/weekend", List.class));
        determineCurrentUserRating(events);
        return events;
    }

    public List<EventDto> nextWeekendEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = convertEvents(restTemplate.getForObject(backendServiceUrl + "/events/nextWeekend", List.class));
        determineCurrentUserRating(events);
        return events;
    }

    public List<EventDto> getSelectedDateEvents(Date selectedDate) {
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        RestTemplate restTemplate = new RestTemplate();
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(selectedDate);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(selectedDate);
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("from", dateFormate.format(startTime.getTime()));
        parameters.put("to", dateFormate.format(endTime.getTime()));
        List<EventDto> events = convertEvents(restTemplate.getForObject(backendServiceUrl + "/events/find?from={from}&to={to}", List.class, parameters));

        return events;
    }

    public List<String> cityNames() {
        RestTemplate restTemplate = new RestTemplate();
        List<String> cities = restTemplate.getForObject(backendServiceUrl + "/events/cityNames", List.class);

        return cities;
    }

    public void setCity(String city) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("city", city);
        restTemplate.getForObject(backendServiceUrl + "/events/setCity?city={city}", List.class, parameters);
    }

    private void determineCurrentUserRating(List<EventDto> events) {
        // get the user
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        UserDto loggedInUser = (UserDto) session.getAttribute("user");
        
        events.stream().map((event) -> {
            event.getAttendingUsers().forEach((dbUserId) -> {
                if (dbUserId.equals(loggedInUser.getId())) {
                    event.setUserIsAttending(Boolean.TRUE);
                }
            });
            return event;
        }).forEachOrdered((event) -> {
            event.getInterestedUsers().forEach((dbUserId) -> {
                if (dbUserId.equals(loggedInUser.getId())) {
                    event.setUserIsInterested(Boolean.TRUE);
                }
            });
        });
    }

    private List<EventDto> convertEvents(List<EventDto> events) {
        List<EventDto> convertedEvents = new ArrayList<>();
        for (int i = 0; i<events.size(); i++) {
            ObjectMapper mapper = new ObjectMapper(); 
            convertedEvents.add(mapper.convertValue(events.get(i), EventDto.class));
        }
        return convertedEvents;
    }
}
