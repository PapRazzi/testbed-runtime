package de.uniluebeck.itm.tr.iwsn.common;

import de.uniluebeck.itm.tr.iwsn.messages.SingleNodeResponse;
import de.uniluebeck.itm.tr.util.ProgressListenableFutureMap;
import eu.wisebed.api.v3.common.NodeUrn;

public interface ResponseTracker extends ProgressListenableFutureMap<NodeUrn, SingleNodeResponse> {

}
