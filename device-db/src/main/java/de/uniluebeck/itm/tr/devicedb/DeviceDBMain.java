package de.uniluebeck.itm.tr.devicedb;

import com.google.inject.Guice;
import de.uniluebeck.itm.tr.util.Logging;
import org.apache.log4j.Level;

import static de.uniluebeck.itm.tr.iwsn.common.config.ConfigHelper.parseOrExit;
import static de.uniluebeck.itm.tr.iwsn.common.config.ConfigHelper.setLogLevel;

public class DeviceDBMain {

	static {
		Logging.setLoggingDefaults(Level.WARN);
	}

	public static void main(String[] args) {

		final DeviceDBMainConfig config = setLogLevel(
				parseOrExit(new DeviceDBMainConfig(), DeviceDBServiceImpl.class, args),
				"de.uniluebeck.itm"
		);

		final DeviceDBServiceFactory deviceDBServiceFactory = Guice
				.createInjector(new DeviceDBMainModule(config))
				.getInstance(DeviceDBServiceFactory.class);

		final DeviceDBService deviceDBService = deviceDBServiceFactory.create("/rest", "/");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (deviceDBService.isRunning()) {
					deviceDBService.stopAndWait();
				}
			}
		});

		deviceDBService.startAndWait();
	}

}