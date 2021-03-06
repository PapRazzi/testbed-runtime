package de.uniluebeck.itm.tr.iwsn.portal;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import de.uniluebeck.itm.servicepublisher.ServicePublisher;
import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
import de.uniluebeck.itm.tr.common.Constants;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class WiseGuiServiceImpl extends AbstractService implements WiseGuiService {

	private final ServicePublisher servicePublisher;

	private final WiseGuiServiceConfig wiseGuiServiceConfig;

	private ServicePublisherService webapp;

	@Inject
	public WiseGuiServiceImpl(final ServicePublisher servicePublisher,
							  final WiseGuiServiceConfig wiseGuiServiceConfig) {
		this.servicePublisher = servicePublisher;
		this.wiseGuiServiceConfig = wiseGuiServiceConfig;
	}

	@Override
	protected void doStart() {
		try {

			final String resourceBase;
			final File wiseGuiSourceDir = wiseGuiServiceConfig.getWiseGuiSourceDir();
			if (wiseGuiSourceDir != null && !"".equals(wiseGuiSourceDir.toString())) {

				if (!wiseGuiSourceDir.exists()) {
					throw new IllegalArgumentException(
							"WiseGui source directory \"" + wiseGuiSourceDir + "\" does not exist"
					);
				}

				if (!wiseGuiSourceDir.isDirectory()) {
					throw new IllegalArgumentException(
							"WiseGui source directory \"" + wiseGuiSourceDir + "\" is not a directory"
					);
				}

				if (!wiseGuiSourceDir.canRead()) {
					throw new IllegalArgumentException(
							"WiseGui source directory \"" + wiseGuiSourceDir + "\" can't be read"
					);
				}

				resourceBase = wiseGuiSourceDir.toString();

			} else {
				final URL resource = this.getClass().getResource("/de/uniluebeck/itm/tr/iwsn/portal/wisegui");
				if (resource == null) {
					throw new IllegalArgumentException("The internal version of WiseGui cannot be started. " +
							"Did you forget to initialize and update the submodule running " +
							"'git submodule init' and 'git submodule update'?"
					);
				}
				resourceBase = resource.toString();
			}

			final Map<String, String> params = newHashMap();

			params.put(WiseGuiServiceConfig.WISEGUI_TESTBED_NAME, wiseGuiServiceConfig.getWiseguiTestbedName());

			params.put(Constants.WISEGUI.CONTEXT_PATH_KEY, Constants.WISEGUI.CONTEXT_PATH_VALUE);
			params.put(Constants.REST_API_V1.REST_API_CONTEXT_PATH_KEY, Constants.REST_API_V1.REST_API_CONTEXT_PATH_VALUE);
			params.put(Constants.REST_API_V1.WEBSOCKET_CONTEXT_PATH_KEY, Constants.REST_API_V1.WEBSOCKET_CONTEXT_PATH_VALUE);

			params.put("allowedOrigins", "http://*");
			params.put("allowedMethods", "GET,POST,PUT,DELETE");
			params.put("allowCredentials", "true");

			webapp = servicePublisher.createServletService(
					Constants.WISEGUI.CONTEXT_PATH_VALUE,
					resourceBase,
					params,
					null,
					new CrossOriginFilter()
			);

			webapp.startAsync().awaitRunning();

			notifyStarted();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {
			if (webapp != null && webapp.isRunning()) {
				webapp.stopAsync().awaitTerminated();
			}
			notifyStopped();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}
}
