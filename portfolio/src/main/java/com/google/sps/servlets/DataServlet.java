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
import com.google.sps.data.Comment;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

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

    ArrayList<Comment> comments = new ArrayList<>();
    
    for (Entity entity : results.asIterable(fetchOptions)) {
      String nickname = (String) entity.getProperty("nickname");
      String text = (String) entity.getProperty("text");
      Double score = (double) entity.getProperty("score");

      comments.add(new Comment(nickname, text, score));
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

    // Only stores comment if user is logged in. 
    if (userService.isUserLoggedIn()) {
      String commentText = request.getParameter("comment-input");
      long timestamp = System.currentTimeMillis();
      String email = userService.getCurrentUser().getEmail();
      String nickname = request.getParameter("nickname-input");
      float score = 0.2f;//calculateSentimentScore(commentText);
      
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("text", commentText);
      commentEntity.setProperty("timestamp", timestamp);
      commentEntity.setProperty("email", email);
      commentEntity.setProperty("nickname", nickname);
      commentEntity.setProperty("score", score);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
    }

    // Redirect back to the homepage.
    response.sendRedirect("/index.html");
  }

  /**
   * Calculates sentiment score of a comment's text and returns the score as a float. 
   */
  private float calculateSentimentScore(String text) throws IOException {
    Document doc =
        Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    return score;
  }
}