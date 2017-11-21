package org.xonyne.events.mbean;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;
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
        
//        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(httpClient);

        String target = "login";
        UserDto user = AppContext.getEvetnsService().login(username, password);
		
        if(user != null ) {
        	SessionUtils.setUser(true, user);
        	target = "home";
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
         
        return target;
    }   
}
