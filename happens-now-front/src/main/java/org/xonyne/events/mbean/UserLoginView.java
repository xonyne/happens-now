package org.xonyne.events.mbean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.xonyne.events.config.AppContext;
import org.xonyne.events.dto.UserDto;
import org.xonyne.events.service.EventsService;
import org.xonyne.events.util.SessionUtils;

@ManagedBean
public class UserLoginView {

    @Autowired
	private EventsService eventsService;
    
	private String username;
    
    private String password;
 
    public String getUsername() {
        return username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
   
    public String login() {
        FacesMessage message = null;
        String target = "login";
        
        UserDto user = AppContext.getEventsService().login(username, password);
		
        if(user != null ) {
        	SessionUtils.setUser(true, user);
        	target = "home";
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
         
        return target;
    }
    
    public String logout(){
    	FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    	return "login";
    }
}
