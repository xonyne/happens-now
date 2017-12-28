package org.xonyne.events.mbean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.xonyne.events.config.AppContext;
import org.xonyne.events.dto.EventDto;

@ManagedBean(name = "findEventsView")
@ViewScoped
public class FindEventsView {

    private Map<String, String> categories = new HashMap<>();
    private String selectedCategory;
    
    private List<String> cities;
    private String selectedCity;

    
    private Date selectedDate;

    public List<EventDto> events;
    
    private boolean companion;

    public FindEventsView() {
        categories.put("Sports", "Sports");
        this.cities = loadCities();
        if (this.cities.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "Backend can not be reached!"));
        }
    }
    
    private List<String> loadCities() {
        return AppContext.getEventsService().cityNames();
    }
    
    public String getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
        //set the city in the backend
        AppContext.getEventsService().setCity(selectedCity);
    }

    public boolean isCompanion() {
        return companion;
    }

    public void setCompanion(boolean companion) {
        this.companion = companion;
    }

    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    public void todayEvents() {
        this.events = AppContext.getEventsService().todayEvents();
    }

    public void tonightEvents() {
        this.events = AppContext.getEventsService().tonightEvents();
    }

    public void weekendEvents() {
        this.events = AppContext.getEventsService().weekendEvents();
    }

    public void nextWeekendEvents() {
        this.events = AppContext.getEventsService().nextWeekendEvents();
    }

    public void getSelectedDateEvents() {
        if (selectedDate == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Error", "Select a date first"));
            return;
        }
        this.events = AppContext.getEventsService().getSelectedDateEvents(selectedDate);
    }

    public Map<String, String> getCategories() {
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

    public boolean getCompanion() {
        return companion;
    }

    public void setCompanion(Boolean yesNo) {
        this.companion = yesNo;
    }

}
