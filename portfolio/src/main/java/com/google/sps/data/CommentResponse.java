package com.google.sps.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// Represents a comment response
public class CommentResponse {

  private List<String> comments = new ArrayList<>();
  private List<String> emails = new ArrayList<>();

  public CommentResponse(List<String> comments, List<String> emails) {
    this.comments = comments;
    this.emails = emails;
  }

  public List<String> getComments() {
    return comments;
  }

  public List<String> getEmails() {
    return emails;
  }
}
