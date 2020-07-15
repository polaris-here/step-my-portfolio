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
  public static final int WHOLE_DAY = 24 * 60;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // throw new UnsupportedOperationException("TODO: Implement this method.");
    System.out.println("query has been called");


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
      Set<String> tempOverlappedEventAttendees = event.getAttendees();
      Set<String> overlappedEventAttendees = new HashSet<String>();

      // Copy attendees into a modifiable collection
      for (String attendee : tempOverlappedEventAttendees) {
        overlappedEventAttendees.add(attendee);
      }

      overlappedEventAttendees.retainAll(requestedAttendees);
      System.out.println("event: " + event.getTitle() + ", overlapped attendees: " + overlappedEventAttendees);

      // If the event contains attendees that were requested
      if (overlappedEventAttendees.size() != 0) {
        TimeRange eventTimeRange = event.getWhen();
        System.out.println("Overlap in proposed meets found >> Event of interest is: " + eventTimeRange.start() + " to " + eventTimeRange.end());
        System.out.println("Current proposed meetings: " + meetings);
        ArrayList<TimeRange> proposedMeetings = new ArrayList<>();

        // Make new proposed meets to resolve timing conflicts
        System.out.println("Calling method: makeNewProposedMeetings...");
        meetings = makeNewProposedMeetings(meetings, eventTimeRange, request);

        System.out.println("meetings has been updated to: " + meetings);
        Comparator<TimeRange> orderMeetings = TimeRange.ORDER_BY_START;
        Collections.sort(meetings, orderMeetings);
        System.out.println("meetings have been sorted by start time: " + meetings);
      }
    }
    
    System.out.println("FindMeetingQuery returns: " + meetings);
    return meetings;
  }

  /** Helper func: make new proposed meets by adjusting previous proposed
    * meets to exclude times that conflict with @events
    */ 
  public ArrayList<TimeRange> makeNewProposedMeetings(
    ArrayList<TimeRange> meetings,
    TimeRange eventTimeRange,
    MeetingRequest request) {
    // TODO
    ArrayList<TimeRange> tempListMeetings = new ArrayList<TimeRange>();

    for (int i = 0; i < meetings.size(); i++) {
      TimeRange curMeetingTimeRange = meetings.get(i);
      System.out.println("curMeetingTimeRange: " + curMeetingTimeRange.start() + " to " + curMeetingTimeRange.end());

      if (eventTimeRange.overlaps(curMeetingTimeRange)) {
        System.out.println("Event overlaps with current proposed meeting timeranges");
        if (eventTimeRange.start() < curMeetingTimeRange.start()) {
          if (eventTimeRange.end() < curMeetingTimeRange.end()) {
            System.out.println("Overlap type: case 1");
            /** Case 1:
              *     |--Existing Event--|
              *                |--Conflict--|
              */
            TimeRange tempProposedMeeting = 
              TimeRange.fromStartEnd(
                eventTimeRange.end(), 
                curMeetingTimeRange.end(), 
                false);

            System.out.println("Suggested TimeRange 1: " + tempProposedMeeting.start() + " to " + tempProposedMeeting.end());

            tempListMeetings.add(tempProposedMeeting);
          } else {
            System.out.println("Overlap type: case 2");
            /** Case 2:
              *     |--Existing Event--|
              *         |--Conflict--|
              */
            continue;
          }
        } else {
          if (eventTimeRange.end() < curMeetingTimeRange.end()) {
            System.out.println("Overlap type: case 3");
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
            
            System.out.println("Suggested TimeRange 1: " + tempProposedMeetingA.start() + " to " + tempProposedMeetingA.end());
            System.out.println("Suggested TimeRange 1: " + tempProposedMeetingB.start() + " to " + tempProposedMeetingB.end());
            
            tempListMeetings.add(tempProposedMeetingA);
            tempListMeetings.add(tempProposedMeetingB);
          } else {
            System.out.println("Overlap type: case 4");
            /** Case 4:
              *            |--Existing Event--|
              *     |--Conflict--|
              */
            TimeRange tempProposedMeeting = 
              TimeRange.fromStartEnd(
                curMeetingTimeRange.start(),
                eventTimeRange.start(),
                false);
            
            System.out.println("Suggested TimeRange 1: " + tempProposedMeeting.start() + " to " + tempProposedMeeting.end());
            
            tempListMeetings.add(tempProposedMeeting);
          }
        }
      } else {
        /** If event doesn't conflict with current proposed meet times,
          * then keep current set of proposed meets
          */
        tempListMeetings.add(curMeetingTimeRange);
        System.out.println("Event does not conflict with current proposed meet times. Proposed meets: " + tempListMeetings);
      }
    }

    System.out.println("tempListMeetings: " + tempListMeetings);

    // Remove proposed meetings that are not long enough to fit request
    Iterator<TimeRange> iter = tempListMeetings.iterator();
    System.out.println("Initiated iteration through  tempListMeetings...");
    while (iter.hasNext()) {
      TimeRange tempMeeting = iter.next();
      System.out.println("Current TimeRange element: " + tempMeeting.start() + " to " + tempMeeting.end());

      if (tempMeeting.duration() < request.getDuration()) {
        System.out.println("Duration of suggested time too short; removing suggestion...");
        iter.remove();
      }
    }

    return tempListMeetings;
  }
}