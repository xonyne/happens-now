package org.xonyne.events.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xonyne.events.config.AppContext;
import org.xonyne.events.dto.EventDto;
import org.xonyne.events.dto.UserDto;
import org.xonyne.events.service.LoadEventsService;

@Path("/events")
@Controller
public class EventsEndPoint {
	
	private static final Logger logger = LoggerFactory.getLogger(EventsEndPoint.class);
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private LoadEventsService eventsService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/today")
	public List<EventDto> getTodayEvents() {
		logger.debug("start getTodayEvents");
		return AppContext.ctx.getBean(LoadEventsService.class).getTodayEvents();
	}
        
        @GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/cityNames")
	public List<String> getCityNames() {
		logger.debug("start getCityNames");
		return AppContext.ctx.getBean(LoadEventsService.class).getCityNames();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tonight")
	public List<EventDto> getTonightEvents() {
		logger.debug("start getTonightEvents");
		return AppContext.ctx.getBean(LoadEventsService.class).getTonightEvents();
	}

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/weekend")
	public List<EventDto> getWeekendEvents() {
		logger.debug("start getWeekendEvents");
		return AppContext.ctx.getBean(LoadEventsService.class).getWeekendEvents();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/nextWeekend")
	public List<EventDto> getNextWeekendEvents() {
		logger.debug("start getNextWeekendEvents");
		return AppContext.ctx.getBean(LoadEventsService.class).getNextWeekendEvents();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public List<EventDto> findEvents(@QueryParam("from") String from, @QueryParam("to") String to) {
		try{
			logger.debug("find events [from-to]:"+from + ", " + to);
			SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date fromDate = dateFormate.parse(from);
			Date toDate = dateFormate.parse(to);
			return AppContext.ctx.getBean(LoadEventsService.class).getEvents(fromDate, toDate);
		}catch(Exception ex){
			logger.error("error in findEvents,"+ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
        
        @GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/setCity")
	public void setCity(@QueryParam("city") String city) {
		try{
			logger.debug("set city");
                        AppContext.ctx.getBean(LoadEventsService.class).setSelectedCity(city);
		}catch(Exception ex){
			logger.error("error in setting the city,"+ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	@GET
	@Path("/authenticate")
	@Produces(MediaType.APPLICATION_JSON)
	public UserDto authenticate(@QueryParam("username") String userName, @QueryParam("password") String password) {
		logger.debug("authenticate for user :" + userName);
		return AppContext.ctx.getBean(LoadEventsService.class).findUser(userName, password);
	}
        
        //TODO
        /*@GET
	@Path("/setAttending")
	@Produces(MediaType.APPLICATION_JSON)
	public UserDto setAttending(@QueryParam("username") String userName, @QueryParam("password") String password) {
		logger.debug("authenticate for user :" + userName);
		return AppContext.ctx.getBean(LoadEventsService.class).findUser(userName, password);
	}*/
}
