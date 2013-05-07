package de.uniluebeck.itm.tr.iwsn.portal;

import com.google.inject.Provider;
import de.uniluebeck.itm.tr.devicedb.DeviceDB;
import de.uniluebeck.itm.tr.iwsn.common.SchedulerService;
import de.uniluebeck.itm.tr.iwsn.common.SchedulerServiceFactory;
import de.uniluebeck.itm.tr.util.Logging;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.NodeUrnPrefix;
import eu.wisebed.api.v3.common.SecretReservationKey;
import eu.wisebed.api.v3.rs.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReservationManagerImplTest {

	static {
		Logging.setLoggingDefaults();
	}

	private static final String USERNAME = "My Awesome Username";

	private static final NodeUrnPrefix NODE_URN_PREFIX = new NodeUrnPrefix("urn:unit-test:");

	private static final String KNOWN_SECRET_RESERVATION_KEY_1 = "YOU_KNOW_ME_ONE";

	private static final String KNOWN_SECRET_RESERVATION_KEY_2 = "YOU_KNOW_ME_TWO";

	private static final String KNOWN_SECRET_RESERVATION_KEY_3 = "YOU_KNOW_ME_THREE";

	private static final String UNKNOWN_SECRET_RESERVATION_KEY_1 = "YOU_DO_NOT_KNOW_ME";

	private static final List<SecretReservationKey> KNOWN_SECRET_RESERVATION_KEY_LIST_1 = newArrayList();

	private static final List<SecretReservationKey> KNOWN_SECRET_RESERVATION_KEY_LIST_2 = newArrayList();

	private static final List<SecretReservationKey> KNOWN_SECRET_RESERVATION_KEY_LIST_3 = newArrayList();

	private static final List<SecretReservationKey> UNKNOWN_SECRET_RESERVATION_KEY_LIST = newArrayList();

	static {

		final SecretReservationKey srk1 = new SecretReservationKey();
		srk1.setUrnPrefix(NODE_URN_PREFIX);
		srk1.setKey(KNOWN_SECRET_RESERVATION_KEY_1);
		KNOWN_SECRET_RESERVATION_KEY_LIST_1.add(srk1);

		final SecretReservationKey srk2 = new SecretReservationKey();
		srk2.setUrnPrefix(NODE_URN_PREFIX);
		srk2.setKey(KNOWN_SECRET_RESERVATION_KEY_2);
		KNOWN_SECRET_RESERVATION_KEY_LIST_2.add(srk2);

		final SecretReservationKey srk3 = new SecretReservationKey();
		srk3.setUrnPrefix(NODE_URN_PREFIX);
		srk3.setKey(KNOWN_SECRET_RESERVATION_KEY_3);
		KNOWN_SECRET_RESERVATION_KEY_LIST_3.add(srk3);

		final SecretReservationKey srk4 = new SecretReservationKey();
		srk4.setUrnPrefix(NODE_URN_PREFIX);
		srk4.setKey(UNKNOWN_SECRET_RESERVATION_KEY_1);
		UNKNOWN_SECRET_RESERVATION_KEY_LIST.add(srk4);
	}

	private static final Set<NodeUrn> RESERVATION_NODE_URNS_1 = newHashSet(new NodeUrn(NODE_URN_PREFIX + "0x0001"));

	private static final Set<NodeUrn> RESERVATION_NODE_URNS_2 = newHashSet(new NodeUrn(NODE_URN_PREFIX + "0x0002"));

	private static final Set<NodeUrn> RESERVATION_NODE_URNS_3 = newHashSet(new NodeUrn(NODE_URN_PREFIX + "0x0003"));

	private static final Interval RESERVATION_INTERVAL_1 = new Interval(
			DateTime.now(),
			DateTime.now().plusHours(1)
	);

	private static final Interval RESERVATION_INTERVAL_2 = new Interval(
			DateTime.now().plusMinutes(1),
			DateTime.now().plusHours(1)
	);

	private static final Interval RESERVATION_INTERVAL_3 = new Interval(
			DateTime.now().minusHours(1),
			DateTime.now().minusMinutes(1)
	);

	private static final List<ConfidentialReservationData> RESERVATION_DATA_1 = newArrayList();

	private static final List<ConfidentialReservationData> RESERVATION_DATA_2 = newArrayList();

	private static final List<ConfidentialReservationData> RESERVATION_DATA_3 = newArrayList();

	static {

		final ConfidentialReservationData crd1 = new ConfidentialReservationData();
		crd1.setFrom(RESERVATION_INTERVAL_1.getStart());
		crd1.setTo(RESERVATION_INTERVAL_1.getEnd());
		crd1.getNodeUrns().addAll(RESERVATION_NODE_URNS_1);
		final ConfidentialReservationDataKey key1 = new ConfidentialReservationDataKey();
		key1.setKey(KNOWN_SECRET_RESERVATION_KEY_1);
		key1.setUrnPrefix(NODE_URN_PREFIX);
		key1.setUsername(USERNAME);
		crd1.getKeys().add(key1);
		RESERVATION_DATA_1.add(crd1);

		final ConfidentialReservationData crd2 = new ConfidentialReservationData();
		crd2.setFrom(RESERVATION_INTERVAL_2.getStart());
		crd2.setTo(RESERVATION_INTERVAL_2.getEnd());
		crd2.getNodeUrns().addAll(RESERVATION_NODE_URNS_2);
		final ConfidentialReservationDataKey key2 = new ConfidentialReservationDataKey();
		key2.setKey(KNOWN_SECRET_RESERVATION_KEY_2);
		key2.setUrnPrefix(NODE_URN_PREFIX);
		key2.setUsername(USERNAME);
		crd2.getKeys().add(key2);
		RESERVATION_DATA_2.add(crd2);

		final ConfidentialReservationData crd3 = new ConfidentialReservationData();
		crd3.setFrom(RESERVATION_INTERVAL_3.getStart());
		crd3.setTo(RESERVATION_INTERVAL_3.getEnd());
		crd3.getNodeUrns().addAll(RESERVATION_NODE_URNS_3);
		final ConfidentialReservationDataKey key3 = new ConfidentialReservationDataKey();
		key3.setKey(KNOWN_SECRET_RESERVATION_KEY_3);
		key3.setUrnPrefix(NODE_URN_PREFIX);
		key3.setUsername(USERNAME);
		crd3.getKeys().add(key3);
		RESERVATION_DATA_3.add(crd3);
	}

	@Mock
	private Provider<RS> rsProvider;

	@Mock
	private RS rs;

	@Mock
	private DeviceDB deviceDB;

	@Mock
	private ReservationFactory reservationFactory;

	@Mock
	private Reservation reservation1;

	@Mock
	private Reservation reservation2;

	@Mock
	private Reservation reservation3;

	@Mock
	private SchedulerServiceFactory schedulerServiceFactory;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private PortalConfig portalConfig;

	private ReservationManagerImpl reservationManager;

	@Before
	public void setUp() throws Exception {
		when(portalConfig.getUrnPrefix()).thenReturn(NODE_URN_PREFIX);
		when(schedulerServiceFactory.create(anyInt(), anyString())).thenReturn(schedulerService);
		when(rsProvider.get()).thenReturn(rs);
		reservationManager = new ReservationManagerImpl(
				portalConfig,
				rsProvider,
				deviceDB,
				reservationFactory,
				schedulerServiceFactory
		);
		reservationManager.startAndWait();
	}

	@After
	public void tearDown() throws Exception {
		reservationManager.stopAndWait();
	}

	@Test
	public void testThatReservationIsStartedAndReturnedIfKnown() throws Exception {

		setUpReservation1();

		assertSame(reservation1, reservationManager.getReservation(KNOWN_SECRET_RESERVATION_KEY_1));
		verify(reservationFactory).create(
				eq(KNOWN_SECRET_RESERVATION_KEY_1),
				eq(USERNAME),
				eq(RESERVATION_NODE_URNS_1),
				eq(RESERVATION_INTERVAL_1)
		);
		verify(reservation1).startAndWait();
	}

	@Test
	public void testThatNullIsReturnedAndNoReservationIsCreatedIfReservationIsUnknown() throws Exception {

		setUpUnknownReservation();

		try {
			reservationManager.getReservation(UNKNOWN_SECRET_RESERVATION_KEY_1);
		} catch (ReservationUnknownException e) {
			verifyZeroInteractions(deviceDB);
			verifyZeroInteractions(reservationFactory);
		}
	}

	@Test
	public void testThatAllRunningReservationsAreShutDownWhenReservationManagerIsShutDown() throws Exception {

		setUpReservation1();
		setUpReservation2();

		reservationManager.getReservation(KNOWN_SECRET_RESERVATION_KEY_1);
		reservationManager.getReservation(KNOWN_SECRET_RESERVATION_KEY_2);

		reservationManager.stopAndWait();

		verify(reservation1).stopAndWait();
		verify(reservation2).stopAndWait();
	}

	@Test
	public void testThatReservationStartIsImmediatelyIfIntervalIsMet() throws Exception {

		setUpReservation1();
		when(reservation1.isRunning()).thenReturn(false);

		reservationManager.getReservation(KNOWN_SECRET_RESERVATION_KEY_1);

		verify(reservation1).isRunning();
		verify(reservation1).startAndWait();
		verify(schedulerService).schedule(any(ReservationStopCallable.class), anyLong(), any(TimeUnit.class));
	}

	@Test
	public void testThatReservationStartAndStopIsScheduledIfIntervalIsNotYetMet() throws Exception {

		setUpReservation2();
		when(reservation2.isRunning()).thenReturn(false);

		reservationManager.getReservation(KNOWN_SECRET_RESERVATION_KEY_2);

		verify(reservation2).isRunning();
		verify(reservation2, never()).startAndWait();
		verify(schedulerService).schedule(isA(ReservationStartCallable.class), anyLong(), any(TimeUnit.class));
		verify(schedulerService).schedule(isA(ReservationStopCallable.class), anyLong(), any(TimeUnit.class));
	}

	@Test
	public void testThatReservationStartAndStopIsNotScheduledIfReservationIntervalLiesInThePast() throws Exception {

		setUpReservation3();
		when(reservation3.isRunning()).thenReturn(false);

		reservationManager.getReservation(KNOWN_SECRET_RESERVATION_KEY_3);

		verify(reservation3).isRunning();
		verify(reservation3, never()).startAndWait();
		verify(schedulerService, never()).schedule(any(ReservationStartCallable.class), anyLong(), any(TimeUnit.class));
		verify(schedulerService, never()).schedule(any(ReservationStopCallable.class), anyLong(), any(TimeUnit.class));
	}

	private void setUpReservation1() throws RSFault_Exception, UnknownSecretReservationKeyFault {
		when(rs.getReservation(KNOWN_SECRET_RESERVATION_KEY_LIST_1)).thenReturn(RESERVATION_DATA_1);
		when(reservationFactory.create(
				KNOWN_SECRET_RESERVATION_KEY_1,
				USERNAME,
				RESERVATION_NODE_URNS_1,
				RESERVATION_INTERVAL_1
		)
		).thenReturn(reservation1);
		when(reservation1.getInterval()).thenReturn(RESERVATION_INTERVAL_1);
	}

	private void setUpReservation2() throws RSFault_Exception, UnknownSecretReservationKeyFault {
		when(rs.getReservation(KNOWN_SECRET_RESERVATION_KEY_LIST_2)).thenReturn(RESERVATION_DATA_2);
		when(reservationFactory.create(
				KNOWN_SECRET_RESERVATION_KEY_2,
				USERNAME,
				RESERVATION_NODE_URNS_2,
				RESERVATION_INTERVAL_2
		)
		).thenReturn(reservation2);
		when(reservation2.getInterval()).thenReturn(RESERVATION_INTERVAL_2);
	}

	private void setUpReservation3() throws RSFault_Exception, UnknownSecretReservationKeyFault {
		when(rs.getReservation(KNOWN_SECRET_RESERVATION_KEY_LIST_3)).thenReturn(RESERVATION_DATA_3);
		when(reservationFactory.create(
				KNOWN_SECRET_RESERVATION_KEY_3,
				USERNAME,
				RESERVATION_NODE_URNS_3,
				RESERVATION_INTERVAL_3
		)
		).thenReturn(reservation3);
		when(reservation3.getInterval()).thenReturn(RESERVATION_INTERVAL_3);
	}

	private void setUpUnknownReservation() throws RSFault_Exception, UnknownSecretReservationKeyFault {
		when(rs.getReservation(eq(UNKNOWN_SECRET_RESERVATION_KEY_LIST))).thenThrow(
				new UnknownSecretReservationKeyFault(
						"not found",
						new eu.wisebed.api.v3.common.UnknownSecretReservationKeyFault()
				)
		);
	}
}
