/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote*
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

package de.uniluebeck.itm.tr.rs.persistence.gcal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gdata.client.Query;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import de.uniluebeck.itm.tr.rs.RSServiceConfig;
import de.uniluebeck.itm.tr.rs.persistence.ConfidentialReservationDataComparator;
import de.uniluebeck.itm.tr.rs.persistence.RSPersistence;
import de.uniluebeck.itm.util.SecureIdGenerator;
import de.uniluebeck.itm.util.Tuple;
import eu.wisebed.api.v3.WisebedServiceHelper;
import eu.wisebed.api.v3.common.KeyValuePair;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.NodeUrnPrefix;
import eu.wisebed.api.v3.common.SecretReservationKey;
import eu.wisebed.api.v3.rs.ConfidentialReservationData;
import eu.wisebed.api.v3.rs.RSFault;
import eu.wisebed.api.v3.rs.RSFault_Exception;
import eu.wisebed.api.v3.rs.UnknownSecretReservationKeyFault;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

import static com.google.common.base.Throwables.propagate;
import static de.uniluebeck.itm.tr.rs.persistence.OffsetAmountHelper.limitResults;
import static java.util.Collections.sort;

public class GCalRSPersistence implements RSPersistence {

	private static final Logger log = LoggerFactory.getLogger(GCalRSPersistence.class);

	private final SecureIdGenerator secureIdGenerator = new SecureIdGenerator();

	// The base URL for a user's calendar meta feed (needs a username appended).
	private static final String META_FEED_URL_BASE = "http://www.google.com/calendar/feeds/";

	// The string to add to the user's meta feed URL to access the event feed for their primary calendar.
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";

	// The URL for the event feed of the specified user's primary calendar. (e.g.
	// http://www.google.com/feeds/calendar/jdoe@gmail.com/private/full)
	private URL eventFeedUrl = null;

	private CalendarService myService;

	@VisibleForTesting
	GCalRSPersistence(final String username, final String password) {

		myService = new CalendarService("GCal Persistence Service");

		try {
			eventFeedUrl = new URL(META_FEED_URL_BASE + username + EVENT_FEED_URL_SUFFIX);
			myService.setUserCredentials(username, password);
		} catch (Exception e) {
			throw propagate(e);
		}

		log.debug("New GCal Persistence instance for calendar @ " + eventFeedUrl);
	}

	@Inject
	public GCalRSPersistence(final RSServiceConfig rsServiceConfig) {
		this(rsServiceConfig.getRsGcalUsername(), rsServiceConfig.getRsGcalPassword());
	}

	@Override
	public ConfidentialReservationData addReservation(final List<NodeUrn> nodeUrns,
													  final org.joda.time.DateTime from,
													  final org.joda.time.DateTime to,
													  final String username,
													  final NodeUrnPrefix urnPrefix,
													  final String description,
													  final List<KeyValuePair> options) throws RSFault_Exception {
		try {

			final String secretReservationKeyString = secureIdGenerator.getNextId();

			final ConfidentialReservationData crd = new ConfidentialReservationData();
			crd.setFrom(from);
			crd.setTo(to);
			crd.getNodeUrns().addAll(nodeUrns);
			crd.setDescription(description);
			crd.getOptions().addAll(options);
			crd.setUsername(username);

			final SecretReservationKey secretReservationKey = new SecretReservationKey();
			secretReservationKey.setKey(secretReservationKeyString);
			secretReservationKey.setUrnPrefix(urnPrefix);

			crd.setSecretReservationKey(secretReservationKey);

			final ReservationData reservationData = new ReservationData(
					crd,
					urnPrefix.toString(),
					secretReservationKeyString
			);

			String reservationDataXml = marshall(reservationData);
			String eventTitle = crd.getNodeUrns().size() + " Node(s)";

			// Create a new calendar entry
			CalendarEventEntry myEntry = new CalendarEventEntry();
			{

				myEntry.setTitle(new PlainTextConstruct(eventTitle));
				myEntry.setContent(new PlainTextConstruct(reservationDataXml));
				myEntry.setQuickAdd(false);
				myEntry.setWebContent(null);

				GregorianCalendar gregorianCalendarFrom = crd.getFrom().toGregorianCalendar();
				DateTime gDataDateTimeFrom = new DateTime(
						gregorianCalendarFrom.getTime(),
						gregorianCalendarFrom.getTimeZone()
				);

				GregorianCalendar gregorianCalendarTo = crd.getTo().toGregorianCalendar();
				DateTime gDataDateTimeTo = new DateTime(
						gregorianCalendarTo.getTime(),
						gregorianCalendarTo.getTimeZone()
				);

				When eventTimes = new When();
				eventTimes.setStartTime(gDataDateTimeFrom);
				eventTimes.setEndTime(gDataDateTimeTo);
				myEntry.addTime(eventTimes);
			}

			// Persist it and check if the content was saved correctly (i.e., not truncated by Google)
			try {
				// Save the event to GCal
				CalendarEventEntry eventEntry = myService.insert(eventFeedUrl, myEntry);

				// Update the event and check if the stored text is not truncated, otherwise: fail
				{
					// Re-retrieve from GCal
					// TODO: Validate that this works!!!
					CalendarEventEntry updatedEntry = eventEntry.getSelf();
					String content = updatedEntry.getPlainTextContent();

					if (!reservationDataXml.equals(marshall(unmarshall(content)))) {
						log.error("Unmarshalled contents do not match (maybe text was truncated)");
						log.debug("---- Original : " + reservationDataXml);
						log.debug("---- Text@Gcal: " + content);
						throw new Exception("Unmarshalled contents do not match (maybe text was truncated)");
					}
				}

			} catch (Exception e) {
				log.error("Error: " + e, e);
				throw createRSFault("Internal Server Error");
			}

			return crd;

		} catch (JAXBException e) {
			log.error("Error: " + e, e);
			throw createRSFault("Internal Server Error!");
		}
	}

