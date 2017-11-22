package org.xonyne.events.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.xonyne.events.rest.EventsEndPoint;

/**
 * This class is required by RestEasy to define the REST services. 
 * 
 * @author Ridwan Nizam
 *
 */
public class RestApplication extends Application
{
	   HashSet<Object> singletons = new HashSet<Object>();
	   
	   public RestApplication()
	   {
	      //singletons.add(new EventsEndPoint());
	   }

	   @Override
	   public Set<Class<?>> getClasses()
	   {
	      HashSet<Class<?>> set = new HashSet<Class<?>>();
//	      set.add(JacksonConfig.class);
	      return set;
	   }

	   @Override
	   public Set<Object> getSingletons()
	   {
	      return singletons;  
	   }

}
