/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xonyne.events.service;

/**
 *
 * @author kevin
 */
public class LoadEventsServiceStatistics {

    private int totalEvents;
    private int newEvents;
    private int newPlaces;
    private int newUsers;
    private int graphApiCalls;
    
    public LoadEventsServiceStatistics(){
        this.totalEvents = 0;
        this.newEvents = 0;
        this.newPlaces = 0;
        this.newUsers = 0;
        this.graphApiCalls = 0;
    }

    public LoadEventsServiceStatistics(int totalEvents, int newEvents, int newPlaces, int newUsers, int graphApiCalls) {
        this.totalEvents = totalEvents;
        this.newEvents = newEvents;
        this.newPlaces = newPlaces;
        this.newUsers = newUsers;
        this.graphApiCalls = graphApiCalls;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void increaseTotalEvents(int count) {
        this.totalEvents+=count;
    }

    public int getNewEvents() {
        return newEvents;
    }

    public void increaseNewEvents(int count) {
        this.newEvents+=count;
    }

    public int getNewPlaces() {
        return newPlaces;
    }

    public void increaseNewPlaces(int count) {
        this.newPlaces+=count;
    }

    public int getNewUsers() {
        return newUsers;
    }

    public void increaseNewUsers(int count) {
        this.newUsers+=count;
    }

    public int getGraphApiCalls() {
        return graphApiCalls;
    }

    public void increaseGraphApiCalls(int count) {
        this.graphApiCalls+=count;
    }
}
