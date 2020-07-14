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

import com.google.sps.TimeRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
  public static final int WHOLE_DAY = 24 * 60;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // throw new UnsupportedOperationException("TODO: Implement this method.");

    ArrayList<TimeRange> meetings = new ArrayList<>();
    Collection<String> requestedAttendees = request.getAttendees();
    long durationOfRequest = request.getDuration();

    // Case 0: if no attendees in a meeting, return whole day
    if (requestedAttendees.size() == 0) {
      TimeRange emptyMeeting = TimeRange.fromStartDuration(0, WHOLE_DAY);
      meetings.add(emptyMeeting);
      return meetings;
    }

    // Case 1: if duration too long (longer than a day), return no options
    if (durationOfRequest > WHOLE_DAY) {
      return meetings;
    }

    /** Case 2: GENERAL CASE >> remove conflicting times(from @events) 
      * from possible meetings
      */
    meetings.add(TimeRange.fromStartDuration(0, WHOLE_DAY));

    for (Event event : events) {
      Set<String> overlappedEventAttendees = event.getAttendees();
      overlappedEventAttendees.retainAll(requestedAttendees);

      // If the event contains attendees that were requested
      if (overlappedEventAttendees.size() != 0) {
        TimeRange eventTimeRange = event.getWhen();
        ArrayList<TimeRange> proposedMeetings = new ArrayList<>();

        for (int i = 0; i < meetings.size(); i++) {
          TimeRange curMeetingTimeRange = meetings.get(i);

          if (eventTimeRange.overlaps(curMeetingTimeRange)) {
            // TODO
            /** Case 1:
              * Event overlaps prior to start of proposed meeting time
              */
            if (eventTimeRange.start() <= curMeetingTimeRange.start()) {
              /** If event ends before proposed meeting ends,
                * then can still make an altered proposed meeting 
                */
              if (eventTimeRange.end() < curMeetingTimeRange.end()) {
                  TimeRange proposedTimeRange = TimeRange.fromStartEnd(eventTimeRange.end(), curMeetingTimeRange.end(), true);

                  // Cannot have meeting if not enough time
                  if (proposedTimeRange.duration() < request.getDuration()) {
                    continue;
                  }

                  proposedMeetings.add(proposedTimeRange);
              }
            }

            /** Case 2:
              * Event overlaps after end of proposed meeting time
              */
            if (eventTimeRange.end() >= curMeetingTimeRange.end()) {
              /** If event starts after proposed meeting starts,
                * then can still make an altered proposed meeting
                */
              if (eventTimeRange.start() > curMeetingTimeRange.start()) {
                TimeRange proposedTimeRange = TimeRange.fromStartEnd(curMeetingTimeRange.start(), eventTimeRange.start(), true);

                // Cannot have meeting if not enough time
                if (proposedTimeRange.duration() < request.getDuration()) {
                  continue;
                }

                proposedMeetings.add(proposedTimeRange);
              }
            }
          } else {
            /** If event doesn't conflict with current proposed meet times,
              * then keep it
              */
            proposedMeetings.add(curMeetingTimeRange);
          }
        }
        // Update proposed meet times
        meetings = proposedMeetings;
        Comparator<TimeRange> orderMeetings = TimeRange.ORDER_BY_START;
        Collections.sort(meetings, orderMeetings);
      }
    }
    
    return meetings;
  }

  // Helper func: remove events that are nested and combines events that overlap
}
