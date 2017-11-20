package org.xonyne.events.dto;

import java.io.Serializable;
import java.util.Date;

public class EventDto implements Serializable{
	private static final long serialVersionUID = -2448882588957057786L;
	
	private Long id;
	private String title;
	private Date start;
	private Date end;
	
	
	public Long getId() {
		return id;
	}
	public EventDto(Long id, String title, Date start, Date end) {
		super();
		this.id = id;
		this.title = title;
		this.start = start;
		this.end = end;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	
	
}