	@Override
	public ConfidentialReservationData deleteReservation(SecretReservationKey secretReservationKey)
			throws UnknownSecretReservationKeyFault, RSFault_Exception {

		Tuple<Entry, ReservationData> reservation = getReservationInternal(secretReservationKey);
		ConfidentialReservationData confidentialReservationData = reservation.getSecond().getReservation();
		Entry gcalEntry = reservation.getFirst();

		try {
			gcalEntry.delete();
			if (log.isDebugEnabled()) {
				log.debug("Deleted entry[" + marshall(reservation.getSecond()) + "] for key: " + secretReservationKey);
			}
		} catch (Exception e) {
			log.error("Exception: " + e, e);
			throw createRSFault("Internal Server Error");
		}

		return confidentialReservationData;
	}

	@Override
	public ConfidentialReservationData getReservation(SecretReservationKey secretReservationKey)
			throws UnknownSecretReservationKeyFault, RSFault_Exception {
		return getReservationInternal(secretReservationKey).getSecond().getReservation();
	}


	@Override
	public List<ConfidentialReservationData> getReservations(Interval interval,
															 @Nullable final Integer offset,
															 @Nullable final Integer amount) throws RSFault_Exception {

		boolean fetchedAll = false;

		List<ConfidentialReservationData> matchedReservations = Lists.newLinkedList();
		int lastCount = 0;

		while (!fetchedAll) {

			fetchReservations(matchedReservations, interval, 20);

			if (matchedReservations.size() - lastCount < 20) {
				log.debug("Fetched all matchedReservations.");
				fetchedAll = true;
			} else {
				lastCount = matchedReservations.size();
				log.debug("Fetched {} in total.", lastCount);
				interval = new Interval(
						interval.getStart(),
						matchedReservations.get(0).getFrom()
				);
			}

		}

		sort(matchedReservations, new ConfidentialReservationDataComparator());

		return limitResults(matchedReservations, offset, amount);

	}

	private void fetchReservations(final List<ConfidentialReservationData> reservations,
								   final Interval interval,
								   final int maxResults) throws RSFault_Exception {

		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);

		// because of different time span logic of calendar api we need to shift the time span to match interval
		long shiftStartMillis = interval.getStart().getMillis() - 2000;
		long shiftEndMillis = interval.getEnd().getMillis() + 2000;

		DateTime gcalStart = new DateTime(new Date(shiftStartMillis), interval.getStart().getZone().toTimeZone());
		DateTime gcalEnd = new DateTime(new Date(shiftEndMillis), interval.getEnd().getZone().toTimeZone());

