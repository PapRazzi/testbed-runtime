package de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.resources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import de.uniluebeck.itm.tr.common.IdProvider;
import de.uniluebeck.itm.tr.common.NodeUrnHelper;
import de.uniluebeck.itm.tr.common.WisemlProvider;
import de.uniluebeck.itm.tr.devicedb.CachedDeviceDBService;
import de.uniluebeck.itm.tr.devicedb.DeviceConfig;
import de.uniluebeck.itm.tr.iwsn.common.ResponseTracker;
import de.uniluebeck.itm.tr.iwsn.common.ResponseTrackerFactory;
import de.uniluebeck.itm.tr.iwsn.messages.Request;
import de.uniluebeck.itm.tr.iwsn.messages.SingleNodeResponse;
import de.uniluebeck.itm.tr.iwsn.portal.*;
import de.uniluebeck.itm.tr.iwsn.portal.api.RequestHelper;
import de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.dto.*;
import de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.exceptions.ReservationNotRunningException;
import de.uniluebeck.itm.tr.iwsn.portal.api.rest.v1.exceptions.UnknownSecretReservationKeysException;
import de.uniluebeck.itm.tr.iwsn.portal.api.soap.v3.Converters;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.wsn.ChannelPipelinesMap;
import eu.wisebed.wiseml.Capability;
import eu.wisebed.wiseml.Setup.Node;
import eu.wisebed.wiseml.Wiseml;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static de.uniluebeck.itm.tr.common.Base64Helper.*;
import static de.uniluebeck.itm.tr.common.NodeUrnHelper.NODE_URN_TO_STRING;
import static de.uniluebeck.itm.tr.common.json.JSONHelper.fromJSON;
import static de.uniluebeck.itm.tr.common.json.JSONHelper.toJSON;
import static de.uniluebeck.itm.tr.iwsn.messages.MessagesHelper.*;

@Path("/experiments/")
public class ExperimentResourceImpl implements ExperimentResource {

	private static final Logger log = LoggerFactory.getLogger(ExperimentResourceImpl.class);

	private final CachedDeviceDBService cachedDeviceDBService;

	private enum Operation {
		RESET,
		ALIVE,
		CONNECTED,
		FLASH,
		SEND,
		SET_CHANNEL_PIPELINE,
		NODE_API
	}

	private static final Map<Operation, Duration> DEFAULT_TIMEOUTS_MS = newHashMap();

	static {
		DEFAULT_TIMEOUTS_MS.put(Operation.RESET, Duration.standardSeconds(10));
		DEFAULT_TIMEOUTS_MS.put(Operation.ALIVE, Duration.standardSeconds(10));
		DEFAULT_TIMEOUTS_MS.put(Operation.CONNECTED, Duration.standardSeconds(3));
		DEFAULT_TIMEOUTS_MS.put(Operation.FLASH, Duration.standardMinutes(3));
		DEFAULT_TIMEOUTS_MS.put(Operation.NODE_API, Duration.standardSeconds(10));
		DEFAULT_TIMEOUTS_MS.put(Operation.SEND, Duration.standardSeconds(10));
		DEFAULT_TIMEOUTS_MS.put(Operation.SET_CHANNEL_PIPELINE, Duration.standardSeconds(10));
	}

	private final WisemlProvider wisemlProvider;

	private final ReservationManager reservationManager;

	private final IdProvider requestIdProvider;

	private final PortalEventBus portalEventBus;

	private final ResponseTrackerFactory responseTrackerFactory;

	@Context
	private UriInfo uriInfo;

	@Inject
	public ExperimentResourceImpl(final WisemlProvider wisemlProvider,
								  final PortalEventBus portalEventBus,
								  final ResponseTrackerFactory responseTrackerFactory,
								  final ReservationManager reservationManager,
								  final IdProvider requestIdProvider,
								  final CachedDeviceDBService cachedDeviceDBService) {
		this.wisemlProvider = checkNotNull(wisemlProvider);
		this.portalEventBus = checkNotNull(portalEventBus);
		this.responseTrackerFactory = checkNotNull(responseTrackerFactory);
		this.reservationManager = checkNotNull(reservationManager);
		this.requestIdProvider = checkNotNull(requestIdProvider);
		this.cachedDeviceDBService = checkNotNull(cachedDeviceDBService);
	}

	@Override
	@GET
	@Path("network")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Wiseml getNetwork() {
		log.trace("ExperimentResourceImpl.getNetwork()");
		return wisemlProvider.get();
	}

	@Override
	@GET
	@Path("network.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Wiseml getNetworkAsJson() {
		log.trace("ExperimentResourceImpl.getNetworkAsJson()");
		return getNetwork();
	}

