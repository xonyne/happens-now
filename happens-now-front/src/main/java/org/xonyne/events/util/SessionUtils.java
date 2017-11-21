package org.xonyne.events.util;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.xonyne.events.dto.UserDto;

public class SessionUtils {

	public static HttpSession getSession() {
		return (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
	}

	public static UserDto getUser() {
		if (FacesContext.getCurrentInstance() == null){
			return null;
		}
		
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		return (UserDto) session.getAttribute("user");
	}

	public static void setUser(boolean createSession, UserDto user) {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(createSession);
		session.setAttribute("user", user);
	}

	public static boolean hasUser(HttpSession session) {
		return session.getAttribute("user") != null; 
	}

}