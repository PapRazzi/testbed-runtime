package de.uniluebeck.itm.tr.wsn.federator;

import eu.wisebed.testbed.api.wsn.v23.WSN;

import java.util.List;

class WSNAreNodesAliveRunnable extends AbstractRequestRunnable {

	private List<String> nodes;

	WSNAreNodesAliveRunnable(FederatorController federatorController, WSN wsnEndpoint,
							 String federatorRequestId,
							 List<String> nodes) {
		super(federatorController, wsnEndpoint, federatorRequestId);
		this.nodes = nodes;
	}

	@Override
	public void run() {
		// instance wsnEndpoint is potentially not thread-safe!!!
		synchronized (wsnEndpoint) {
			done(wsnEndpoint.areNodesAlive(nodes));
		}
	}
}