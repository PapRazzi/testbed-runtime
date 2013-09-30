/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                  *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote *
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.tr.federator.iwsn;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uniluebeck.itm.servicepublisher.ServicePublisher;
import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
import de.uniluebeck.itm.tr.common.CommonPreconditions;
import de.uniluebeck.itm.tr.common.PreconditionsFactory;
import de.uniluebeck.itm.tr.federator.utils.FederatedEndpoints;
import de.uniluebeck.itm.tr.iwsn.common.DeliveryManager;
import de.uniluebeck.itm.tr.iwsn.common.DeliveryManagerController;
import de.uniluebeck.itm.tr.iwsn.common.DeliveryManagerTestbedClientController;
import de.uniluebeck.itm.util.SecureIdGenerator;
import de.uniluebeck.itm.util.TimedCache;
import eu.wisebed.api.v3.WisebedServiceHelper;
import eu.wisebed.api.v3.common.Message;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.NodeUrnPrefix;
import eu.wisebed.api.v3.controller.Notification;
import eu.wisebed.api.v3.controller.RequestStatus;
import eu.wisebed.api.v3.wsn.WSN;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

@WebService(
		name = "Controller",
		endpointInterface = "eu.wisebed.api.v3.controller.Controller",
		portName = "ControllerPort",
		serviceName = "ControllerService",
		targetNamespace = "http://wisebed.eu/api/v3/controller"
)
public class WSNFederatorControllerImpl extends AbstractService implements WSNFederatorController {

	private static final Logger log = LoggerFactory.getLogger(WSNFederatorControllerImpl.class);

	private static final int CACHE_TIMEOUT = 10;

	private static final TimeUnit CACHE_TIMEOUT_UNIT = TimeUnit.MINUTES;

	private final URI endpointUri;

	private final String contextPath;

	private final CommonPreconditions preconditions;

	/**
	 * Maps the federatedRequestId to the federatorRequestId (i.e. remote to local)
	 */
	private TimedCache<Long, Long> requestIdMappingCache = new TimedCache<Long, Long>(
			CACHE_TIMEOUT,
			CACHE_TIMEOUT_UNIT
	);

	/**
	 * Maps federatedRequestID to a list of received RequestStatus instances (multiple updates for one id possible). This
	 * map caches received RequestStatus instances until the final mapping of federatedRequestID to federatorRequestId is
	 * known. This should normally never happen, but in very fast networks, it may happen that an asynchronous status
	 * update is received before the mapping is set using addRequestIdMapping.
	 */
	private final TimedCache<Long, LinkedList<RequestStatus>> pendingRequestStatus =
			new TimedCache<Long, LinkedList<RequestStatus>>(CACHE_TIMEOUT, CACHE_TIMEOUT_UNIT);

	private final ServicePublisher servicePublisher;

	private final DeliveryManager deliveryManager;

	private final FederatedEndpoints<WSN> wsnFederatedEndpoints;

	private ServicePublisherService jaxWsService;

	@Inject
	public WSNFederatorControllerImpl(final ServicePublisher servicePublisher,
									  final DeliveryManager deliveryManager,
									  final IWSNFederatorServiceConfig config,
									  final SecureIdGenerator secureIdGenerator,
									  final PreconditionsFactory preconditionsFactory,
									  @Assisted final FederatedEndpoints<WSN> wsnFederatedEndpoints,
									  @Assisted final Set<NodeUrnPrefix> nodeUrnPrefixes,
									  @Assisted final Set<NodeUrn> nodeUrns) {

		this.servicePublisher = checkNotNull(servicePublisher);
		this.deliveryManager = checkNotNull(deliveryManager);
		this.wsnFederatedEndpoints = checkNotNull(wsnFederatedEndpoints);
		this.preconditions = preconditionsFactory.createCommonPreconditions(nodeUrnPrefixes, nodeUrns);

		final String id = secureIdGenerator.getNextId();
		final String endpointUriBaseString = config.getFederatorControllerEndpointUriBase().toString();
		this.contextPath = config.getFederatorControllerEndpointUriBase().getPath() +
				(config.getFederatorControllerEndpointUriBase().getPath().endsWith("/") ? id : "/" + id);
		this.endpointUri = URI.create(endpointUriBaseString + (endpointUriBaseString.endsWith("/") ? id : "/" + id));
	}

