// // Copyright 2019 Google LLC
// //
// // Licensed under the Apache License, Version 2.0 (the "License");
// // you may not use this file except in compliance with the License.
// // You may obtain a copy of the License at
// //
// //     https://www.apache.org/licenses/LICENSE-2.0
// //
// // Unless required by applicable law or agreed to in writing, software
// // distributed under the License is distributed on an "AS IS" BASIS,
// // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// // See the License for the specific language governing permissions and
// // limitations under the License.

// package com.google.sps;

// import com.google.sps.TimeRange;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.BitSet;
// import java.util.Collection;
// import java.util.Collections;
// import java.util.Comparator;
// import java.util.HashSet;
// import java.util.Iterator;
// import java.util.Set;

// public final class FindMeetingQuery {
//   public static final int NUM_MINUTES_IN_DAY = 24 * 60;

//   /** Return a collection of TimeRanges where the requested attendees 
//     * are available to meet
//     */
//   public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
//     ArrayList<TimeRange> meetings = new ArrayList<>();
//     BitSet scheduleBitset = new BitSet(1440);
//     Collection<String> requestedAttendees = request.getAttendees();
//     Collection<String> requestedOptionalAttendees = 
//       request.getOptionalAttendees();
//     long durationOfRequest = request.getDuration();

//     /** Case 0: If duration too long (longer than a day)
//       *  or invalid input/negative time, return no options
//       */
//     if (durationOfRequest > NUM_MINUTES_IN_DAY || durationOfRequest < 0) {
//       return meetings;
//     }

//     /** Case 1: GENERAL CASE >> remove conflicting times(from @events) 
//       * from possible meetings
//       */
//     meetings.add(TimeRange.fromStartDuration(0, NUM_MINUTES_IN_DAY));
//     ArrayList<Event> optionalOverlappedEvents = new ArrayList<>();
//     for (Event event : events) {
//       Set<String> tempOverlappedEventAttendees = event.getAttendees();

//       Set<String> overlappedEventAttendees = 
//         new HashSet<String>(tempOverlappedEventAttendees);
//       Set<String> optionalOverlappedAttendees = 
//         new HashSet<String>(tempOverlappedEventAttendees);

//       // Filter events for overlaps with request
//       overlappedEventAttendees.retainAll(requestedAttendees);
//       optionalOverlappedAttendees.retainAll(requestedOptionalAttendees);

//       if (!overlappedEventAttendees.isEmpty()) {
//         // Event contains attendees that were requested
//         TimeRange eventTimeRange = event.getWhen();

//         // Resolve timing conflicts
//         scheduleBitset = makeNewProposedMeetings(scheduleBitset, eventTimeRange, request);

//         // Comparator<TimeRange> orderMeetings = TimeRange.ORDER_BY_START;
//         // Collections.sort(meetings, orderMeetings);
//       } else if (!optionalOverlappedAttendees.isEmpty()) {
//         // Event contains optional attendees that were requested
//         optionalOverlappedEvents.add(event);
//       }
//     }

//     // Handle optional events restrictions separately from mandatory ones
//     BitSet bitsetWithOptional = (BitSet) scheduleBitset.clone();

//     for (Event event : optionalOverlappedEvents) {
//       TimeRange eventTimeRange = event.getWhen();

//       // Resolve timing conflicts
//       bitsetWithOptional = 
//         makeNewProposedMeetings(bitsetWithOptional, eventTimeRange, request);

//       // Comparator<TimeRange> orderMeetings = TimeRange.ORDER_BY_START;
//       // Collections.sort(meetingsWithOptional, orderMeetings);
//     }

//     // Return meeting set based on attendees and availabilities
//     if (requestedAttendees.isEmpty() && !requestedOptionalAttendees.isEmpty()) {
//       meetings = bitsetToMeetings(bitsetWithOptional, (int) durationOfRequest);
//       return meetings;
//     }

//     // If optionals attendees unavailable, do not include them
//     ArrayList<TimeRange> meetingsWithOptional = 
//       bitsetToMeetings(bitsetWithOptional, (int) durationOfRequest);

//     if (meetingsWithOptional.isEmpty()) {
//       meetings = bitsetToMeetings(scheduleBitset, (int) durationOfRequest);
//       return meetings;
//     }
//     return meetingsWithOptional;
//   }

//   /** Return new proposed meets by adjusting input proposed
//     * meets to exclude times that conflict with @events
//     */ 
//   public BitSet makeNewProposedMeetings(
//     BitSet scheduleBitset,
//     TimeRange eventTimeRange,
//     MeetingRequest request) {
//       int eventStart = eventTimeRange.start();
//       int eventEnd = eventTimeRange.end();

//       // Bits set to true are unavailable times
//       scheduleBitset.set(eventStart, eventEnd, true);

//       return scheduleBitset;
//   }

//   // Convert bitset to an arraylist of TimeRanges
//   public ArrayList<TimeRange> bitsetToMeetings(BitSet bitset, int requestDuration) {
//     ArrayList<TimeRange> meetings = new ArrayList<>();
//     for (int start = bitset.nextClearBit(0); start < 1440 && start >= 0;
//     start = bitset.nextClearBit(bitset.nextSetBit(start))) {
//       int end = bitset.nextSetBit(start);

//       if (end != -1) {
//         TimeRange meet = TimeRange.fromStartEnd(start, end, false);

//         if (requestDuration <= meet.duration()) {
//           meetings.add(meet);
//         }
//       } else {
//         TimeRange meet = TimeRange.fromStartEnd(start, 1440, false);

//         if (requestDuration <= meet.duration()) {
//           meetings.add(meet);
//         }
//         return meetings;
//       }
//     }

//     return meetings;
//   }
// }