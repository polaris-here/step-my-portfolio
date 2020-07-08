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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private List<String> comments;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get input from the form
    int commentLimitChoice = getCommentLimitChoice(request);

    if (commentLimitChoice == -1) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter an integer between 1 and 10.");
      return;
    }

    // Load entries from database
    Query query = new Query("comment").addSort("timestamp", 
                                               SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (commentLimitChoice == 0) {
        break;
      }
      String commentEntry = (String) entity.getProperty("comment-text");

      comments.add(commentEntry);
      commentLimitChoice -= 1;
    }

    // Convert object to json
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json");
    response.getWriter().println(json);
    // response.sendRedirect("/index.html");
  }
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String text = getParameter(request, "text-input", "");
    boolean upperCase = Boolean.parseBoolean(getParameter(request, "upper-case", "false"));
    long timestamp = System.currentTimeMillis();
    
    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();

    // Convert the text to upper case.
    if (upperCase) {
      text = text.toUpperCase();
    }
    
    // Store comments in a database
    Entity taskEntity = new Entity("comment");
    taskEntity.setProperty("email", email);
    taskEntity.setProperty("timestamp", timestamp);
    taskEntity.setProperty("comment-text", text);


    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    // Redirect to original page after response
    response.sendRedirect("/index.html");
  }

  // Helper function: @return player choice for displayed comment limit
  private int getCommentLimitChoice(HttpServletRequest request) {
    // Get input from the form
    String commentLimitChoiceString = getParameter(request, "comment-limit-choice", "1");

    // Convert input to int
    int commentLimitChoice;
    try {
      commentLimitChoice = Integer.parseInt(commentLimitChoiceString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentLimitChoiceString);
      return -1;
    }

    // Check that input is between 1 and 10
    if (commentLimitChoice < 1 || commentLimitChoice > 10) {
      System.err.println("Player choice is out of range: " + commentLimitChoiceString);
      return -1;
    }

    return commentLimitChoice;
  }


  // Helper function: @return request parameter
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
