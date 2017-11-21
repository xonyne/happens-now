package org.xonyne.events.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xonyne.events.dto.UserDto;
import org.xonyne.events.service.EventsService;

@WebFilter(filterName = "PermissionFilter", dispatcherTypes = { DispatcherType.ERROR, DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST })
public class PermissionFilter implements Filter {

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(PermissionFilter.class);

	@Autowired
	EventsService eventsService;
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpSession session = httpServletRequest.getSession(false);
		
		String url = httpServletRequest.getRequestURL().toString();
		logger.debug("doFilter for url:" + url);
		
		if ((session != null && SessionUtils.hasUser(session)) || isFreeResource(url)) {
			filterChain.doFilter(request, response);
		}else{
			((HttpServletResponse) response)
			.sendRedirect(((HttpServletRequest) request).getContextPath() + "/login.xhtml");
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	
	public boolean isFreeResource(String resource){
		if (resource.indexOf("/login") != -1){
			return true;
		}
		
		if (resource.indexOf("javax.faces.resource") != -1){
			return true;
		}
		
		return false;
	}
}