	@Override
	@GET
	@Path("network.xml")
	@Produces(MediaType.APPLICATION_XML)
	public Wiseml getNetworkAsXml() {
		log.trace("ExperimentResourceImpl.getNetworkAsXml()");
		return getNetwork();
	}

	@Override
	@GET
	@Path("{secretReservationKeysBase64}/network")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Wiseml getExperimentNetwork(
			@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64) throws Exception {
		log.trace("ExperimentResourceImpl.getExperimentNetwork({})", secretReservationKeysBase64);
		return filterWisemlForReservedNodes(secretReservationKeysBase64);
	}

	@Override
	@GET
	@Path("{secretReservationKeyBase64}/network.json")
	@Produces(MediaType.APPLICATION_JSON)
	public Wiseml getExperimentNetworkAsJson(
			@PathParam("secretReservationKeyBase64") final String secretReservationKeyBase64)
			throws Exception {
		log.trace("ExperimentResourceImpl.getExperimentNetworkAsJson({})", secretReservationKeyBase64);
		return filterWisemlForReservedNodes(secretReservationKeyBase64);
	}

	@Override
	@GET
	@Path("{secretReservationKeyBase64}/network.xml")
	@Produces(MediaType.APPLICATION_XML)
	public Wiseml getExperimentNetworkAsXml(
			@PathParam("secretReservationKeyBase64") final String secretReservationKeyBase64)
			throws Exception {
		log.trace("ExperimentResourceImpl.getExperimentNetworkAsXml({})", secretReservationKeyBase64);
		return filterWisemlForReservedNodes(secretReservationKeyBase64);
	}

	@Override
	@GET
	@Path("nodes")
	@Produces({MediaType.APPLICATION_JSON})
	public NodeUrnList getNodes(@QueryParam("filter") final String filter,
								@QueryParam("capability") final String capability) {

		log.trace("ExperimentResourceImpl.getNodes({}, {})", filter, capability);
		final Wiseml wiseml = wisemlProvider.get();

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

		//noinspection ConstantConditions
		if (c.getDatatype() != null && c.getDatatype() != null) {
			sb.append(c.getDatatype()).append(" ");
		}

		if (c.getDefault() != null) {
			sb.append(c.getDefault()).append(" ");
		}

		//noinspection ConstantConditions
		if (c.getUnit() != null && c.getUnit() != null) {
			sb.append(c.getUnit()).append(" ");
		}

		return sb.toString();
	}

	@Override
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/nodeUrns")
	public NodeUrnList getNodeUrns(@PathParam("secretReservationKeysBase64") final String secretReservationKeysBase64)
			throws Exception {
		log.trace("ExperimentResourceImpl.getNodeUrns({})", secretReservationKeysBase64);
		return new NodeUrnList(
				newArrayList(
						transform(
								getReservationOrThrow(secretReservationKeysBase64).getNodeUrns(),
								NODE_URN_TO_STRING
						)
				)
		);
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/flash")
	public Response flashPrograms(@PathParam("secretReservationKeysBase64") final String secretReservationKeysBase64,
								  final FlashProgramsRequest flashData) throws Exception {

		log.trace("ExperimentResourceImpl.flashPrograms({}, {})", secretReservationKeysBase64, flashData);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final List<Long> requestIds = newArrayList();

		for (FlashProgramsRequest.FlashTask flashTask : flashData.configurations) {

			final long requestId = requestIdProvider.get();
			final Request request = newFlashImagesRequest(
					secretReservationKeysBase64,
					requestId,
					transform(flashTask.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN),
					extractByteArrayFromDataURL(flashTask.image)
			);

			requestIds.add(requestId);

			// just create ResponseTracker, we can retrieve it using the reservation later
			reservation.createResponseTracker(request);
			reservation.getEventBus().post(request);
		}

		// remember response trackers, make them available via URL, redirect callers to this URL
		URI location = UriBuilder
				.fromUri(uriInfo.getRequestUri())
				.path("{requestIdsJSONListBase64}")
				.build(encode(toJSON(requestIds)));

		return Response.ok(location.toString()).location(location).build();
	}

	@Override
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/flash/{requestIdsJSONListBase64}")
	public Response flashProgramsStatus(
			@PathParam("secretReservationKeysBase64") final String secretReservationKeysBase64,
			@PathParam("requestIdsJSONListBase64") final String requestIdsJSONListBase64)
			throws Exception {

		log.trace("ExperimentResourceImpl.flashProgramsStatus({}, {})",
				secretReservationKeysBase64,
				requestIdsJSONListBase64
		);
		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final List<Long> requestIds = fromJSON(decode(requestIdsJSONListBase64), new TypeReference<List<Long>>() {
				}
		);
		return Response.ok(buildOperationStatusMap(reservation, requestIds)).build();
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/resetNodes")
	public Response resetNodes(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
							   NodeUrnList nodeUrnList) throws Exception {

		log.trace("ExperimentResourceImpl.resetNodes({}, {})", secretReservationKeysBase64, nodeUrnList);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newResetNodesRequest(secretReservationKeysBase64, requestIdProvider.get(), nodeUrns);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.RESET, nodeUrns));
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeyBase64}/setChannelPipelines")
	public Response setChannelPipelines(
			@PathParam("secretReservationKeyBase64") String secretReservationKeysBase64,
			ChannelHandlerConfigurationList chcl) throws Exception {

		log.trace("ExperimentResourceImpl.setChannelPipelines({}, {})", secretReservationKeysBase64, chcl);

		if (chcl.nodeUrns == null || chcl.nodeUrns.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Empty node URN list").build();
		}

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final Iterable<NodeUrn> nodeUrns = transform(chcl.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);

		final Request request = newSetChannelPipelinesRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				nodeUrns,
				Converters.convertCHCs(chcl.handlers)
		);

