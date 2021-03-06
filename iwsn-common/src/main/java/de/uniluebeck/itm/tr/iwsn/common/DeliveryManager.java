package de.uniluebeck.itm.tr.iwsn.common;

import com.google.common.util.concurrent.Service;
import eu.wisebed.api.v3.common.Message;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.controller.Controller;
import eu.wisebed.api.v3.controller.Notification;
import eu.wisebed.api.v3.controller.RequestStatus;
import org.joda.time.DateTime;

public interface DeliveryManager extends Service {

	void addController(final String endpointUri, Controller controller);

	void removeController(final String endpointUri);

	/**
	 * Asynchronously notifies all currently registered controllers that the experiment has started.
	 */
	void reservationStarted(DateTime timestamp);

	/**
	 * Asynchronously notifies the controller with the endpoint URL {@code controllerEndpointUrl} that the experiment has
	 * started.
	 */
	void reservationStarted(DateTime timestamp, String controllerEndpointUri);

	/**
	 * Asynchronously notifies all currently registered controllers that the experiment has ended.
	 */
	void reservationEnded(DateTime timestamp);

	/**
	 * Asynchronously notifies the controller with the endpoint URL {@code controllerEndpointUrl} that the experiment has
	 * ended.
	 */
	void reservationEnded(DateTime timestamp, String controllerEndpointUri);

	void nodesAttached(DateTime timestamp, Iterable<NodeUrn> nodeUrns);

	void nodesDetached(DateTime timestamp, Iterable<NodeUrn> nodeUrns);

	void receive(Message... messages);

	void receive(Iterable<Message> messages);

	void receiveNotification(Notification... notifications);

	void receiveNotification(Iterable<Notification> notifications);

	void receiveStatus(RequestStatus... statuses);

	void receiveStatus(Iterable<RequestStatus> statuses);

	void receiveFailureStatusMessages(Iterable<NodeUrn> nodeUrns, long requestId, Exception e, int statusValue);

	void receiveUnknownNodeUrnRequestStatus(Iterable<NodeUrn> nodeUrns, String msg, long requestId);
}
