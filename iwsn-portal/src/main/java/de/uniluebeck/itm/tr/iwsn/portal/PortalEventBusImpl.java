package de.uniluebeck.itm.tr.iwsn.portal;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import de.uniluebeck.itm.tr.iwsn.common.netty.ExceptionChannelHandler;
import de.uniluebeck.itm.tr.iwsn.messages.Message;
import de.uniluebeck.itm.tr.iwsn.messages.MessageHeaderPair;
import de.uniluebeck.itm.tr.iwsn.portal.netty.NettyServer;
import de.uniluebeck.itm.tr.iwsn.portal.netty.NettyServerFactory;
import de.uniluebeck.itm.tr.iwsn.common.KeepAliveHandler;
import de.uniluebeck.itm.tr.iwsn.common.MessageUnwrapper;
import de.uniluebeck.itm.tr.iwsn.common.MessageWrapper;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;

class PortalEventBusImpl extends AbstractService implements PortalEventBus {

	private final Logger log = LoggerFactory.getLogger(PortalEventBusImpl.class);

	private final PortalServerConfig config;

	private final EventBus eventBus;

	private final NettyServerFactory nettyServerFactory;

	private final PortalChannelHandler portalChannelHandler;

	private NettyServer nettyServer;

	@Inject
	public PortalEventBusImpl(final PortalServerConfig config,
							  final EventBusFactory eventBusFactory,
							  final NettyServerFactory nettyServerFactory,
							  final PortalChannelHandler portalChannelHandler) {
		this.config = config;
		this.eventBus = eventBusFactory.create("PortalEventBus");
		this.nettyServerFactory = nettyServerFactory;
		this.portalChannelHandler = portalChannelHandler;
	}

	@Override
	public void register(final Object object) {
		eventBus.register(object);
	}

	@Override
	public void unregister(final Object object) {
		eventBus.unregister(object);
	}

	@Override
	public void post(final Object event) {
		if (MessageHeaderPair.isWrappedMessageEvent(event)) {
			throw new IllegalArgumentException("Posting wrapped messages is not allowed. Please use a concrete event / request / response type!");
		}
		eventBus.post(event);
	}

	@Override
	protected void doStart() {

		log.trace("PortalEventBusImpl.doStart()");

		try {

			portalChannelHandler.doStart();

			final ChannelPipelineFactory pipelineFactory = () -> {
				//noinspection unchecked
				return Channels.pipeline(
						new ExceptionChannelHandler(ClosedChannelException.class),
						new IdleStateHandler(new HashedWheelTimer(), 30, 15, 0),
						new ProtobufVarint32FrameDecoder(),
						new ProtobufDecoder(Message.getDefaultInstance()),
						new ProtobufVarint32LengthFieldPrepender(),
						new ProtobufEncoder(),
						new KeepAliveHandler(),
						new MessageWrapper(),
						portalChannelHandler
				);
			};

			nettyServer = nettyServerFactory.create(new InetSocketAddress(config.getGatewayPort()), pipelineFactory);
			nettyServer.startAsync().awaitRunning();

			notifyStarted();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {

		log.trace("PortalEventBusImpl.doStop()");

		try {

			nettyServer.stopAsync().awaitTerminated();
			portalChannelHandler.doStop();

			notifyStopped();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}
}
