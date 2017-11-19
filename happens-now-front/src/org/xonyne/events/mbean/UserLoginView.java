package org.xonyne.events.mbean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.context.RequestContext;
import org.springframework.web.client.RestTemplate;

@ManagedBean
public class UserLoginView {

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
   
    public void login(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        boolean loggedIn = false;
        
//        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(httpClient);

//        RestTemplate restTemplate = new RestTemplate();
//        User<Event> quote = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
//        log.info(quote.toString());
//        
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//        
//        if(username != null && username.equals("admin") && password != null && password.equals("admin")) {
//            loggedIn = true;
//            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", username);
//        } else {
//            loggedIn = false;
//            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
//        }
//         
//        FacesContext.getCurrentInstance().addMessage(null, message);
//        context.addCallbackParam("loggedIn", loggedIn);
    }   
}
