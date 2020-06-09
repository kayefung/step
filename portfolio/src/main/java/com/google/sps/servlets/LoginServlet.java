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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.gson.Gson;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    ArrayList<String> loginStatus = new ArrayList<>();

    // Returns login status of user and login/logout URL
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      loginStatus.add("True");
      
      String redirectUrl = "/index.html";
      String logoutUrl = userService.createLogoutURL(redirectUrl);
      loginStatus.add(logoutUrl);
    } else {
      loginStatus.add("False");

      String redirectUrl = "/index.html";
      String loginUrl = userService.createLoginURL(redirectUrl);
      loginStatus.add(loginUrl);
    }

    // Convert loginStatus ArrayList to JSON String using GSON library. 
    Gson gson = new Gson();
    String json = gson.toJson(loginStatus);

    response.getWriter().println(json);
  }
}