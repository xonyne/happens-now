package org.xonyne.events.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
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
	
	@Value("${loadEventsService.backendSeviceUrl}")
	private String backendSeviceUrl;

	public UserDto login(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);
        
        UserDto user = restTemplate.getForObject(backendSeviceUrl+"/events/authenticate?username={username}&password={password}", UserDto.class, parameters);
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(true);
        session.setAttribute("user", user);
        
        return user;
	}
	
	public List<EventDto> todayEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = restTemplate.getForObject(backendSeviceUrl+"/events/today", List.class);
        
        return events;
	}

	public List<EventDto> tonightEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = restTemplate.getForObject(backendSeviceUrl+"/events/tonight", List.class);
        
        return events;
	}

	public List<EventDto> weekendEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = restTemplate.getForObject(backendSeviceUrl+"/events/weekend", List.class);
        
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
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("from", dateFormate.format(startTime.getTime()));
        parameters.put("to", dateFormate.format(endTime.getTime()));
        List<EventDto> events = restTemplate.getForObject(backendSeviceUrl+"/events/find?from={from}&to={to}", List.class, parameters);
        
        return events;
	}

	public List<EventDto> nextWeekendEvents() {
        RestTemplate restTemplate = new RestTemplate();
        List<EventDto> events = restTemplate.getForObject(backendSeviceUrl+"/events/nextWeekend", List.class);
        
        return events;
	}
}
