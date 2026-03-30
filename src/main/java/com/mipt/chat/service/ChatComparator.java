package com.mipt.chat.service;

import com.mipt.chat.model.Chat;
import java.util.Comparator;

public class ChatComparator implements Comparator<Chat> {

  @Override
  public int compare(Chat chat0, Chat chat1) {
    return chat0.getLastUpdate().compareTo(chat1.getLastUpdate());
  }
}
