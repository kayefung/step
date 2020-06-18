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
import java.util.Comparator;
import com.google.sps.Event;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {

  /**
   * Returns a Collection of TimeRange objects that represent a time range where a requested
   * meeting can be held. Start and end markers for an available time range are moved to times
   * where a conflicting event is not taking place. Only ranges that are long enough to host the
   * requested meeting are added to the Collection to be returned. 
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Do not provide any time options if the meeting requested is longer than a whole day. 
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    ArrayList<Event> sortedEvents = new ArrayList<>(events);
    Collections.sort(sortedEvents, Event.ORDER_BY_START);

    Collection<TimeRange> times = new ArrayList<>();

    // Start and end markers of a time range that is available for a meeting. 
    int start = TimeRange.START_OF_DAY;
    int end = TimeRange.START_OF_DAY;

    for (Event event : sortedEvents) {

      // Do not use event to determine time range options if none of the event attendees will be
      // attending the requested meeting. 
      if (Collections.disjoint(event.getAttendees(), request.getAttendees())) {
        continue;
      }

      TimeRange eventTime = event.getWhen();

      // "Skips" over events that start at the same time and places start marker at end of event.
      if (start == eventTime.start()) {
        start = eventTime.end();
        continue;
      }

      end = eventTime.start();
      
      TimeRange time = TimeRange.fromStartEnd(start, end, false);

      // Only add time range if it's long enough to host the meeting.
      if (time.duration() >= request.getDuration()) {
        times.add(time);
      }

      // Moves marker to set the start of the next available time range. 
      if (start < eventTime.end()) {
        start = eventTime.end();
      }
    }

    // Add the last time range between the end of the last event and the end of the day.
    TimeRange time = TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true);
    if (time.duration() >= request.getDuration()) {
      times.add(time);
    }

    return times;
  }
}