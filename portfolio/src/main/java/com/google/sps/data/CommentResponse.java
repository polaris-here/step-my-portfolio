package com.google.sps.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Represents a comment response
public class CommentResponse {

  private String comment;
  private String email;

  public CommentResponse(String comment, String email) {
    this.comment = comment;
    this.email = email;
  }

  public String getComment() {
    return comment;
  }

  public String getEmail() {
    return email;
  }
}
