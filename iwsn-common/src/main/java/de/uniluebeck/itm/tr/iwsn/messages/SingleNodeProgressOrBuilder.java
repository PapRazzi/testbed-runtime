// Generated by the protocol buffer compiler.  DO NOT EDIT!

package de.uniluebeck.itm.tr.iwsn.messages;

public interface SingleNodeProgressOrBuilder
    extends com.google.protobuf.MessageOrBuilder {
  
  // required int64 requestId = 1;
  boolean hasRequestId();
  long getRequestId();
  
  // required string nodeUrn = 2;
  boolean hasNodeUrn();
  String getNodeUrn();
  
  // required uint32 progressInPercent = 3;
  boolean hasProgressInPercent();
  int getProgressInPercent();
}