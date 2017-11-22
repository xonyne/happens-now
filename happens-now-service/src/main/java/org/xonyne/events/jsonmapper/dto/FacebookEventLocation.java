package org.xonyne.events.jsonmapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookEventLocation {
    public String city;
    public String country;
    public Double latitude;
    public Double longitude;
    public String street;
    public String zip;
}
