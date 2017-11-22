package org.xonyne.events.jsonmapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookEventVenue {
    public String id;
    public String name;
    public String about;
    public String[] emails;
    public String coverPicture;
    public String profilePicture;
    public String category;
    public String[] categoryList;
    public FacebookEventLocation location;
}