		myQuery.setMinimumStartTime(gcalStart);
		myQuery.setMaximumStartTime(gcalEnd);
		myQuery.setMaxResults(maxResults);

		// Send the request and receive the response:
		Feed resultFeed;
		ConfidentialReservationData reservation;
		try {

			resultFeed = myService.query(myQuery, Feed.class);

			if (log.isDebugEnabled()) {
				log.debug("Got {} entries for interval: {}", resultFeed.getEntries().size(), interval);
			}

			for (Entry entry : resultFeed.getEntries()) {
				try {

					reservation = convert(entry).getReservation();
					Interval reservedInterval = new Interval(reservation.getFrom(), reservation.getTo());
					if (reservedInterval.overlaps(interval)) {
						reservations.add(reservation);
					}

				} catch (RSFault_Exception e) {
					//noinspection StatementWithEmptyBody
					if (e.getCause() instanceof JAXBException) {
						// ignore and just don't add to reservations, logging is done in create()
					}
					throw e;
				}
			}

			sort(reservations, new Comparator<ConfidentialReservationData>() {
				@Override
				public int compare(final ConfidentialReservationData first,
								   final ConfidentialReservationData second) {

					Interval firstInterval = getReservationInterval(first);
					Interval secondInterval = getReservationInterval(second);

					return firstInterval.isBefore(secondInterval) ? -1 : 1;
				}
			}
			);

		} catch (IOException e) {
			log.error("Error: " + e, e);
			throw createRSFault("Internal Server Error");
		} catch (ServiceException e) {
			log.error("Error: " + e, e);
			throw createRSFault("Internal Server Error");
		} catch (RSFault_Exception e) {
			log.error("Error: " + e, e);
			throw createRSFault("Internal Server Error");
		}

	}

	private Interval getReservationInterval(ConfidentialReservationData crd) {
		return new Interval(crd.getFrom(), crd.getTo());
	}

	private Tuple<Entry, ReservationData> getReservationInternal(SecretReservationKey secretReservationKey)
			throws UnknownSecretReservationKeyFault, RSFault_Exception {
		// Do a full text search for the secretReservationKey and then iterate over the results
		Query myQuery = new Query(eventFeedUrl);
		myQuery.setFullTextQuery("" + secretReservationKey);
		Entry reservationGcalEntry = null;
		ReservationData reservationData = null;

		try {
			Feed myResultsFeed = myService.query(myQuery, Feed.class);

			for (Entry entry : myResultsFeed.getEntries()) {
				// Unmarshal the text and check if the secretReservationKey is the desired one
				ReservationData res = convert(entry);
				if (secretReservationKey.getKey().equals(res.getSecretReservationKey())) {
					reservationData = res;
					reservationGcalEntry = entry;
					break;
				}
			}
		} catch (IOException e) {
			log.error("Error: " + e, e);
			throw createRSFault("Internal Server Error");
		} catch (ServiceException e) {
			log.error("Error: " + e, e);
			throw createRSFault("Internal Server Error");
		}

		// If no reservation was found, throw an exception
		if (reservationData == null) {
			throw WisebedServiceHelper.createRSUnknownSecretReservationKeyFault("Reservation not found",
					secretReservationKey
			);
		}

		// Reservation found, return it
		return new Tuple<Entry, ReservationData>(reservationGcalEntry, reservationData);
	}

	private ReservationData convert(Entry entry) throws RSFault_Exception {
		String content = entry.getPlainTextContent();
		try {
			return unmarshall(content);
		} catch (JAXBException e) {
			log.error("Could not unmarshal " + content, e);
			throw createRSFault("Internal Server Error");
		}
	}

	private RSFault_Exception createRSFault(String msg) {
		log.warn(msg);
		RSFault exception = new RSFault();
		exception.setMessage(msg);
		return new RSFault_Exception(msg, exception);
	}

	private String marshall(ReservationData reservationData) throws JAXBException {
		StringWriter writer = new StringWriter();
		JAXBContext jc = JAXBContext.newInstance(ReservationData.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.marshal(reservationData, writer);
		return writer.toString();
	}

	private ReservationData unmarshall(String reservationDataXml) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(ReservationData.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return (ReservationData) unmarshaller.unmarshal(new StringReader(reservationDataXml));
	}

	@Override
	public String toString() {
		return "GCalRSPersistence{}";
	}


}
