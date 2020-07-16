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
import java.util.Iterator;
import java.util.Set;

public final class FindMeetingQuery {
  public static final int NUM_MINUTES_IN_DAY = 24 * 60;

  /** Return a collection of TimeRanges where the requested attendees 
    * are available to meet
    */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> meetings = new ArrayList<>();
    Collection<String> requestedAttendees = request.getAttendees();
    Collection<String> requestedOptionalAttendees = 
      request.getOptionalAttendees();
    long durationOfRequest = request.getDuration();

    // Case 0: If duration too long (longer than a day), return no options
    if (durationOfRequest > NUM_MINUTES_IN_DAY) {
      return meetings;
    }
    
    // Case 1: Invalid input
    if (durationOfRequest < 0) {
      return Arrays.asList();
    }

    /** Case 2: GENERAL CASE >> remove conflicting times(from @events) 
      * from possible meetings
      */
    meetings.add(TimeRange.fromStartDuration(0, NUM_MINUTES_IN_DAY));
    ArrayList<Event> optionalOverlappedEvents = new ArrayList<>();
    for (Event event : events) {
      Set<String> tempOverlappedEventAttendees = event.getAttendees();

      Set<String> overlappedEventAttendees = 
        new HashSet<String>(tempOverlappedEventAttendees);
      Set<String> optionalOverlappedAttendees = 
        new HashSet<String>(tempOverlappedEventAttendees);

      // Filter events for overlaps with request
      overlappedEventAttendees.retainAll(requestedAttendees);
      optionalOverlappedAttendees.retainAll(requestedOptionalAttendees);

      if (!overlappedEventAttendees.isEmpty()) {
        // Event contains attendees that were requested
        TimeRange eventTimeRange = event.getWhen();

        // Resolve timing conflicts
        meetings = makeNewProposedMeetings(meetings, eventTimeRange, request);

        Comparator<TimeRange> orderMeetings = TimeRange.ORDER_BY_START;
        Collections.sort(meetings, orderMeetings);
      } else if (!optionalOverlappedAttendees.isEmpty()) {
        // Event contains optional attendees that were requested
        optionalOverlappedEvents.add(event);
      }
    }

    // Handle optional events restrictions separately from mandatory ones
    ArrayList<TimeRange> meetingsWithOptional = 
      (ArrayList<TimeRange>) meetings.clone();

    for (Event event : optionalOverlappedEvents) {
      TimeRange eventTimeRange = event.getWhen();

      // Resolve timing conflicts
      meetingsWithOptional = 
        makeNewProposedMeetings(meetingsWithOptional, eventTimeRange, request);

      Comparator<TimeRange> orderMeetings = TimeRange.ORDER_BY_START;
      Collections.sort(meetingsWithOptional, orderMeetings);
    }

    // Return meeting set based on attendees and availabilities
    if (requestedAttendees.isEmpty() && !requestedOptionalAttendees.isEmpty()) {
      return meetingsWithOptional;
    }
    return !meetingsWithOptional.isEmpty() ? meetingsWithOptional : meetings;
  }

  /** Return new proposed meets by adjusting input proposed
    * meets to exclude times that conflict with @events
    */ 
  public ArrayList<TimeRange> makeNewProposedMeetings(
    ArrayList<TimeRange> meetings,
    TimeRange eventTimeRange,
    MeetingRequest request) {
    ArrayList<TimeRange> tempListMeetings = new ArrayList<TimeRange>();

    for (int i = 0; i < meetings.size(); i++) {
      TimeRange curMeetingTimeRange = meetings.get(i);

      if (eventTimeRange.overlaps(curMeetingTimeRange)) {
        if (eventTimeRange.start() < curMeetingTimeRange.start()) {
          if (eventTimeRange.end() < curMeetingTimeRange.end()) {
            /** Case 1:
              *     |--Existing Event--|
              *                |--Conflict--|
              */
            TimeRange tempProposedMeeting = 
              TimeRange.fromStartEnd(
                eventTimeRange.end(), 
                curMeetingTimeRange.end(), 
                false);

            tempListMeetings.add(tempProposedMeeting);
          } else {
            /** Case 2:
              *     |--Existing Event--|
              *         |--Conflict--|
              */
            continue;
          }
        } else {
          if (eventTimeRange.end() < curMeetingTimeRange.end()) {
            /** Case 3:
              *        |--Existing Event--|
              *     |--------Conflict--------|
              */
            TimeRange tempProposedMeetingA = 
              TimeRange.fromStartEnd(
                curMeetingTimeRange.start(), 
                eventTimeRange.start(),
                false);
            TimeRange tempProposedMeetingB = 
              TimeRange.fromStartEnd(
                eventTimeRange.end(),
                curMeetingTimeRange.end(),
                false);
            
            tempListMeetings.add(tempProposedMeetingA);
            tempListMeetings.add(tempProposedMeetingB);
          } else {
            /** Case 4:
              *            |--Existing Event--|
              *     |--Conflict--|
              */
            TimeRange tempProposedMeeting = 
              TimeRange.fromStartEnd(
                curMeetingTimeRange.start(),
                eventTimeRange.start(),
                false);
            
            tempListMeetings.add(tempProposedMeeting);
          }
        }
      } else {
        /** If event doesn't conflict with current proposed meet times,
          * then keep current set of proposed meets
          */
        tempListMeetings.add(curMeetingTimeRange);
      }
    }

    // Remove proposed meetings that are not long enough to fit request
    Iterator<TimeRange> iter = tempListMeetings.iterator();
    while (iter.hasNext()) {
      TimeRange tempMeeting = iter.next();

      if (tempMeeting.duration() < request.getDuration()) {
        iter.remove();
      }
    }

    return tempListMeetings;
  }
}