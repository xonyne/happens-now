package org.xonyne.events.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class EventDto implements Serializable {

    private static final long serialVersionUID = -2448882588957057786L;

    private Long id;
    private String title;
    private Date start;
    private Date end;
    public Set<Long> interestedUsers;
    public Set<Long> attendingUsers;
    private Boolean userIsInterested;
    private Boolean userIsAttending;

    public Long getId() {
        return id;
    }
    
    public EventDto(){}

    public EventDto(Long id, String title, Date start, Date end,Set<Long> interestedUsers,Set<Long> attendingUsers) {
        super();
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.interestedUsers = interestedUsers;
        this.attendingUsers = attendingUsers;
        this.userIsInterested = false;
        this.userIsAttending = false;
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

    public Boolean getUserIsInterested() {
        return userIsInterested;
    }

    public void setUserIsInterested(Boolean userIsInterested) {
        this.userIsInterested = userIsInterested;
    }

    public Boolean getUserIsAttending() {
        return userIsAttending;
    }

    public void setUserIsAttending(Boolean userIsAttending) {
        this.userIsAttending = userIsAttending;
    }

    public Set<Long> getInterestedUsers() {
        return interestedUsers;
    }

    public void setInterestedUsers(Set<Long> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }

    public Set<Long> getAttendingUsers() {
        return attendingUsers;
    }

    public void setAttendingUsers(Set<Long> attendingUsers) {
        this.attendingUsers = attendingUsers;
    }

}
