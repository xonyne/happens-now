package org.xonyne.events.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name="event")
//@SequenceGenerator(name="event_eventId_seq", sequenceName="event_eventId_seq", initialValue = 1, allocationSize = 1)
public class Event {

//	@GeneratedValue(generator="event_eventId_seq")
	@Id
	@Column(name="event_id")
	private Long eventId;
	private String title;
	private String description;
	private Date startDateTime;
	private Date endDateTime;
	private String url;
	@ManyToMany(
            targetEntity=Tag.class
          //  cascade={CascadeType.PERSIST, CascadeType.MERGE}
        )
        @JoinTable(
            name="EventTag",
            joinColumns=@JoinColumn(name="event_id"),
            inverseJoinColumns=@JoinColumn(name="tag_id")
        )
	public Set<Tag> tags;
    
	@ManyToMany(
            targetEntity=User.class
            //cascade={CascadeType.PERSIST, CascadeType.MERGE}
        )
        @JoinTable(
            name="interested",
            joinColumns=@JoinColumn(name="event_id"),
            inverseJoinColumns=@JoinColumn(name="user_id")
        )
	public Set<User> interestedUsers;

	@ManyToMany(
            targetEntity=User.class,
            cascade={CascadeType.PERSIST, CascadeType.MERGE}
        )
        @JoinTable(
            name="attending",
            joinColumns=@JoinColumn(name="event_id"),
            inverseJoinColumns=@JoinColumn(name="user_id")
        )
	public Set<User> attendingUsers;
	
	@OneToOne()
	@JoinColumn(name="place_id")
	public Place place;

	public Event(Long eventId, String title, String description,
			Date startDateTime, Date endDateTime, String url, Set<Tag> tags,
			Set<User> interestedUsers, Set<User> attendingUsers, Place place) {
		super();
		this.eventId = eventId;
		this.title = title;
		this.description = description;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.url = url;
		this.tags = tags;
		this.interestedUsers = interestedUsers;
		this.attendingUsers = attendingUsers;
		this.place = place;
	}
	
	public Event(){

	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Date getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<User> getInterestedUsers() {
		return interestedUsers;
	}

	public void setInterestedUsers(Set<User> interestedUsers) {
		this.interestedUsers = interestedUsers;
	}

	public Set<User> getAttendingUsers() {
		return attendingUsers;
	}

	public void setAttendingUsers(Set<User> attendingUsers) {
		this.attendingUsers = attendingUsers;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

}