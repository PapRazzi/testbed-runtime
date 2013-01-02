// Generated by the protocol buffer compiler.  DO NOT EDIT!

package de.uniluebeck.itm.tr.iwsn.messages;

public  final class SetVirtualLinksRequest extends
    com.google.protobuf.GeneratedMessage
    implements SetVirtualLinksRequestOrBuilder {
  // Use SetVirtualLinksRequest.newBuilder() to construct.
  private SetVirtualLinksRequest(Builder builder) {
    super(builder);
  }
  private SetVirtualLinksRequest(boolean noInit) {}
  
  private static final SetVirtualLinksRequest defaultInstance;
  public static SetVirtualLinksRequest getDefaultInstance() {
    return defaultInstance;
  }
  
  public SetVirtualLinksRequest getDefaultInstanceForType() {
    return defaultInstance;
  }
  
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_SetVirtualLinksRequest_descriptor;
  }
  
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_SetVirtualLinksRequest_fieldAccessorTable;
  }
  
  // repeated .de.uniluebeck.itm.tr.iwsn.messages.Link links = 1;
  public static final int LINKS_FIELD_NUMBER = 1;
  private java.util.List<de.uniluebeck.itm.tr.iwsn.messages.Link> links_;
  public java.util.List<de.uniluebeck.itm.tr.iwsn.messages.Link> getLinksList() {
    return links_;
  }
  public java.util.List<? extends de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder> 
      getLinksOrBuilderList() {
    return links_;
  }
  public int getLinksCount() {
    return links_.size();
  }
  public de.uniluebeck.itm.tr.iwsn.messages.Link getLinks(int index) {
    return links_.get(index);
  }
  public de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder getLinksOrBuilder(
      int index) {
    return links_.get(index);
  }
  
  private void initFields() {
    links_ = java.util.Collections.emptyList();
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized != -1) return isInitialized == 1;
    
    for (int i = 0; i < getLinksCount(); i++) {
      if (!getLinks(i).isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
    }
    memoizedIsInitialized = 1;
    return true;
  }
  
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    for (int i = 0; i < links_.size(); i++) {
      output.writeMessage(1, links_.get(i));
    }
    getUnknownFields().writeTo(output);
  }
  
  private int memoizedSerializedSize = -1;
  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;
  
    size = 0;
    for (int i = 0; i < links_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, links_.get(i));
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
  
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return newBuilder().mergeFrom(data).buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return newBuilder().mergeFrom(data, extensionRegistry)
             .buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return newBuilder().mergeFrom(data).buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return newBuilder().mergeFrom(data, extensionRegistry)
             .buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return newBuilder().mergeFrom(input).buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return newBuilder().mergeFrom(input, extensionRegistry)
             .buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    Builder builder = newBuilder();
    if (builder.mergeDelimitedFrom(input)) {
      return builder.buildParsed();
    } else {
      return null;
    }
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    Builder builder = newBuilder();
    if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
      return builder.buildParsed();
    } else {
      return null;
    }
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return newBuilder().mergeFrom(input).buildParsed();
  }
  public static de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return newBuilder().mergeFrom(input, extensionRegistry)
             .buildParsed();
  }
  
  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }
  
  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder>
     implements de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_SetVirtualLinksRequest_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return de.uniluebeck.itm.tr.iwsn.messages.Messages.internal_static_de_uniluebeck_itm_tr_iwsn_messages_SetVirtualLinksRequest_fieldAccessorTable;
    }
    
    // Construct using de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }
    
    private Builder(BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        getLinksFieldBuilder();
      }
    }
    private static Builder create() {
      return new Builder();
    }
    
    public Builder clear() {
      super.clear();
      if (linksBuilder_ == null) {
        links_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        linksBuilder_.clear();
      }
      return this;
    }
    
    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }
    
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest.getDescriptor();
    }
    
    public de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest getDefaultInstanceForType() {
      return de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest.getDefaultInstance();
    }
    
    public de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest build() {
      de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }
    
    private de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest buildParsed()
        throws com.google.protobuf.InvalidProtocolBufferException {
      de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(
          result).asInvalidProtocolBufferException();
      }
      return result;
    }
    
    public de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest buildPartial() {
      de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest result = new de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest(this);
      int from_bitField0_ = bitField0_;
      if (linksBuilder_ == null) {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          links_ = java.util.Collections.unmodifiableList(links_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.links_ = links_;
      } else {
        result.links_ = linksBuilder_.build();
      }
      onBuilt();
      return result;
    }
    
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest) {
        return mergeFrom((de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }
    
    public Builder mergeFrom(de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest other) {
      if (other == de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest.getDefaultInstance()) return this;
      if (linksBuilder_ == null) {
        if (!other.links_.isEmpty()) {
          if (links_.isEmpty()) {
            links_ = other.links_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureLinksIsMutable();
            links_.addAll(other.links_);
          }
          onChanged();
        }
      } else {
        if (!other.links_.isEmpty()) {
          if (linksBuilder_.isEmpty()) {
            linksBuilder_.dispose();
            linksBuilder_ = null;
            links_ = other.links_;
            bitField0_ = (bitField0_ & ~0x00000001);
            linksBuilder_ = 
              com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                 getLinksFieldBuilder() : null;
          } else {
            linksBuilder_.addAllMessages(other.links_);
          }
        }
      }
      this.mergeUnknownFields(other.getUnknownFields());
      return this;
    }
    
    public final boolean isInitialized() {
      for (int i = 0; i < getLinksCount(); i++) {
        if (!getLinks(i).isInitialized()) {
          
          return false;
        }
      }
      return true;
    }
    
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder(
          this.getUnknownFields());
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            this.setUnknownFields(unknownFields.build());
            onChanged();
            return this;
          default: {
            if (!parseUnknownField(input, unknownFields,
                                   extensionRegistry, tag)) {
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            }
            break;
          }
          case 10: {
            de.uniluebeck.itm.tr.iwsn.messages.Link.Builder subBuilder = de.uniluebeck.itm.tr.iwsn.messages.Link.newBuilder();
            input.readMessage(subBuilder, extensionRegistry);
            addLinks(subBuilder.buildPartial());
            break;
          }
        }
      }
    }
    
    private int bitField0_;
    
    // repeated .de.uniluebeck.itm.tr.iwsn.messages.Link links = 1;
    private java.util.List<de.uniluebeck.itm.tr.iwsn.messages.Link> links_ =
      java.util.Collections.emptyList();
    private void ensureLinksIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        links_ = new java.util.ArrayList<de.uniluebeck.itm.tr.iwsn.messages.Link>(links_);
        bitField0_ |= 0x00000001;
       }
    }
    
    private com.google.protobuf.RepeatedFieldBuilder<
        de.uniluebeck.itm.tr.iwsn.messages.Link, de.uniluebeck.itm.tr.iwsn.messages.Link.Builder, de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder> linksBuilder_;
    
    public java.util.List<de.uniluebeck.itm.tr.iwsn.messages.Link> getLinksList() {
      if (linksBuilder_ == null) {
        return java.util.Collections.unmodifiableList(links_);
      } else {
        return linksBuilder_.getMessageList();
      }
    }
    public int getLinksCount() {
      if (linksBuilder_ == null) {
        return links_.size();
      } else {
        return linksBuilder_.getCount();
      }
    }
    public de.uniluebeck.itm.tr.iwsn.messages.Link getLinks(int index) {
      if (linksBuilder_ == null) {
        return links_.get(index);
      } else {
        return linksBuilder_.getMessage(index);
      }
    }
    public Builder setLinks(
        int index, de.uniluebeck.itm.tr.iwsn.messages.Link value) {
      if (linksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureLinksIsMutable();
        links_.set(index, value);
        onChanged();
      } else {
        linksBuilder_.setMessage(index, value);
      }
      return this;
    }
    public Builder setLinks(
        int index, de.uniluebeck.itm.tr.iwsn.messages.Link.Builder builderForValue) {
      if (linksBuilder_ == null) {
        ensureLinksIsMutable();
        links_.set(index, builderForValue.build());
        onChanged();
      } else {
        linksBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    public Builder addLinks(de.uniluebeck.itm.tr.iwsn.messages.Link value) {
      if (linksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureLinksIsMutable();
        links_.add(value);
        onChanged();
      } else {
        linksBuilder_.addMessage(value);
      }
      return this;
    }
    public Builder addLinks(
        int index, de.uniluebeck.itm.tr.iwsn.messages.Link value) {
      if (linksBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureLinksIsMutable();
        links_.add(index, value);
        onChanged();
      } else {
        linksBuilder_.addMessage(index, value);
      }
      return this;
    }
    public Builder addLinks(
        de.uniluebeck.itm.tr.iwsn.messages.Link.Builder builderForValue) {
      if (linksBuilder_ == null) {
        ensureLinksIsMutable();
        links_.add(builderForValue.build());
        onChanged();
      } else {
        linksBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    public Builder addLinks(
        int index, de.uniluebeck.itm.tr.iwsn.messages.Link.Builder builderForValue) {
      if (linksBuilder_ == null) {
        ensureLinksIsMutable();
        links_.add(index, builderForValue.build());
        onChanged();
      } else {
        linksBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    public Builder addAllLinks(
        java.lang.Iterable<? extends de.uniluebeck.itm.tr.iwsn.messages.Link> values) {
      if (linksBuilder_ == null) {
        ensureLinksIsMutable();
        super.addAll(values, links_);
        onChanged();
      } else {
        linksBuilder_.addAllMessages(values);
      }
      return this;
    }
    public Builder clearLinks() {
      if (linksBuilder_ == null) {
        links_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        linksBuilder_.clear();
      }
      return this;
    }
    public Builder removeLinks(int index) {
      if (linksBuilder_ == null) {
        ensureLinksIsMutable();
        links_.remove(index);
        onChanged();
      } else {
        linksBuilder_.remove(index);
      }
      return this;
    }
    public de.uniluebeck.itm.tr.iwsn.messages.Link.Builder getLinksBuilder(
        int index) {
      return getLinksFieldBuilder().getBuilder(index);
    }
    public de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder getLinksOrBuilder(
        int index) {
      if (linksBuilder_ == null) {
        return links_.get(index);  } else {
        return linksBuilder_.getMessageOrBuilder(index);
      }
    }
    public java.util.List<? extends de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder> 
         getLinksOrBuilderList() {
      if (linksBuilder_ != null) {
        return linksBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(links_);
      }
    }
    public de.uniluebeck.itm.tr.iwsn.messages.Link.Builder addLinksBuilder() {
      return getLinksFieldBuilder().addBuilder(
          de.uniluebeck.itm.tr.iwsn.messages.Link.getDefaultInstance());
    }
    public de.uniluebeck.itm.tr.iwsn.messages.Link.Builder addLinksBuilder(
        int index) {
      return getLinksFieldBuilder().addBuilder(
          index, de.uniluebeck.itm.tr.iwsn.messages.Link.getDefaultInstance());
    }
    public java.util.List<de.uniluebeck.itm.tr.iwsn.messages.Link.Builder> 
         getLinksBuilderList() {
      return getLinksFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilder<
        de.uniluebeck.itm.tr.iwsn.messages.Link, de.uniluebeck.itm.tr.iwsn.messages.Link.Builder, de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder> 
        getLinksFieldBuilder() {
      if (linksBuilder_ == null) {
        linksBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
            de.uniluebeck.itm.tr.iwsn.messages.Link, de.uniluebeck.itm.tr.iwsn.messages.Link.Builder, de.uniluebeck.itm.tr.iwsn.messages.LinkOrBuilder>(
                links_,
                ((bitField0_ & 0x00000001) == 0x00000001),
                getParentForChildren(),
                isClean());
        links_ = null;
      }
      return linksBuilder_;
    }
    
    // @@protoc_insertion_point(builder_scope:de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest)
  }
  
  static {
    defaultInstance = new SetVirtualLinksRequest(true);
    defaultInstance.initFields();
  }
  
  // @@protoc_insertion_point(class_scope:de.uniluebeck.itm.tr.iwsn.messages.SetVirtualLinksRequest)
}

