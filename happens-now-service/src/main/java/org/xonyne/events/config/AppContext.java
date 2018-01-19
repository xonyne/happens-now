package org.xonyne.events.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public final class AppContext{

	@Autowired
	public ApplicationContext context;
	
	public static ApplicationContext ctx;
	
	@PostConstruct
	public void init(){
		ctx = context;
	}

}