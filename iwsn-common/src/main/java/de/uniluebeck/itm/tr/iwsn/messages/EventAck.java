// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/iwsn-messages.proto

package de.uniluebeck.itm.tr.iwsn.messages;

/**
 * Protobuf type {@code de.uniluebeck.itm.tr.iwsn.messages.EventAck}
 */
public  final class EventAck extends
    com.google.protobuf.GeneratedMessage
    implements EventAckOrBuilder {
  // Use EventAck.newBuilder() to construct.
  private EventAck(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private EventAck(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

  private static final EventAck defaultInstance;
  public static EventAck getDefaultInstance() {
    return defaultInstance;
  }

  public EventAck getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final com.google.protobuf.UnknownFieldSet unknownFields;
  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
      getUnknownFields() {
    return this.unknownFields;
  }
  private EventAck(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    initFields();
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownField(input, unknownFields,
                                   extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 8: {
            bitField0_ |= 0x00000001;
            eventId_ = input.readInt64();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e.getMessage()).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_EventAck_descriptor;
  }

  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_EventAck_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            de.uniluebeck.itm.tr.iwsn.messages.EventAck.class, de.uniluebeck.itm.tr.iwsn.messages.EventAck.Builder.class);
  }

  public static com.google.protobuf.Parser<EventAck> PARSER =
      new com.google.protobuf.AbstractParser<EventAck>() {
    public EventAck parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new EventAck(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public com.google.protobuf.Parser<EventAck> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  // required int64 eventId = 1;
  public static final int EVENTID_FIELD_NUMBER = 1;
  private long eventId_;
  /**
   * <code>required int64 eventId = 1;</code>
   */
  public boolean hasEventId() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>required int64 eventId = 1;</code>
   */
  public long getEventId() {
    return eventId_;
  }

  private void initFields() {
    eventId_ = 0L;
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized != -1) return isInitialized == 1;

    if (!hasEventId()) {
      memoizedIsInitialized = 0;
      return false;
    }
    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      output.writeInt64(1, eventId_);
    }
    getUnknownFields().writeTo(output);
  }

  private int memoizedSerializedSize = -1;
  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, eventId_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSerializedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  protected java.lang.Object writeReplace()
      throws java.io.ObjectStreamException {
    return super.writeReplace();
  }

  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.EventAck parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(de.uniluebeck.itm.tr.iwsn.messages.EventAck prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code de.uniluebeck.itm.tr.iwsn.messages.EventAck}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder>
     implements de.uniluebeck.itm.tr.iwsn.messages.EventAckOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_EventAck_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_EventAck_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              de.uniluebeck.itm.tr.iwsn.messages.EventAck.class, de.uniluebeck.itm.tr.iwsn.messages.EventAck.Builder.class);
    }

    // Construct using de.uniluebeck.itm.tr.iwsn.messages.EventAck.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
      }
    }
    private static Builder create() {
      return new Builder();
    }

    public Builder clear() {
      super.clear();
      eventId_ = 0L;
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_EventAck_descriptor;
    }

    public de.uniluebeck.itm.tr.iwsn.messages.EventAck getDefaultInstanceForType() {
      return de.uniluebeck.itm.tr.iwsn.messages.EventAck.getDefaultInstance();
    }

    public de.uniluebeck.itm.tr.iwsn.messages.EventAck build() {
      de.uniluebeck.itm.tr.iwsn.messages.EventAck result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public de.uniluebeck.itm.tr.iwsn.messages.EventAck buildPartial() {
      de.uniluebeck.itm.tr.iwsn.messages.EventAck result = new de.uniluebeck.itm.tr.iwsn.messages.EventAck(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
        to_bitField0_ |= 0x00000001;
      }
      result.eventId_ = eventId_;
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
    }

    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof de.uniluebeck.itm.tr.iwsn.messages.EventAck) {
        return mergeFrom((de.uniluebeck.itm.tr.iwsn.messages.EventAck)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(de.uniluebeck.itm.tr.iwsn.messages.EventAck other) {
      if (other == de.uniluebeck.itm.tr.iwsn.messages.EventAck.getDefaultInstance()) return this;
      if (other.hasEventId()) {
        setEventId(other.getEventId());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      return this;
    }

    public final boolean isInitialized() {
      if (!hasEventId()) {
        
        return false;
      }
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      de.uniluebeck.itm.tr.iwsn.messages.EventAck parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (de.uniluebeck.itm.tr.iwsn.messages.EventAck) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    // required int64 eventId = 1;
    private long eventId_ ;
    /**
     * <code>required int64 eventId = 1;</code>
     */
    public boolean hasEventId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required int64 eventId = 1;</code>
     */
    public long getEventId() {
      return eventId_;
    }
    /**
     * <code>required int64 eventId = 1;</code>
     */
    public Builder setEventId(long value) {
      bitField0_ |= 0x00000001;
      eventId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>required int64 eventId = 1;</code>
     */
    public Builder clearEventId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      eventId_ = 0L;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:de.uniluebeck.itm.tr.iwsn.messages.EventAck)
  }

  static {
    defaultInstance = new EventAck(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:de.uniluebeck.itm.tr.iwsn.messages.EventAck)
}

