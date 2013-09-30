package de.uniluebeck.itm.tr.federator.iwsn;

import de.uniluebeck.itm.tr.federator.utils.FederatedEndpoints;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.NodeUrnPrefix;
import eu.wisebed.api.v3.wsn.WSN;

import java.util.Set;

public interface WSNFederatorServiceFactory {

	WSNFederatorService create(WSNFederatorController wsnFederatorController,
							   FederatedEndpoints<WSN> federatedEndpoints,
							   Set<NodeUrnPrefix> nodeUrnPrefixes,
							   Set<NodeUrn> nodeUrns);

}
