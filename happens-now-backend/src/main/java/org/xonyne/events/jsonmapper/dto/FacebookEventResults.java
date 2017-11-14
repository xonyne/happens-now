package org.xonyne.events.jsonmapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author kevin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookEventResults {
    public FacebookEvent[] events;
}
