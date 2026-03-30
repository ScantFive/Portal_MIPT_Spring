package com.mipt.service;

import com.mipt.model.chat.Message;

import java.util.Comparator;

public class MessageComparator implements Comparator<Message> {

  @Override
  public int compare(Message message0, Message message1) {
    return message0.getSendingTime().compareTo(message1.getSendingTime());
  }
}
