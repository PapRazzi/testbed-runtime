// Generated by the protocol buffer compiler.  DO NOT EDIT!

package de.uniluebeck.itm.tr.iwsn.messages;

public interface ChannelHandlerConfigurationOrBuilder
    extends com.google.protobuf.MessageOrBuilder {
  
  // required string name = 1;
  boolean hasName();
  String getName();
  
  // repeated .de.uniluebeck.itm.tr.iwsn.messages.ChannelHandlerConfiguration.KeyValuePair configuration = 2;
  java.util.List<de.uniluebeck.itm.tr.iwsn.messages.ChannelHandlerConfiguration.KeyValuePair> 
      getConfigurationList();
  de.uniluebeck.itm.tr.iwsn.messages.ChannelHandlerConfiguration.KeyValuePair getConfiguration(int index);
  int getConfigurationCount();
  java.util.List<? extends de.uniluebeck.itm.tr.iwsn.messages.ChannelHandlerConfiguration.KeyValuePairOrBuilder> 
      getConfigurationOrBuilderList();
  de.uniluebeck.itm.tr.iwsn.messages.ChannelHandlerConfiguration.KeyValuePairOrBuilder getConfigurationOrBuilder(
      int index);
}