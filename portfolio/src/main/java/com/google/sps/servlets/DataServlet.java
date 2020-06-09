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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int maxComments;
    try {
        maxComments = Integer.parseInt(request.getParameter("max-comments"));
    }
    catch (NumberFormatException e) {
        // Default to displaying 3 comments in case of error.
        maxComments = 3;
    }
    
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(maxComments);

    ArrayList<HashMap> comments = new ArrayList<>();
    
    for (Entity entity : results.asIterable(fetchOptions)) {
      HashMap<String, String> comment = new HashMap<String, String>();
      
      String userEmail = (String) entity.getProperty("email");
      String commentText = (String) entity.getProperty("text");
      
      comment.put("email", userEmail);
      comment.put("text", commentText);

      comments.add(comment);
    }

    // Convert comments ArrayList to JSON String using GSON library. 
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    String commentText = request.getParameter("comment-input");
    long timestamp = System.currentTimeMillis();
    String email = userService.getCurrentUser().getEmail();
    
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", commentText);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("email", email);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the homepage.
    response.sendRedirect("/index.html");
  }
}