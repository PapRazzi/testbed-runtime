// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/iwsn-messages.proto

package de.uniluebeck.itm.tr.iwsn.messages;

public interface GatewayDisconnectedEventOrBuilder
    extends com.google.protobuf.MessageOrBuilder {

  // required uint64 timestamp = 1;
  /**
   * <code>required uint64 timestamp = 1;</code>
   */
  boolean hasTimestamp();
  /**
   * <code>required uint64 timestamp = 1;</code>
   */
  long getTimestamp();

  // required string hostname = 2;
  /**
   * <code>required string hostname = 2;</code>
   */
  boolean hasHostname();
  /**
   * <code>required string hostname = 2;</code>
   */
  java.lang.String getHostname();
  /**
   * <code>required string hostname = 2;</code>
   */
  com.google.protobuf.ByteString
      getHostnameBytes();

  // repeated string nodeUrns = 3;
  /**
   * <code>repeated string nodeUrns = 3;</code>
   */
  java.util.List<java.lang.String>
  getNodeUrnsList();
  /**
   * <code>repeated string nodeUrns = 3;</code>
   */
  int getNodeUrnsCount();
  /**
   * <code>repeated string nodeUrns = 3;</code>
   */
  java.lang.String getNodeUrns(int index);
  /**
   * <code>repeated string nodeUrns = 3;</code>
   */
  com.google.protobuf.ByteString
      getNodeUrnsBytes(int index);
}
