// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import com.google.sps.Event;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    
    // Do not provide any time options if the meeting requested is longer than a whole day. 
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    ArrayList<Event> sortedEvents = new ArrayList<>(events);
    Collections.sort(sortedEvents, Event.ORDER_BY_START);

    Collection<Event> mandatoryEvents =
        findEventsByAttendees(sortedEvents, request.getAttendees());
    sortedEvents.removeAll(mandatoryEvents);
    Collection<Event> optionalEvents =
        findEventsByAttendees(sortedEvents, request.getOptionalAttendees());
    
    // If there's no mandatory attendees, only consider optional attendees.
    if (mandatoryEvents.isEmpty()) {
      return findAvailableTimes(optionalEvents, duration);
    }

    Collection<TimeRange> mandatoryTimes = findAvailableTimes(mandatoryEvents, duration);
    Collection<TimeRange> optionalTimes = findAvailableTimes(optionalEvents, duration);

    Collection<TimeRange> overlappingTimes =
        findOverlappingTimes(mandatoryTimes, optionalTimes, duration);

    ArrayList<TimeRange> toRemove = new ArrayList<>();
    for (TimeRange time : overlappingTimes) {
      if (time.duration() < duration) {
        toRemove.add(time);
      }
    }
    overlappingTimes.removeAll(toRemove);

    // Return time slots where both mandatory and optional attendees are available, if any.
    if (!overlappingTimes.isEmpty()) {
      return overlappingTimes;
    }

    return mandatoryTimes;
  }

  /**
   * Return a list of events where the event's attendees share at least one person with the
   * attendee list.
   */
  private ArrayList<Event> findEventsByAttendees(Collection<Event> events,
      Collection<String> attendees) {
    ArrayList<Event> attendeeEvents = new ArrayList<>();

    for (Event event : events) {
      if (!Collections.disjoint(event.getAttendees(), attendees)) {
        attendeeEvents.add(event);
      }
    }

    return attendeeEvents;
  }

  /**
   * Returns a Collection of TimeRange objects that represent a time range where a requested
   * meeting can be held. Start and end markers for an available time range are moved to times
   * where a conflicting event is not taking place. Only ranges that are long enough to host the
   * requested meeting are added to the Collection to be returned. 
   */
  private Collection<TimeRange> findAvailableTimes(Collection<Event> events, long duration) {
    Collection<TimeRange> times = new ArrayList<>();

    // Start and end markers of a time range that is available for a meeting. 
    int start = TimeRange.START_OF_DAY;
    int end = TimeRange.START_OF_DAY;

    for (Event event : events) {

      TimeRange eventTime = event.getWhen();

      // "Skips" over events that start at the same time and places start marker at end of event.
      if (start == eventTime.start()) {
        start = eventTime.end();
        continue;
      }

      end = eventTime.start();
      
      TimeRange time = TimeRange.fromStartEnd(start, end, false);

      // Only add time range if it's long enough to host the meeting.
      if (time.duration() >= duration) {
        times.add(time);
      }

      // Moves marker to set the start of the next available time range. 
      if (start < eventTime.end()) {
        start = eventTime.end();
      }
    }

    // Add the last time range between the end of the last event and the end of the day.
    TimeRange time = TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true);
    if (time.duration() >= duration) {
      times.add(time);
    }

    return times;
  }

  /**
   * Returns a Collection of TimeRange objects that represent the overlapping times that mandatory
   * and optional attendees can attend a requested meeting. If there are no overlapping times, then
   * only mandatory attendee times are returned. 
   */
  public static Collection<TimeRange> findOverlappingTimes(Collection<TimeRange> mandatoryTimes,
      Collection<TimeRange> optionalTimes, long duration) {
    Collection<TimeRange> times = new ArrayList<>();
    
    // Find common available times between mandatory and optional attendees. 
    for (TimeRange mandatoryTime : mandatoryTimes) {
      for (TimeRange optionalTime : optionalTimes) {
        
        if (mandatoryTime.contains(optionalTime)) {
          times.add(optionalTime);
        } else if (optionalTime.contains(mandatoryTime)) {
          times.add(mandatoryTime);
        } else if (mandatoryTime.overlaps(optionalTime)) {
          int start = Math.max(mandatoryTime.start(), optionalTime.start());
          int end = Math.min(mandatoryTime.end(), optionalTime.end());

          times.add(TimeRange.fromStartEnd(start, end, false));
        }
      }
    }

    return times;
  }
}