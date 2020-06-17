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
import java.util.ListIterator;
import java.util.Collections;
import com.google.sps.Event;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Do not provide any time options if the meeting requested is longer than a whole day. 
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    ArrayList<TimeRange> times = new ArrayList<>(Arrays.asList(TimeRange.WHOLE_DAY));

    for (Event event : events) {

      // Do not use event to determine time range options if none of the event attendees will be
      // attending the requested meeting. 
      if (Collections.disjoint(event.getAttendees(), request.getAttendees())) {
        continue;
      }

      ListIterator<TimeRange> timesIterator = times.listIterator();

      while (timesIterator.hasNext()) {
        TimeRange time = timesIterator.next();

        if (time.contains(event.getWhen()) || time.overlaps(event.getWhen())) {
          // Remove time because event runs through it.
          timesIterator.remove();
          
          // Split time into two time ranges, one before the event and after the event.
          TimeRange beforeEvent = 
              TimeRange.fromStartEnd(time.start(), event.getWhen().start(), false);
          TimeRange afterEvent =
              TimeRange.fromStartEnd(event.getWhen().end(), time.end(), false);
          
          // Only add new ranges if the meeting requested can occur in that time range.
          if (beforeEvent.duration() >= request.getDuration()) {
            timesIterator.add(beforeEvent);
          }
          if (afterEvent.duration() >= request.getDuration()) {
            timesIterator.add(afterEvent);
          }
        }
      }
    }

    return times;
  }
}