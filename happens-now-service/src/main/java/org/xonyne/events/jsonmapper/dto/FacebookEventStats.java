package org.xonyne.events.jsonmapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookEventStats {
    public String attending;
    public String declined;
    public String maybe;
    public String noreply;
}
