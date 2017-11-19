package org.xonyne.events.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xonyne.events.config.AppContext;
import org.xonyne.events.dto.EventDto;
import org.xonyne.events.service.LoadEventsService;

@Path("/account")
@Controller
public class EventsEndPoint {
	
	private static final Logger logger = LoggerFactory.getLogger(EventsEndPoint.class);
	private SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
	
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
	@Path("/weekend")
	public List<EventDto> getWeekendEvents() {
		return AppContext.ctx.getBean(LoadEventsService.class).getWeekendEvents();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/nextWeekend")
	public List<EventDto> getNextWeekendEvents() {
		return AppContext.ctx.getBean(LoadEventsService.class).getNextWeekendEvents();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find")
	public List<EventDto> findEvents(@QueryParam("from") String from, @QueryParam("to") String to) {
		try{
			Date fromDate = dateFormate.parse(from);
			Date toDate = dateFormate.parse(to);
			return AppContext.ctx.getBean(LoadEventsService.class).getEvents(fromDate, toDate);
		}catch(Exception ex){
			logger.error("error in findEvents,"+ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
		
		
	}
	

}
