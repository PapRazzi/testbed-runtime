package de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.resources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import de.uniluebeck.itm.tr.common.NodeUrnHelper;
import de.uniluebeck.itm.tr.devicedb.DeviceDBService;
import de.uniluebeck.itm.tr.iwsn.common.ResponseTracker;
import de.uniluebeck.itm.tr.iwsn.common.ResponseTrackerFactory;
import de.uniluebeck.itm.tr.iwsn.messages.Request;
import de.uniluebeck.itm.tr.iwsn.messages.SingleNodeResponse;
import de.uniluebeck.itm.tr.iwsn.portal.*;
import de.uniluebeck.itm.tr.iwsn.portal.api.RequestHelper;
import de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.dto.*;
import de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.exceptions.UnknownSecretReservationKeyException;
import de.uniluebeck.itm.util.TimedCache;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.SecretReservationKey;
import eu.wisebed.api.v3.wsn.ChannelPipelinesMap;
import eu.wisebed.wiseml.Capability;
import eu.wisebed.wiseml.Setup.Node;
import eu.wisebed.wiseml.Wiseml;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static de.uniluebeck.itm.tr.common.NodeUrnHelper.NODE_URN_TO_STRING;
import static de.uniluebeck.itm.tr.devicedb.WiseMLConverter.convertToWiseML;
import static de.uniluebeck.itm.tr.iwsn.messages.MessagesHelper.*;
import static de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.util.Base64Helper.*;

@Path("/experiments/")
public class ExperimentResource {

	private static final Logger log = LoggerFactory.getLogger(ExperimentResource.class);

	private static final Random RANDOM = new Random();

	private final TimedCache<Long, List<Long>> flashResponseTrackers;

	private final DeviceDBService deviceDBService;

	private final ReservationManager reservationManager;

	private final RequestIdProvider requestIdProvider;

	private final PortalEventBus portalEventBus;

	private final ResponseTrackerFactory responseTrackerFactory;

	@Context
	private UriInfo uriInfo;

	@Inject
	public ExperimentResource(final DeviceDBService deviceDBService,
							  final PortalEventBus portalEventBus,
							  final ResponseTrackerFactory responseTrackerFactory,
							  final ReservationManager reservationManager,
							  final RequestIdProvider requestIdProvider,
							  final TimedCache<Long, List<Long>> flashResponseTrackers) {
		this.deviceDBService = checkNotNull(deviceDBService);
		this.portalEventBus = checkNotNull(portalEventBus);
		this.responseTrackerFactory = checkNotNull(responseTrackerFactory);
		this.reservationManager = checkNotNull(reservationManager);
		this.requestIdProvider = checkNotNull(requestIdProvider);
		this.flashResponseTrackers = checkNotNull(flashResponseTrackers);
	}

	@GET
	@Path("network")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getNetwork() {
		log.trace("ExperimentResource.getNetwork()");
		return Response.ok(convertToWiseML(deviceDBService.getAll())).build();
	}

	@GET
	@Path("{secretReservationKeyBase64}/network")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getExperimentNetwork(
			@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64) throws Base64Exception {
		return Response.ok(getWiseml(secretReservationKeyBase64)).build();
	}

	@GET
	@Path("nodes")
	@Produces({MediaType.APPLICATION_JSON})
	public NodeUrnList getNodes(@QueryParam("filter") final String filter,
								@QueryParam("capability") final String capability) {

		final Wiseml wiseml = convertToWiseML(deviceDBService.getAll());

		NodeUrnList nodeList = new NodeUrnList();
		nodeList.nodeUrns = new LinkedList<String>();

		// First add all
		for (Node node : wiseml.getSetup().getNode()) {
			nodeList.nodeUrns.add(node.getId());
		}

		// Then remove non-matching ones
		for (Node node : wiseml.getSetup().getNode()) {
			boolean remove = false;
			String text = "" + node.getDescription() + " " + node.getId() + " " + node.getNodeType() + " " + node
					.getProgramDetails() + " "
					+ toString(node.getCapability());

			if (filter != null && !text.contains(filter)) {
				remove = true;
			}

			if (capability != null) {
				if (!toString(node.getCapability()).contains(capability)) {
					remove = true;
				}
			}

			if (remove) {
				nodeList.nodeUrns.remove(node.getId());
			}
		}

		return nodeList;
	}

