package de.uniluebeck.itm.tr.iwsn.portal.api.soap.v3;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uniluebeck.itm.nettyprotocols.HandlerFactory;
import de.uniluebeck.itm.tr.iwsn.common.*;
import de.uniluebeck.itm.tr.iwsn.devicedb.DeviceConfig;
import de.uniluebeck.itm.tr.iwsn.devicedb.DeviceConfigDB;
import de.uniluebeck.itm.tr.iwsn.messages.Request;
import de.uniluebeck.itm.tr.iwsn.portal.*;
import eu.wisebed.api.v3.common.KeyValuePair;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.NodeUrnPrefix;
import eu.wisebed.api.v3.common.SecretReservationKey;
import eu.wisebed.api.v3.sm.*;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static de.uniluebeck.itm.tr.iwsn.devicedb.WiseMLConverter.convertToWiseML;
import static de.uniluebeck.itm.tr.iwsn.messages.MessagesHelper.newAreNodesConnectedRequest;
import static eu.wisebed.wiseml.WiseMLHelper.serialize;

@WebService
public class SessionManagementImpl implements SessionManagement {

	private static final Function<DeviceConfig, NodeUrn> DEVICE_CONFIG_TO_NODE_URN_FUNCTION =
			new Function<DeviceConfig, NodeUrn>() {
				@Override
				public NodeUrn apply(final DeviceConfig input) {
					return input.getNodeUrn();
				}
			};

	private final DeliveryManager sessionManagementDeliveryManager;

	private final PortalEventBus portalEventBus;

	private final RequestIdProvider requestIdProvider;

	private final ResponseTrackerFactory responseTrackerFactory;

	private final PortalConfig portalConfig;

	private final Set<HandlerFactory> handlerFactories;

	private final DeviceConfigDB deviceConfigDB;

	private final ReservationManager reservationManager;

	private final SessionManagementPreconditions preconditions;

	private final WSNServiceFactory wsnServiceFactory;

	private final DeliveryManagerFactory deliveryManagerFactory;

	private final Map<Reservation, WSNService> wsnInstances = newHashMap();

	private final Map<Reservation, DeliveryManager> deliveryManagers = newHashMap();

	@Inject
	public SessionManagementImpl(final DeliveryManager sessionManagementDeliveryManager,
								 final PortalEventBus portalEventBus,
								 final RequestIdProvider requestIdProvider,
								 final ResponseTrackerFactory responseTrackerFactory,
								 final PortalConfig portalConfig,
								 final Set<HandlerFactory> handlerFactories,
								 final DeviceConfigDB deviceConfigDB,
								 final ReservationManager reservationManager,
								 final WSNServiceFactory wsnServiceFactory,
								 final DeliveryManagerFactory deliveryManagerFactory) {

		this.sessionManagementDeliveryManager = checkNotNull(sessionManagementDeliveryManager);
		this.portalEventBus = checkNotNull(portalEventBus);
		this.requestIdProvider = checkNotNull(requestIdProvider);
		this.responseTrackerFactory = checkNotNull(responseTrackerFactory);
		this.portalConfig = checkNotNull(portalConfig);
		this.handlerFactories = checkNotNull(handlerFactories);
		this.deviceConfigDB = checkNotNull(deviceConfigDB);
		this.reservationManager = checkNotNull(reservationManager);
		this.wsnServiceFactory = checkNotNull(wsnServiceFactory);
		this.deliveryManagerFactory = checkNotNull(deliveryManagerFactory);

		this.preconditions = new SessionManagementPreconditions();
		this.preconditions.addServedUrnPrefixes(portalConfig.urnPrefix);
		this.preconditions.addKnownNodeUrns(transform(deviceConfigDB.getAll(), DEVICE_CONFIG_TO_NODE_URN_FUNCTION));
	}

	@Override
	public void areNodesAlive(final long requestId, final List<NodeUrn> nodeUrns, final String controllerEndpointUrl) {

		sessionManagementDeliveryManager.addController(controllerEndpointUrl);

		final Request request = newAreNodesConnectedRequest(requestId, nodeUrns);
		final ResponseTracker responseTracker = responseTrackerFactory.create(request, portalEventBus);

		portalEventBus.post(request);

		responseTracker.addListener(new Runnable() {
			@Override
			public void run() {
				sessionManagementDeliveryManager.removeController(controllerEndpointUrl);
			}
		}, sameThreadExecutor()
		);
	}

