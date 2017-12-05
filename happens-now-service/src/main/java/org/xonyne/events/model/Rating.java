package org.xonyne.events.model;

import java.lang.Long;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name="rating")
@SequenceGenerator(name="rating_rating_id_seq", sequenceName="rating_rating_id_seq", initialValue = 1, allocationSize = 1)
public class Rating {

	@GeneratedValue(generator="rating_rating_id_seq")
	@Id
	@Column(name="rating_id")
	private Long id;
	
        private Integer rating;
	
	@OneToOne()
	@JoinColumn(name="event_id")
	private Event event;
	@OneToOne()
	@JoinColumn(name="user_id")
	private User user;

	public Rating(){

	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
        
        public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	
}