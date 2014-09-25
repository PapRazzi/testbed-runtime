// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/iwsn-messages.proto

package de.uniluebeck.itm.tr.iwsn.messages;

/**
 * Protobuf type {@code de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent}
 */
public  final class DeviceConfigDeletedEvent extends
    com.google.protobuf.GeneratedMessage
    implements DeviceConfigDeletedEventOrBuilder {
  // Use DeviceConfigDeletedEvent.newBuilder() to construct.
  private DeviceConfigDeletedEvent(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private DeviceConfigDeletedEvent(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

  private static final DeviceConfigDeletedEvent defaultInstance;
  public static DeviceConfigDeletedEvent getDefaultInstance() {
    return defaultInstance;
  }

  public DeviceConfigDeletedEvent getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final com.google.protobuf.UnknownFieldSet unknownFields;
  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
      getUnknownFields() {
    return this.unknownFields;
  }
  private DeviceConfigDeletedEvent(
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
          case 10: {
            bitField0_ |= 0x00000001;
            nodeUrn_ = input.readBytes();
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
    return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_DeviceConfigDeletedEvent_descriptor;
  }

  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_DeviceConfigDeletedEvent_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.class, de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.Builder.class);
  }

  public static com.google.protobuf.Parser<DeviceConfigDeletedEvent> PARSER =
      new com.google.protobuf.AbstractParser<DeviceConfigDeletedEvent>() {
    public DeviceConfigDeletedEvent parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new DeviceConfigDeletedEvent(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public com.google.protobuf.Parser<DeviceConfigDeletedEvent> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  // required string nodeUrn = 1;
  public static final int NODEURN_FIELD_NUMBER = 1;
  private java.lang.Object nodeUrn_;
  /**
   * <code>required string nodeUrn = 1;</code>
   */
  public boolean hasNodeUrn() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>required string nodeUrn = 1;</code>
   */
  public java.lang.String getNodeUrn() {
    java.lang.Object ref = nodeUrn_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      if (bs.isValidUtf8()) {
        nodeUrn_ = s;
      }
      return s;
    }
  }
  /**
   * <code>required string nodeUrn = 1;</code>
   */
  public com.google.protobuf.ByteString
      getNodeUrnBytes() {
    java.lang.Object ref = nodeUrn_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      nodeUrn_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private void initFields() {
    nodeUrn_ = "";
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized != -1) return isInitialized == 1;

    if (!hasNodeUrn()) {
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
      output.writeBytes(1, getNodeUrnBytes());
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
        .computeBytesSize(1, getNodeUrnBytes());
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

  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent prototype) {
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
   * Protobuf type {@code de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder>
     implements de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEventOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_DeviceConfigDeletedEvent_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_DeviceConfigDeletedEvent_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.class, de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.Builder.class);
    }

    // Construct using de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.newBuilder()
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
      nodeUrn_ = "";
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_DeviceConfigDeletedEvent_descriptor;
    }

    public de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent getDefaultInstanceForType() {
      return de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.getDefaultInstance();
    }

    public de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent build() {
      de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent buildPartial() {
      de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent result = new de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
        to_bitField0_ |= 0x00000001;
      }
      result.nodeUrn_ = nodeUrn_;
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
    }

    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent) {
        return mergeFrom((de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent other) {
      if (other == de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent.getDefaultInstance()) return this;
      if (other.hasNodeUrn()) {
        bitField0_ |= 0x00000001;
        nodeUrn_ = other.nodeUrn_;
        onChanged();
      }
      this.mergeUnknownFields(other.getUnknownFields());
      return this;
    }

    public final boolean isInitialized() {
      if (!hasNodeUrn()) {
        
        return false;
      }
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    // required string nodeUrn = 1;
    private java.lang.Object nodeUrn_ = "";
    /**
     * <code>required string nodeUrn = 1;</code>
     */
    public boolean hasNodeUrn() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string nodeUrn = 1;</code>
     */
    public java.lang.String getNodeUrn() {
      java.lang.Object ref = nodeUrn_;
      if (!(ref instanceof java.lang.String)) {
        java.lang.String s = ((com.google.protobuf.ByteString) ref)
            .toStringUtf8();
        nodeUrn_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>required string nodeUrn = 1;</code>
     */
    public com.google.protobuf.ByteString
        getNodeUrnBytes() {
      java.lang.Object ref = nodeUrn_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        nodeUrn_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>required string nodeUrn = 1;</code>
     */
    public Builder setNodeUrn(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
      nodeUrn_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>required string nodeUrn = 1;</code>
     */
    public Builder clearNodeUrn() {
      bitField0_ = (bitField0_ & ~0x00000001);
      nodeUrn_ = getDefaultInstance().getNodeUrn();
      onChanged();
      return this;
    }
    /**
     * <code>required string nodeUrn = 1;</code>
     */
    public Builder setNodeUrnBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
      nodeUrn_ = value;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent)
  }

  static {
    defaultInstance = new DeviceConfigDeletedEvent(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:de.uniluebeck.itm.tr.iwsn.messages.DeviceConfigDeletedEvent)
}

