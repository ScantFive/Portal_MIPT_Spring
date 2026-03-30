package com.mipt.chat.service;

import java.util.Comparator;

import com.mipt.chat.model.Message;

public class MessageComparator implements Comparator<Message> {

  @Override
  public int compare(Message message0, Message message1) {
    return message0.getSendingTime().compareTo(message1.getSendingTime());
  }
}
