package de.uniluebeck.itm.tr.iwsn.portal;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uniluebeck.itm.tr.iwsn.messages.*;
import eu.wisebed.api.v3.common.NodeUrn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static de.uniluebeck.itm.tr.iwsn.common.NodeUrnHelper.STRING_TO_NODE_URN;
import static de.uniluebeck.itm.tr.iwsn.messages.MessagesHelper.newDevicesAttachedEvent;
import static de.uniluebeck.itm.tr.iwsn.messages.MessagesHelper.newDevicesDetachedEvent;
import static de.uniluebeck.itm.tr.iwsn.messages.RequestHelper.extractNodeUrns;

public class ReservationEventBusImpl extends AbstractService implements ReservationEventBus {

	private static final Logger log = LoggerFactory.getLogger(ReservationEventBus.class);

	private final PortalEventBus portalEventBus;

	private final EventBus eventBus;

	private final Reservation reservation;

	@Inject
	public ReservationEventBusImpl(final PortalEventBus portalEventBus,
								   final EventBus eventBus,
								   @Assisted final Reservation reservation) {
		this.portalEventBus = portalEventBus;
		this.eventBus = eventBus;
		this.reservation = reservation;
	}

	@Override
	public void register(final Object object) {
		log.trace("ReservationEventBusImpl.register(object={})", object);
		eventBus.register(object);
	}

	@Override
	public void unregister(final Object object) {
		log.trace("ReservationEventBusImpl.unregister(object={}", object);
		eventBus.unregister(object);
	}

	@Override
	public void post(final Object event) {

		log.trace("ReservationEventBusImpl.post(event={})", event);
		checkState(isRunning());

		if (event instanceof Request) {
			assertNodesArePartOfReservation(extractNodeUrns((Request) event));
			portalEventBus.post(event);
		}
	}

	private void assertNodesArePartOfReservation(final Set<NodeUrn> nodeUrns) {
		if (!reservation.getNodeUrns().containsAll(nodeUrns)) {
			final Set<NodeUrn> unreservedNodeUrns = Sets.filter(nodeUrns, not(in(reservation.getNodeUrns())));
			throw new IllegalArgumentException("The node URNs [" + Joiner.on(",").join(unreservedNodeUrns) + "] "
					+ "are not part of the reservation.");
		}
	}

	@Subscribe
	public void onDevicesAttachedEventFromPortalEventBus(final DevicesAttachedEvent event) {

		final Set<NodeUrn> eventNodeUrns = newHashSet(transform(event.getNodeUrnsList(), STRING_TO_NODE_URN));
		final Set<NodeUrn> reservedNodeUrnsOfEvent = Sets.filter(eventNodeUrns, in(reservation.getNodeUrns()));

		if (!reservedNodeUrnsOfEvent.isEmpty()) {
			eventBus.post(newDevicesAttachedEvent(event.getTimestamp(), reservedNodeUrnsOfEvent));
		}
	}

	@Subscribe
	public void onUpstreamMessageEventFromPortalEventBus(final UpstreamMessageEvent event) {

		final NodeUrn sourceNodeUrn = new NodeUrn(event.getSourceNodeUrn());

		if (reservation.getNodeUrns().contains(sourceNodeUrn)) {
			eventBus.post(event);
		}
	}

	@Subscribe
	public void onDevicesDetachedEventFromPortalEventBus(final DevicesDetachedEvent event) {

		final Set<NodeUrn> eventNodeUrns = newHashSet(transform(event.getNodeUrnsList(), STRING_TO_NODE_URN));
		final Set<NodeUrn> reservedNodeUrnsOfEvent = Sets.filter(eventNodeUrns, in(reservation.getNodeUrns()));

		if (!reservedNodeUrnsOfEvent.isEmpty()) {
			eventBus.post(newDevicesDetachedEvent(event.getTimestamp(), reservedNodeUrnsOfEvent));
		}
	}

	@Subscribe
	public void onNotificationEventFromPortalEventBus(final NotificationEvent event) {
		if (!event.hasNodeUrn() || reservation.getNodeUrns().contains(new NodeUrn(event.getNodeUrn()))) {
			eventBus.post(event);
		}
	}

	@Subscribe
	public void onSingleNodeProgressFromPortalEventBus(final SingleNodeProgress progress) {
		if (reservation.getNodeUrns().contains(new NodeUrn(progress.getNodeUrn()))) {
			eventBus.post(progress);
		}
	}

	@Subscribe
	public void onSingleNodeResponseFromPortalEventBus(final SingleNodeResponse response) {
		if (reservation.getNodeUrns().contains(new NodeUrn(response.getNodeUrn()))) {
			eventBus.post(response);
		}
	}

	@Subscribe
	public void onReservationStartedEventFromPortalEventBus(final ReservationStartedEvent event) {
		if (event.getReservation() == reservation) {
			eventBus.post(event);
		}
	}

	@Subscribe
	public void onReservationEndedEventFromPortalEventBus(final ReservationEndedEvent event) {
		if (event.getReservation() == reservation) {
			eventBus.post(event);
		}
	}

	@Override
	protected void doStart() {
		try {
			portalEventBus.register(this);
			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {
			portalEventBus.unregister(this);
			notifyStopped();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}
}