		return sendRequestAndGetOperationStatusMap(
				reservation,
				request,
				retrieveTimeout(Operation.SET_CHANNEL_PIPELINE, nodeUrns)
		);
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/getChannelPipelines")
	public List<ChannelPipelinesMap> getChannelPipelines(
			@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
			NodeUrnList nodeUrnList) throws Exception {

		log.trace("ExperimentResourceImpl.getChannelPipelines({}, {})", secretReservationKeysBase64, nodeUrnList);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final ReservationEventBus reservationEventBus = reservation.getEventBus();
		final long requestId = requestIdProvider.get();

		return RequestHelper.getChannelPipelines(nodeUrns, secretReservationKeysBase64, requestId, reservationEventBus);
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("areNodesConnected")
	public Response areNodesConnected(NodeUrnList nodeUrnList) {

		log.trace("ExperimentResourceImpl.areNodesConnected({})", nodeUrnList);

		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newAreNodesConnectedRequest(null, requestIdProvider.get(), nodeUrns);

		return sendRequestAndGetOperationStatusMap(request, retrieveTimeout(Operation.CONNECTED, nodeUrns));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/areNodesAlive")
	public Response areNodesAlive(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
								  NodeUrnList nodeUrnList) throws Exception {

		log.trace("ExperimentResourceImpl.areNodesAlive({}, {})", secretReservationKeysBase64, nodeUrnList);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final Iterable<NodeUrn> nodeUrns = transform(nodeUrnList.nodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newAreNodesAliveRequest(secretReservationKeysBase64, requestIdProvider.get(), nodeUrns);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.ALIVE, nodeUrns));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/send")
	public Response send(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
						 SendMessageData data) throws Exception {

		log.trace("ExperimentResourceImpl.send({}, {})", secretReservationKeysBase64, data);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final Iterable<NodeUrn> nodeUrns = transform(data.targetNodeUrns, NodeUrnHelper.STRING_TO_NODE_URN);
		final Request request = newSendDownstreamMessageRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				nodeUrns,
				decodeBytes(data.bytesBase64)
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.SEND, nodeUrns));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/destroyVirtualLink")
	public Response destroyVirtualLink(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
									   TwoNodeUrns link) throws Exception {

		log.trace("ExperimentResourceImpl.destroyVirtualLink({}, {})", secretReservationKeysBase64, link);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);

		final NodeUrn from = new NodeUrn(link.from);
		final NodeUrn to = new NodeUrn(link.to);
		final Multimap<NodeUrn, NodeUrn> links = HashMultimap.create();
		links.put(from, to);

		final Request request = newDisableVirtualLinksRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				links
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.NODE_API, from));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/disableNode")
	public Response disableNode(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
								String nodeUrnString) throws Exception {

		log.trace("ExperimentResourceImpl.disableNode({}, {})", secretReservationKeysBase64, nodeUrnString);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final NodeUrn nodeUrn = new NodeUrn(nodeUrnString);
		final Request request = newDisableNodesRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				newArrayList(nodeUrn)
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.NODE_API, nodeUrn));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/enableNode")
	public Response enableNode(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
							   String nodeUrnString) throws Exception {

		log.trace("ExperimentResourceImpl.enableNode({}, {})", secretReservationKeysBase64, nodeUrnString);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);
		final NodeUrn nodeUrn = new NodeUrn(nodeUrnString);
		final Request request = newEnableNodesRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				newArrayList(nodeUrn)
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.NODE_API, nodeUrn));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/disablePhysicalLink")
	public Response disablePhysicalLink(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
										TwoNodeUrns nodeUrns) throws Exception {

		log.trace("ExperimentResourceImpl.disablePhysicalLink({}, {})", secretReservationKeysBase64, nodeUrns);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);

		final Multimap<NodeUrn, NodeUrn> links = HashMultimap.create();
		final NodeUrn from = new NodeUrn(nodeUrns.from);
		final NodeUrn to = new NodeUrn(nodeUrns.to);
		links.put(from, to);

		final Request request = newDisablePhysicalLinksRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				links
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.NODE_API, from));
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("{secretReservationKeysBase64}/enablePhysicalLink")
	public Response enablePhysicalLink(@PathParam("secretReservationKeysBase64") String secretReservationKeysBase64,
									   TwoNodeUrns nodeUrns) throws Exception {

		log.trace("ExperimentResourceImpl.enablePhysicalLink({}, {})", secretReservationKeysBase64, nodeUrns);

		final Reservation reservation = getReservationOrThrow(secretReservationKeysBase64);

		final Multimap<NodeUrn, NodeUrn> links = HashMultimap.create();
		final NodeUrn from = new NodeUrn(nodeUrns.from);
		final NodeUrn to = new NodeUrn(nodeUrns.to);
		links.put(from, to);

		final Request request = newEnablePhysicalLinksRequest(
				secretReservationKeysBase64,
				requestIdProvider.get(),
				links
		);

		return sendRequestAndGetOperationStatusMap(reservation, request, retrieveTimeout(Operation.NODE_API, from));
	}

	private Wiseml filterWisemlForReservedNodes(final String secretReservationKeysBase64) throws Exception {
		return wisemlProvider.get(getReservationOrThrow(secretReservationKeysBase64).getNodeUrns());
	}

	private Reservation getReservationOrThrow(final String secretReservationKeysBase64) throws Exception {

		try {

			return reservationManager.getReservation(secretReservationKeysBase64);

		} catch (ReservationUnknownException e) {
			throw new UnknownSecretReservationKeysException(secretReservationKeysBase64);
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
						final Request request = responseTracker.getRequest();
						status = new JobNodeStatus(
								isErrorStatusCode(request, response) ? JobState.FAILED : JobState.SUCCESS,
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

	private Response sendRequestAndGetOperationStatusMap(final Request request, final Duration timeout) {

		final ResponseTracker responseTracker = responseTrackerFactory.create(request, portalEventBus);
		portalEventBus.post(request);

		final Map<Long, ResponseTracker> map = newHashMap();
		map.put(request.getRequestId(), responseTracker);

		try {
			responseTracker.get(timeout.getMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			return Response.ok(buildOperationStatusMap(map)).build();
		} catch (Exception e) {
			throw propagate(e);
		}

		return Response.ok(buildOperationStatusMap(map)).build();
	}

	private Response sendRequestAndGetOperationStatusMap(final Reservation reservation,
														 final Request request,
														 final Duration timeout)
			throws ReservationNotRunningException {

		final ResponseTracker responseTracker = reservation.createResponseTracker(request);

		if (!reservation.isRunning()) {
			throw new ReservationNotRunningException(reservation.getInterval());
		}

		reservation.getEventBus().post(request);

		try {
			responseTracker.get(timeout.getMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			return Response.ok(buildOperationStatusMap(reservation, request.getRequestId())).build();
		} catch (Exception e) {
			throw propagate(e);
		}

		return Response.ok(buildOperationStatusMap(reservation, request.getRequestId())).build();
	}

	private Duration retrieveTimeout(final Operation op, final Iterable<NodeUrn> nodeUrns) {

		if (op == Operation.SEND || op == Operation.SET_CHANNEL_PIPELINE) {
			return DEFAULT_TIMEOUTS_MS.get(op);
		}

		Duration longest = DEFAULT_TIMEOUTS_MS.get(op);

		for (DeviceConfig config : cachedDeviceDBService.getConfigsByNodeUrns(nodeUrns).values()) {

			long millis;
			switch (op) {
				case ALIVE:
					millis = config.getTimeoutCheckAliveMillis();
					break;
				case CONNECTED:
					millis = config.getTimeoutCheckAliveMillis();
					break;
				case FLASH:
					millis = config.getTimeoutFlashMillis();
					break;
				case RESET:
					millis = config.getTimeoutResetMillis();
					break;
				case NODE_API:
					millis = config.getTimeoutNodeApiMillis();
					break;
				default:
					throw new RuntimeException("Unhandled operation type. This shouldn't be possible!");
			}

			final Duration timeout = Duration.millis(millis);
			if (timeout.isLongerThan(longest)) {
				longest = timeout;
			}
		}

		if (log.isTraceEnabled()) {
			log.trace(
					"Retrieved timeout of {} ms for {} operation on node URNs {}",
					longest.getMillis(),
					op,
					nodeUrns
			);
		}

		return longest;
	}

	private Duration retrieveTimeout(final Operation op, final NodeUrn nodeUrn) {
		return retrieveTimeout(op, newArrayList(nodeUrn));
	}

}