	private String toString(List<Capability> capabilities) {
		StringBuilder sb = new StringBuilder();
		for (Capability c : capabilities) {
			sb.append(toString(c));
		}
		return sb.toString();

	}

	private String toString(Capability c) {
		StringBuilder sb = new StringBuilder();
		if (c.getName() != null) {
			sb.append(c.getName()).append(" ");
		}

		if (c.getDatatype() != null && c.getDatatype().value() != null) {
			sb.append(c.getDatatype().value()).append(" ");
		}

		if (c.getDefault() != null) {
			sb.append(c.getDefault()).append(" ");
		}

		if (c.getUnit() != null && c.getUnit().value() != null) {
			sb.append(c.getUnit().value()).append(" ");
		}

		return sb.toString();
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.TEXT_PLAIN})
	public Response getInstance(SecretReservationKeyListRs secretReservationKeyList) {

		final boolean emptyList = secretReservationKeyList == null ||
				secretReservationKeyList.reservations == null ||
				secretReservationKeyList.reservations.size() == 0;

		if (emptyList) {
			return Response.status(Status.BAD_REQUEST).entity("No secret reservation keys were given.").build();
		}

		final SecretReservationKey secretReservationKey = secretReservationKeyList.reservations.get(0);

		try {

			final Reservation reservation = reservationManager.getReservation(secretReservationKey.getKey());

			URI location = UriBuilder
					.fromUri(uriInfo.getRequestUri())
					.path("{secretReservationKeyBase64}")
					.build(encode(reservation.getKey()));

			return Response.ok(location.toString()).location(location).build();

		} catch (ReservationUnknownException e) {
			return Response
					.status(Status.NOT_FOUND)
					.entity("No reservation with the given secret reservation keys could be found!")
					.build();
		}
	}

	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/nodeUrns")
	public NodeUrnList getNodeUrns(@PathParam("secretReservationKeyBase64") final String secretReservationKeyBase64)
			throws Base64Exception {
		return new NodeUrnList(
				newArrayList(
						transform(
								getReservationOrThrow(secretReservationKeyBase64).getNodeUrns(),
								NODE_URN_TO_STRING
						)
				)
		);
	}

	/**
	 * <code>
	 * {
	 * [
	 * {"nodeUrns" : ["urn:...:0x1234", "urn:...:0x2345", ...], "image" : base64-string },
	 * {"nodeUrns" : ["urn:...:0x1234", "urn:...:0x2345", ...], "image" : base64-string }
	 * ]
	 * }
	 * </code>
	 *
	 * @param secretReservationKeyBase64
	 * 		the base64-encoded URL of the experiment
	 * @param flashData
	 * 		the data to flash onto the nodes
	 *
	 * @return a response
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/flash")
	public Response flashPrograms(@PathParam("secretReservationKeyBase64") final String secretReservationKeyBase64,
								  final FlashProgramsRequest flashData) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);

		long flashResponseTrackersId = RANDOM.nextLong();
		synchronized (flashResponseTrackers) {
			while (flashResponseTrackers.containsKey(flashResponseTrackersId)) {
				flashResponseTrackersId = RANDOM.nextLong();
			}
			flashResponseTrackers.put(flashResponseTrackersId, Lists.<Long>newArrayList());
		}

		for (FlashProgramsRequest.FlashTask flashTask : flashData.configurations) {

			final long requestId = requestIdProvider.get();
			final Request request = newFlashImagesRequest(
					reservation.getKey(),
					requestId,
					transform(flashTask.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN),
					extractByteArrayFromDataURL(flashTask.image)
			);

			synchronized (flashResponseTrackers) {
				flashResponseTrackers.get(flashResponseTrackersId).add(requestId);
			}

			reservation.createResponseTracker(request);
			reservation.getEventBus().post(request);
		}

		// remember response trackers, make them available via URL, redirect callers to this URL
		URI location = UriBuilder
				.fromUri(uriInfo.getRequestUri())
				.path("{flashResponseTrackersIdBase64}")
				.build(encode(Long.toString(flashResponseTrackersId)));

		return Response.ok(location.toString()).location(location).build();
	}

	/**
	 * Response looks like: <code>
	 * {
	 * "status" :
	 * [
	 * "urn:wisebed:...." : 100,
	 * "urn:wisebed:...." : -1,
	 * ]
	 * }
	 * </code>
	 *
	 * @param secretReservationKeyBase64
	 * 		the base64-encoded URL of the experiment
	 * @param flashResponseTrackersIdBase64
	 * 		the base64-encoded requestId of the flash operation
	 *
	 * @return the current state of the flash operation
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/flash/{flashResponseTrackersIdBase64}")
	public Response flashProgramsStatus(
			@PathParam("secretReservationKeyBase64") final String secretReservationKeyBase64,
			@PathParam("flashResponseTrackersIdBase64") final String flashResponseTrackersIdBase64)
			throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Long flashResponseTrackersId = Long.parseLong(decode(flashResponseTrackersIdBase64));

		if (!flashResponseTrackers.containsKey(flashResponseTrackersId)) {
			return Response
					.status(Status.NOT_FOUND)
					.entity("No flash job with request ID " + flashResponseTrackersId + " found!")
					.build();
		}

		final List<Long> requestIds = flashResponseTrackers.get(flashResponseTrackersId);
		return Response.ok(buildOperationStatusMap(reservation, requestIds)).build();
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/resetNodes")
	public Response resetNodes(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
							   NodeUrnList nodeUrnList) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newResetNodesRequest(reservation.getKey(), requestIdProvider.get(), nodeUrns);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/getChannelPipelines")
	public List<ChannelPipelinesMap> getChannelPipelines(
			@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
			NodeUrnList nodeUrnList) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final ReservationEventBus reservationEventBus = reservation.getEventBus();
		final String reservationId = reservation.getKey();
		final long requestId = requestIdProvider.get();

		return RequestHelper.getChannelPipelines(nodeUrns, reservationId, requestId, reservationEventBus);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("areNodesConnected")
	public Response areNodesConnected(NodeUrnList nodeUrnList) {

		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newAreNodesConnectedRequest(null, requestIdProvider.get(), nodeUrns);

		return sendRequestAndGetOperationStatusMap(request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/areNodesAlive")
	public Response areNodesAlive(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
								  NodeUrnList nodeUrnList) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newAreNodesAliveRequest(reservation.getKey(), requestIdProvider.get(), nodeUrns);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/send")
	public Response send(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
						 SendMessageData data) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Iterable<NodeUrn> nodeUrns = transform(data.targetNodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newSendDownstreamMessageRequest(
				reservation.getKey(),
				requestIdProvider.get(),
				nodeUrns,
				decodeBytes(data.bytesBase64)
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/destroyVirtualLink")
	public Response destroyVirtualLink(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
									   TwoNodeUrns nodeUrns) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Multimap<NodeUrn, NodeUrn> links = HashMultimap.create();
		links.put(new NodeUrn(nodeUrns.from), new NodeUrn(nodeUrns.to));
		final Request request = newDisableVirtualLinksRequest(
				reservation.getKey(),
				requestIdProvider.get(),
				links
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/disableNode")
	public Response disableNode(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
								String nodeUrn) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Request request = newDisableNodesRequest(
				reservation.getKey(),
				requestIdProvider.get(),
				newArrayList(new NodeUrn(nodeUrn))
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/enableNode")
	public Response enableNode(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
							   String nodeUrn) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Request request = newEnableNodesRequest(
				reservation.getKey(),
				requestIdProvider.get(),
				newArrayList(new NodeUrn(nodeUrn))
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/disablePhysicalLink")
	public Response disablePhysicalLink(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
										TwoNodeUrns nodeUrns) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Multimap<NodeUrn, NodeUrn> links = HashMultimap.create();
		links.put(new NodeUrn(nodeUrns.from), new NodeUrn(nodeUrns.to));
		final Request request = newDisablePhysicalLinksRequest(
				reservation.getKey(),
				requestIdProvider.get(),
				links
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/enablePhysicalLink")
	public Response enablePhysicalLink(@PathParam("secretReservationKeyBase64") String secretReservationKeyBase64,
									   TwoNodeUrns nodeUrns) throws Base64Exception {

		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		final Multimap<NodeUrn, NodeUrn> links = HashMultimap.create();
		links.put(new NodeUrn(nodeUrns.from), new NodeUrn(nodeUrns.to));
		final Request request = newEnablePhysicalLinksRequest(
				reservation.getKey(),
				requestIdProvider.get(),
				links
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, 10, TimeUnit.SECONDS);
	}

	private Wiseml getWiseml(final String secretReservationKeyBase64) throws Base64Exception {
		final Reservation reservation = getReservationOrThrow(secretReservationKeyBase64);
		return convertToWiseML(deviceDBService.getConfigsByNodeUrns(reservation.getNodeUrns()).values());
	}

	private Reservation getReservationOrThrow(final String secretReservationKeyBase64) throws Base64Exception {
		final String secretReservationKey = decode(secretReservationKeyBase64);
		try {
			return reservationManager.getReservation(secretReservationKey);
		} catch (ReservationUnknownException e) {
			throw new UnknownSecretReservationKeyException(secretReservationKey);
		}
	}

	private byte[] extractByteArrayFromDataURL(String dataURL) {
		// data:[<mediatype>][;base64]
		int commaPos = dataURL.indexOf(',');
		String header = dataURL.substring(0, commaPos);
		if (!header.endsWith("base64")) {
			throw new RuntimeException("Data URLs are only supported with base64 encoding!");
		}
		final char[] chars = dataURL.toCharArray();
		final int offset = commaPos + 1;
		final int length = chars.length - offset;
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length);
		try {
			Base64Utility.decode(chars, offset, length, byteArrayOutputStream);
		} catch (Base64Exception e) {
			throw propagate(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	private OperationStatusMap buildOperationStatusMap(final Reservation reservation, final Long requestId) {
		return buildOperationStatusMap(reservation, newArrayList(requestId));
	}

	private OperationStatusMap buildOperationStatusMap(final Reservation reservation, final List<Long> requestIds) {
		final Map<Long, ResponseTracker> map = newHashMap();
		for (Long requestId : requestIds) {
			map.put(requestId, reservation.getResponseTracker(requestId));
		}
		return buildOperationStatusMap(map);
	}

	private OperationStatusMap buildOperationStatusMap(final Map<Long, ResponseTracker> requestIdToResponseTrackerMap) {

		final OperationStatusMap operationStatusMap = new OperationStatusMap();
		operationStatusMap.operationStatus = new HashMap<String, JobNodeStatus>();

		for (long requestId : requestIdToResponseTrackerMap.keySet()) {

			JobNodeStatus status;
			final ResponseTracker responseTracker = requestIdToResponseTrackerMap.get(requestId);

			for (NodeUrn nodeUrn : responseTracker.keySet()) {

				if (responseTracker.get(nodeUrn).isDone()) {
					try {
						final SingleNodeResponse response = responseTracker.get(nodeUrn).get();
						status = new JobNodeStatus(
								response.getStatusCode() == getUnconnectedStatusCode(responseTracker.getRequest()) ?
										JobState.FAILED : JobState.SUCCESS,
								response.getStatusCode(),
								response.getErrorMessage()
						);
					} catch (Exception e) {
						status = new JobNodeStatus(JobState.FAILED, -2, e.getMessage());
					}
				} else {
					status = new JobNodeStatus(
							JobState.RUNNING,
							(int) (responseTracker.get(nodeUrn).getProgress() * 100),
							null
					);
				}

				operationStatusMap.operationStatus.put(nodeUrn.toString(), status);
			}
		}

		return operationStatusMap;
	}

	private Response sendRequestAndGetOperationStatusMap(final Request request, final int timeout,
														 final TimeUnit timeUnit) {

		final ResponseTracker responseTracker = responseTrackerFactory.create(request, portalEventBus);
		portalEventBus.post(request);

		final Map<Long, ResponseTracker> map = newHashMap();
		map.put(request.getRequestId(), responseTracker);

		try {
			responseTracker.get(timeout, timeUnit);
		} catch (TimeoutException e) {
			return Response.ok(buildOperationStatusMap(map)).build();
		} catch (Exception e) {
			throw propagate(e);
		}

		return Response.ok(buildOperationStatusMap(map)).build();
	}

	private Response sendRequestAndGetOperationStatusMap(final Reservation reservation,
														 final Request request,
														 final int timeout,
														 final TimeUnit timeUnit) {

		final ResponseTracker responseTracker = reservation.createResponseTracker(request);
		reservation.getEventBus().post(request);

		try {
			responseTracker.get(timeout, timeUnit);
		} catch (TimeoutException e) {
			return Response.ok(buildOperationStatusMap(reservation, request.getRequestId())).build();
		} catch (Exception e) {
			throw propagate(e);
		}

		return Response.ok(buildOperationStatusMap(reservation, request.getRequestId())).build();
	}

}
