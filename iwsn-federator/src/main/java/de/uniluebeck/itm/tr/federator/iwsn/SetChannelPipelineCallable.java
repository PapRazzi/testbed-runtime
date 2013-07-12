package de.uniluebeck.itm.tr.federator.iwsn;

import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.wsn.AuthorizationFault;
import eu.wisebed.api.v3.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.v3.wsn.ReservationNotRunningFault_Exception;
import eu.wisebed.api.v3.wsn.WSN;

import java.util.List;

public class SetChannelPipelineCallable extends AbstractRequestCallable {

	private final List<NodeUrn> nodeUrns;

	private final List<ChannelHandlerConfiguration> channelHandlerConfigurations;

	public SetChannelPipelineCallable(final WSNFederatorController federatorController,
									  final WSN wsnEndpoint,
									  final long federatedRequestId,
									  final long federatorRequestId,
									  final List<NodeUrn> nodeUrns,
									  final List<ChannelHandlerConfiguration> channelHandlerConfigurations) {

		super(federatorController, wsnEndpoint, federatedRequestId, federatorRequestId);

		this.nodeUrns = nodeUrns;
		this.channelHandlerConfigurations = channelHandlerConfigurations;
	}

	@Override
	protected void executeRequestOnFederatedTestbed(final long federatedRequestId)
			throws ReservationNotRunningFault_Exception, AuthorizationFault {
		wsnEndpoint.setChannelPipeline(federatedRequestId, nodeUrns, channelHandlerConfigurations);
	}
}