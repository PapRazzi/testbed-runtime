package de.uniluebeck.itm.tr.iwsn.gateway;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.uniluebeck.itm.servicepublisher.ServicePublisher;
import de.uniluebeck.itm.servicepublisher.ServicePublisherConfig;
import de.uniluebeck.itm.servicepublisher.ServicePublisherFactory;
import de.uniluebeck.itm.tr.common.config.CommonConfig;
import de.uniluebeck.itm.tr.common.config.ConfigWithLoggingAndProperties;
import de.uniluebeck.itm.tr.devicedb.DeviceDBService;
import de.uniluebeck.itm.tr.iwsn.gateway.rest.RestApplication;
import de.uniluebeck.itm.util.logging.LogLevel;
import de.uniluebeck.itm.util.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.uniluebeck.itm.tr.common.config.ConfigHelper.parseOrExit;
import static de.uniluebeck.itm.tr.common.config.ConfigHelper.setLogLevel;
import static de.uniluebeck.itm.util.propconf.PropConfBuilder.buildConfig;

public class Gateway extends AbstractService {

	static {
		Logging.setLoggingDefaults(LogLevel.WARN);
	}

	private static final Logger log = LoggerFactory.getLogger(Gateway.class);

	private final GatewayConfig gatewayConfig;

	private final GatewayEventBus gatewayEventBus;

	private final DeviceManager deviceManager;

	private final DeviceObserverWrapper deviceObserverWrapper;

	private final RestApplication restApplication;

	private final RequestHandler requestHandler;

	private final ServicePublisherConfig servicePublisherConfig;

	private final ServicePublisherFactory servicePublisherFactory;

	private final DeviceDBService deviceDBService;

	private ServicePublisher servicePublisher;

	@Inject
	public Gateway(final GatewayConfig gatewayConfig,
				   final GatewayEventBus gatewayEventBus,
				   final DeviceDBService deviceDBService,
				   final DeviceManager deviceManager,
				   final DeviceObserverWrapper deviceObserverWrapper,
				   final RestApplication restApplication,
				   final RequestHandler requestHandler,
				   final ServicePublisherConfig servicePublisherConfig,
				   final ServicePublisherFactory servicePublisherFactory) {
		this.deviceDBService = checkNotNull(deviceDBService);
		this.gatewayConfig = checkNotNull(gatewayConfig);
		this.gatewayEventBus = checkNotNull(gatewayEventBus);
		this.deviceManager = checkNotNull(deviceManager);
		this.deviceObserverWrapper = checkNotNull(deviceObserverWrapper);
		this.restApplication = checkNotNull(restApplication);
		this.requestHandler = checkNotNull(requestHandler);
		this.servicePublisherConfig = checkNotNull(servicePublisherConfig);
		this.servicePublisherFactory = checkNotNull(servicePublisherFactory);
	}

	@Override
	protected void doStart() {

		log.trace("Gateway.doStart()");

		try {

			deviceDBService.startAndWait();
			gatewayEventBus.startAndWait();
			requestHandler.startAndWait();
			deviceManager.startAndWait();
			deviceObserverWrapper.startAndWait();

			if (gatewayConfig.isRestAPI()) {

				servicePublisher = servicePublisherFactory.create(servicePublisherConfig);
				servicePublisher.createJaxRsService("/devices", restApplication);
				servicePublisher.startAndWait();
			}

			notifyStarted();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {

		log.trace("Gateway.doStop()");

		try {

			if (gatewayConfig.isRestAPI() && servicePublisher != null) {
				servicePublisher.stopAndWait();
			}

			deviceObserverWrapper.stopAndWait();
			deviceManager.stopAndWait();
			requestHandler.stopAndWait();
			gatewayEventBus.stopAndWait();
			deviceDBService.stopAndWait();

			notifyStopped();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	public static void main(String[] args) {

		Thread.currentThread().setName("Gateway-Main");

		final ConfigWithLoggingAndProperties config = setLogLevel(
				parseOrExit(new ConfigWithLoggingAndProperties(), Gateway.class, args),
				"de.uniluebeck.itm"
		);

		final CommonConfig commonConfig = buildConfig(CommonConfig.class, config.config);
		final GatewayConfig gatewayConfig = buildConfig(GatewayConfig.class, config.config);
		final GatewayModule gatewayModule = new GatewayModule(commonConfig, gatewayConfig);
		final Injector injector = Guice.createInjector(gatewayModule);
		final Gateway gateway = injector.getInstance(Gateway.class);

		gateway.startAndWait();

		log.info("Gateway started!");

		Runtime.getRuntime().addShutdownHook(new Thread("Gateway-Shutdown") {
			@Override
			public void run() {
				log.info("Received KILL signal. Shutting down iWSN Gateway...");
				gateway.stopAndWait();
				log.info("Over and out.");
			}
		}
		);
	}
}