	@Override
	public void getConfiguration(final Holder<String> rsEndpointUrl,
								 final Holder<String> snaaEndpointUrl,
								 final Holder<List<NodeUrnPrefix>> servedUrnPrefixes,
								 final Holder<List<KeyValuePair>> options) {

		rsEndpointUrl.value = portalConfig.rsEndpointUrl.toString();
		snaaEndpointUrl.value = portalConfig.snaaEndpointUrl.toString();
		servedUrnPrefixes.value = newArrayList(portalConfig.urnPrefix);

		final List<KeyValuePair> optionsList = Lists.newArrayList();
		for (String key : portalConfig.options.keySet()) {
			for (String value : portalConfig.options.get(key)) {
				final KeyValuePair keyValuePair = new KeyValuePair();
				keyValuePair.setKey(key);
				keyValuePair.setValue(value);
				optionsList.add(keyValuePair);
			}
		}
		options.value = optionsList;
	}

	@Override
	public String getInstance(final List<SecretReservationKey> secretReservationKeys)
			throws ExperimentNotRunningFault_Exception, UnknownReservationIdFault_Exception {

		preconditions.checkGetInstanceArguments(secretReservationKeys, true);

		final String secretReservationKey = secretReservationKeys.get(0).getSecretReservationKey();
		final Reservation reservation;

		try {

			reservation = reservationManager.getReservation(secretReservationKey);

		} catch (ReservationUnknownException e) {
			final String message = "Secret reservation key \"" + secretReservationKey + "\" is unknown!";
			final UnknownReservationIdFault fault = new UnknownReservationIdFault();
			fault.setMessage(message);
			fault.setReservationId(secretReservationKey);
			throw new UnknownReservationIdFault_Exception(message, fault, e);
		}

		return getOrCreateAndStartWSNServiceInstance(secretReservationKey, reservation).getURI().toString();
	}

	private WSNService getOrCreateAndStartWSNServiceInstance(final String secretReservationKey,
															 final Reservation reservation) {

		WSNService wsnService;
		DeliveryManager deliveryManager;

		synchronized (wsnInstances) {
			synchronized (deliveryManagers) {

				wsnService = wsnInstances.get(reservation);

				if (wsnService != null) {
					return wsnService;
				}

				deliveryManager = deliveryManagerFactory.create(reservation);
				deliveryManagers.put(reservation, deliveryManager);

				wsnService = wsnServiceFactory.create(secretReservationKey, reservation, deliveryManager);
				wsnInstances.put(reservation, wsnService);
			}
		}

		if (reservation.getInterval().getStart().isBeforeNow() && reservation.getInterval().getEnd().isAfterNow()) {
			wsnService.startAndWait();
		}

		return wsnService;
	}

	@Subscribe
	public void onReservationStarted(final ReservationStartedEvent event) {
		synchronized (wsnInstances) {
			final WSNService wsnService = wsnInstances.get(event.getReservation());
			if (wsnService != null && !wsnService.isRunning()) {
				wsnService.startAndWait();
			}
		}
	}

	@Subscribe
	public void onReservationEnded(final ReservationEndedEvent event) {
		synchronized (wsnInstances) {
			final WSNService wsnService = wsnInstances.get(event.getReservation());
			if (wsnService != null && wsnService.isRunning()) {
				wsnService.stopAndWait();
			}
		}
	}

	@Override
	public String getNetwork() {
		return serialize(convertToWiseML(deviceConfigDB.getAll()));
	}

	@Override
	public List<ChannelHandlerDescription> getSupportedChannelHandlers() {

		final List<ChannelHandlerDescription> list = newArrayList();

		for (HandlerFactory handlerFactory : handlerFactories) {

			final ChannelHandlerDescription channelHandlerDescription = new ChannelHandlerDescription();
			channelHandlerDescription.setName(handlerFactory.getName());
			channelHandlerDescription.setDescription(handlerFactory.getDescription());

			for (String key : handlerFactory.getConfigurationOptions().keySet()) {
				for (String value : handlerFactory.getConfigurationOptions().get(key)) {

					final KeyValuePair keyValuePair = new KeyValuePair();
					keyValuePair.setKey(key);
					keyValuePair.setValue(value);

					channelHandlerDescription.getConfigurationOptions().add(keyValuePair);
				}
			}
		}

		return list;
	}

	@Override
	public List<String> getSupportedVirtualLinkFilters() {
		return newArrayList();
	}
}