	@Override
	protected void doStart() {

		try {

			log.debug("Starting federator controller using endpoint URI {}...", endpointUri);

			jaxWsService = servicePublisher.createJaxWsService(contextPath, this);
			jaxWsService.startAndWait();

			deliveryManager.startAndWait();

			for (Map.Entry<WSN, URI> entry : wsnFederatedEndpoints.getEndpointsURIMap().entrySet()) {
				final WSN endpoint = entry.getKey();
				final URI uri = entry.getValue();
				try {
					endpoint.addController(getEndpointUrl().toString());
				} catch (Exception e) {
					log.error("Exception while adding federator controller to federated testbed " + uri + ": ", e);
					notifyFailed(e);
				}
			}

			log.debug("Started federator controller on {}!", endpointUri);
			notifyStarted();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {

		try {

			for (Map.Entry<WSN, URI> entry : wsnFederatedEndpoints.getEndpointsURIMap().entrySet()) {
				final WSN endpoint = entry.getKey();
				final URI uri = entry.getValue();
				try {
					endpoint.removeController(endpointUri.toString());
				} catch (Exception e) {
					log.error("Exception while removing federator controller from federated testbed " + uri + ": ", e);
				}
			}

			log.debug("Calling reservationEnded() on connected controllers...");
			deliveryManager.reservationEnded(DateTime.now());

			deliveryManager.stopAndWait();

			if (jaxWsService.isRunning()) {
				log.info("Stopping federator controller at {}...", endpointUri);
				jaxWsService.stopAndWait();
			}

			notifyStopped();

		} catch (Exception e) {
			notifyFailed(e);
		}

	}

	public void addRequestIdMapping(long federatedRequestId, long federatorRequestId) {

		// Add the mapping to the list
		log.debug("Mapping federatedRequestId {} to federatorRequestId {} = ", federatedRequestId, federatorRequestId);
		requestIdMappingCache.put(federatedRequestId, federatorRequestId);

		// Dispatch potentially received status updates
		final LinkedList<RequestStatus> requestStatusList = pendingRequestStatus.get(federatedRequestId);

		if (requestStatusList != null) {
			log.debug("Already got {} status updates for federatedRequestId {} ", requestStatusList.size(),
					federatedRequestId
			);

			// Dispatch all status updates and remove them from the list
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (requestStatusList) {
				for (RequestStatus status : requestStatusList) {
					changeIdAndDispatch(federatorRequestId, status);
				}
				pendingRequestStatus.remove(federatedRequestId);
			}
		}
	}

	public void addController(DeliveryManagerController controller) {
		log.trace("WSNFederatorControllerImpl.addController({})", controller);
		deliveryManager.addController(controller);
	}

	public void removeController(DeliveryManagerController controller) {
		log.trace("WSNFederatorControllerImpl.removeController({})", controller);
		deliveryManager.removeController(controller);
	}

	private DeliveryManagerTestbedClientController createController(final String controllerEndpointUrl) {
		return new DeliveryManagerTestbedClientController(
				WisebedServiceHelper.getControllerService(controllerEndpointUrl, null),
				controllerEndpointUrl
		);
	}

	private void receive(final Message msg) {
		preconditions.checkNodesKnown(msg.getSourceNodeUrn());
		deliveryManager.receive(msg);
	}

	/**
	 * Maps federatedRequestID to a list of received RequestStatus instances (multiple updates for one id possible). This
	 * map caches received RequestStatus instances until the final mapping of federatedRequestID to federatorRequestId is
	 * known. This should normally never happen, but in very fast networks, it may happen that an asynchronous status
	 * update is received before the mapping is set using addRequestIdMapping.
	 */
	private void cacheRequestStatus(RequestStatus status) {
		synchronized (pendingRequestStatus) {

			// If no entry for this request id exists, create a new list and add
			// it to the cache
			LinkedList<RequestStatus> requestStatusList = pendingRequestStatus.get(status.getRequestId());

			if (requestStatusList == null) {
				requestStatusList = new LinkedList<RequestStatus>();
				pendingRequestStatus.put(status.getRequestId(), requestStatusList);
			}

			// Append this status to the list for this request ID
			requestStatusList.add(status);
		}
	}

	/**
	 * Change the incoming request ID to the request ID that was issued by the federator to its client.
	 */
	private void changeIdAndDispatch(long newRequestId, RequestStatus status) {
		status.setRequestId(newRequestId);
		deliveryManager.receiveStatus(status);
	}

	private void receiveStatus(final RequestStatus status) {

		Long federatorRequestId = requestIdMappingCache.get(status.getRequestId());

		if (federatorRequestId != null) {
			// change the incoming request ID to the request ID that was issued
			// by the federator to its client
			changeIdAndDispatch(federatorRequestId, status);

		} else {
			log.warn("Unknown requestId {}. Caching the status update for " + CACHE_TIMEOUT + " " + CACHE_TIMEOUT_UNIT
					+ " until the federatedRequestId <-> federatorRequestIdDropping mapping is known", status
					.getRequestId()
			);
			cacheRequestStatus(status);
		}
	}

	@Override
	public void receive(final List<Message> messageList) {
		for (Message message : messageList) {
			log.trace("WSNFederatorControllerImpl.receive({})", message);
			receive(message);
		}
	}

	@Override
	public void receiveNotification(final List<Notification> notifications) {
		log.trace("WSNFederatorControllerImpl.receiveNotification({})", notifications);
		deliveryManager.receiveNotification(notifications);
	}

	@Override
	public void receiveStatus(final List<RequestStatus> requestStatusList) {
		log.trace("WSNFederatorControllerImpl.receiveStatus({})", requestStatusList);
		for (RequestStatus requestStatus : requestStatusList) {
			receiveStatus(requestStatus);
		}
	}

	@Override
	public void nodesAttached(final DateTime timestamp, final List<NodeUrn> nodeUrns) {
		log.trace("WSNFederatorControllerImpl.nodesAttached({}, {})", timestamp, nodeUrns);
		preconditions.checkNodesKnown(nodeUrns);
		deliveryManager.nodesAttached(timestamp, nodeUrns);
	}

	@Override
	public void nodesDetached(final DateTime timestamp, final List<NodeUrn> nodeUrns) {
		log.trace("WSNFederatorControllerImpl.nodesDetached({}, {})", timestamp, nodeUrns);
		preconditions.checkNodesKnown(nodeUrns);
		deliveryManager.nodesDetached(timestamp, nodeUrns);
	}

	@Override
	public void reservationStarted(final DateTime timestamp) {
		log.trace("WSNFederatorControllerImpl.reservationStarted({})", timestamp);
		deliveryManager.reservationStarted(timestamp);
	}

	@Override
	public void reservationEnded(final DateTime timestamp) {
		log.trace("WSNFederatorControllerImpl.reservationEnded({})", timestamp);
		deliveryManager.reservationEnded(timestamp);
	}

	public URI getEndpointUrl() {
		return endpointUri;
	}
}
