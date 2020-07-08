package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    Gson gson = new Gson();
    PrintWriter out = response.getWriter();
    UserService userService = UserServiceFactory.getUserService();

    // Responds with login status of user
    boolean loggedIn = userService.isUserLoggedIn();
    String url;
    if (loggedIn) {
      String logoutUrl = userService.createLogoutURL("/");
      url = logoutUrl;
    } else {
      String loginUrl = userService.createLoginURL("/");
      url = loginUrl;
    }

    String json = "{";
    json += "\"isLoggedIn\": ";
    json += loggedIn;
    json += ", ";
    json += "\"url\": ";
    json += "\"" + url + "\"";
    json += "}";
    out.println(json);
  }
}