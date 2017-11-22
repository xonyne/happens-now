package org.xonyne.events.jsonmapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookEventPlace {
    public Long id;
    public String name;
    public FacebookEventLocation location;

}
