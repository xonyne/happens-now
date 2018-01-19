package org.xonyne.events.rest;

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
	   HashSet<Object> singletons = new HashSet<>();
	   
	   public RestApplication()
	   {
	      //singletons.add(new EventsEndPoint());
	   }

	   @Override
	   public Set<Class<?>> getClasses()
	   {
	      HashSet<Class<?>> set = new HashSet<>();
              //set.add(JacksonConfig.class);
	      return set;
	   }

	   @Override
	   public Set<Object> getSingletons()
	   {
	      return singletons;  
	   }

}
