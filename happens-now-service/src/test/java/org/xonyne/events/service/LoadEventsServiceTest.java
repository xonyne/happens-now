/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xonyne.events.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.xonyne.events.dao.EventsDao;
import org.xonyne.events.dao.UsersDao;
import org.xonyne.events.jsonmapper.JsonReader;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.Place;
import org.xonyne.events.model.User;

/**
 *
 * @author kevin
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JsonReader.class)
@PowerMockIgnore("javax.management.*")
public class LoadEventsServiceTest {

    @Mock
    private EventsDao eventsDaoMock;
    @Mock
    private UsersDao usersDaoMock;

    @InjectMocks
    private LoadEventsService loadEventsService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(loadEventsService, "facebookEventsByLocationUrl", "");
        ReflectionTestUtils.setField(loadEventsService, "facebookGraphApiUrl", "");
        ReflectionTestUtils.setField(loadEventsService, "latitude", 0.0d);
        ReflectionTestUtils.setField(loadEventsService, "longitude", 0.0d);
        ReflectionTestUtils.setField(loadEventsService, "distance", 0);
        ReflectionTestUtils.setField(loadEventsService, "attendingRatingModifier", 1);
        ReflectionTestUtils.setField(loadEventsService, "interestedRatingModifier", 3);
        ReflectionTestUtils.setField(loadEventsService, "callLoopsPerServiceInvocation", 1);
        
        PowerMockito.mockStatic(JsonReader.class);
        PowerMockito.when(JsonReader.readJsonFromUrl(eq(loadEventsService.getEventsUrl()))).thenReturn(new JSONObject(DataContainer.JSON_DATA_EVENTS));
        PowerMockito.when(JsonReader.readJsonFromUrl(contains("/attending"))).thenReturn(new JSONObject(DataContainer.JSON_DATA_USERS));
        PowerMockito.when(JsonReader.readJsonFromUrl(contains("/interested"))).thenReturn(new JSONObject(DataContainer.JSON_DATA_USERS));
        PowerMockito.when(usersDaoMock.findOrPersist(any(User.class))).thenReturn(new User());
        PowerMockito.when(eventsDaoMock.findOrPersist(any(Event.class))).thenReturn(new Event());
        PowerMockito.when(eventsDaoMock.findOrPersist(any(Place.class))).thenReturn(new Place());
    }

    @Test
    public void loadEventsMakeSureEventsAreStoredInDB() throws IOException {
        loadEventsService.loadEvents();
        verify(eventsDaoMock, times(6)).findOrPersist(any(Event.class));
        verify(eventsDaoMock, times(6)).findOrPersist(any(Place.class));
        // 6 events * 7 users * 2 (same users attending and interested)
        verify(usersDaoMock, times(84)).findOrPersist(any(User.class));
    }

    @Test
    public void loadEventsMakeSureEventPlacesAreStoredInDB() {
    }

    @Test
    public void loadEventsMakeSureUserIsStoredInDB() {
    }

    @Test
    public void loadEventsMakeSureNoDuplicatesInDB() {
    }

    @Test
    public void loadEventsMakeSureStoreAttendingInDB() {
    }

    @Test
    public void loadEventsMakeSureStoreInterestedInDB() {
    }

    @Test
    public void loadEventsMakeSureHighestRatingIsStoredInDB() {
    }

}
