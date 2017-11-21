package org.xonyne.events.mbean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;
import org.xonyne.events.config.AppContext;
import org.xonyne.events.dto.EventDto;
import org.xonyne.events.service.EventsService;

@ManagedBean(name="findEventsView")
@ViewScoped
public class FindEventsView {

	private HashMap<String, String> categories = new HashMap<String, String>();
	
	private String selectedCategory;
	
	private Date selectedDate;
	
	public List<EventDto> events;
	
	public FindEventsView(){
		categories.put("Sports", "Sports");
	}
	
	public void todayEvents(){
		this.events = AppContext.getEvetnsService().todayEvents();
	}
	
	public void tonightEvents(){
		this.events = AppContext.getEvetnsService().tonightEvents();
	}

	public void weekendEvents(){
		this.events = AppContext.getEvetnsService().weekendEvents();
	}
	
	public void nextWeekendEvents(){
		this.events = AppContext.getEvetnsService().nextWeekendEvents();
	}

	public void getSelectedDateEvents(){
		this.events = AppContext.getEvetnsService().getSelectedDateEvents(selectedDate);
	}

	public HashMap<String, String> getCategories() {
		return categories;
	}

	public void setCategories(HashMap<String, String> categories) {
		this.categories = categories;
	}

	public String getSelectedCategory() {
		return selectedCategory;
	}

	public void setSelectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	public List<EventDto> getEvents() {
		return events;
	}

	public void setEvents(List<EventDto> events) {
		this.events = events;
	}
	
	
}
