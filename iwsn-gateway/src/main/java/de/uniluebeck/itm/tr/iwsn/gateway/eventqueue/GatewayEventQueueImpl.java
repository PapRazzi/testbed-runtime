package de.uniluebeck.itm.tr.iwsn.gateway.eventqueue;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.protobuf.MessageLite;
import com.leansoft.bigqueue.IBigQueue;
import de.uniluebeck.itm.tr.iwsn.messages.Message;
import de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.NotSerializableException;

public class GatewayEventQueueImpl implements GatewayEventQueue {
    private static Logger log = LoggerFactory.
            getLogger(GatewayEventQueueImpl.class);
    private final Object queueLock = new Object();
    private Channel channel;
    private IBigQueue queue;
    private MultiClassSerializationHelper<MessageLite> serializationHelper;
    private ListenableFuture<byte[]> dequeueFuture;

    @Inject
    public GatewayEventQueueImpl(final GatewayEventQueueHelper queueHelper) {

        try {
            queue = queueHelper.createAndConfigureQueue();
        } catch (IOException e) {
            log.error("Failed to create event queue! Event persistance not available!", e);
        }

        try {
            serializationHelper = queueHelper.configureEventSerializationHelper();
        } catch (Exception e) {
            log.error("Failed to configure serialization helper! Event persistance not available!", e);
        }
        log.trace("GatewayEventQueueImpl configured successfully");
    }

    @Override
    public void channelConnected(final Channel channel) {
        synchronized (queueLock) {
            this.channel = channel;
            dequeueFuture = queue.dequeueAsync();
            Futures.addCallback(dequeueFuture, buildFutureCallback());
            log.trace("channelConnected(): dequeue future callback added");
        }
    }

    @Override
    public void channelDisconnected() {
        synchronized (queueLock) {
            dequeueFuture.cancel(true);
            this.channel = null;
            dequeueFuture = null;
            log.trace("channelDisconnected(): dequeue future canceled");
        }
    }

    @Override
    public void enqueue(Message message) throws UnsupportedOperationException {
        if (isPersistanceAvailable()) {
            synchronized (queueLock) {
                try {
                    byte[] serialization = serializationHelper.serialize(message);
                    log.trace("queue.enqueue");
                    queue.enqueue(serialization);
                } catch (NotSerializableException e) {
                    log.error("The message {} is not serializable. An appropriate serializer is missing!", message);
                    throw buildEnqueueException(message);
                } catch (IOException e) {
                    log.error("Failed to enqueue message {}", message);
                    throw buildEnqueueException(message);
                }
            }
        } else if (channel != null) {
            synchronized (queueLock) {
                log.trace("enqueue({}): Persistance isn't available. Writing message directly to channel", message);
                Channels.write(channel, message);
            }
        } else {
            throw buildEnqueueException(message);
        }
    }

    private UnsupportedOperationException buildEnqueueException(Message message) {
        return new UnsupportedOperationException("EventQueue was neither able to enqueue nor to send the message [" + message + "] - giving up!");
    }

    private boolean isPersistanceAvailable() {
        return queue != null && serializationHelper != null;
    }


    private FutureCallback<byte[]> buildFutureCallback() {
        return new FutureCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                if (serializationHelper != null && channel != null) {
                    Object message = serializationHelper.deserialize(result);
                    log.trace("writing #{} to channel", message);
                    Channels.write(channel, message);
                    if (dequeueFuture != null) {
                        dequeueFuture = queue.dequeueAsync();
                        Futures.addCallback(dequeueFuture, buildFutureCallback());
                    }
                } else {
                    log.error("Failed to deserialize and send the message. Channel or serialization helper may be NULL");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                log.warn("Dequeue Future was canceled!", t);
            }
        };
    }
}
