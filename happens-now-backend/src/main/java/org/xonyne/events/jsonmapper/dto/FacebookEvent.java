package org.xonyne.events.jsonmapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookEvent {
    public Long id;
    public String name;
    public String type;
    public String coverPicture;
    public String profilePicture;
    public String description;
    public Date start_time;
    public Date end_time;
    public String timeFromNow;
    public boolean isCancelled;
    public boolean isDraft;
    public String category;
    public FacebookEventPlace place;
    public FacebookEventStats stats;
    public FacebookEventVenue venue;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacebookEvent that = (FacebookEvent) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

	@Override
	public String toString() {
		return "FacebookEvent [id=" + id + ", name=" + name + ", type=" + type
				+ ", coverPicture=" + coverPicture + ", profilePicture="
				+ profilePicture + ", description=" + description
				+ ", start_time=" + start_time + ", end_time=" + end_time
				+ ", timeFromNow=" + timeFromNow + ", isCancelled="
				+ isCancelled + ", isDraft=" + isDraft + ", category="
				+ category + ", place=" + place + ", stats=" + stats
				+ ", venue=" + venue + "]";
	}
}
