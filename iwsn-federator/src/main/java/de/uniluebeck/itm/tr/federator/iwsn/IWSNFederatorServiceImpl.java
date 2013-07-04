package de.uniluebeck.itm.tr.federator.iwsn;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class IWSNFederatorServiceImpl extends AbstractService implements IWSNFederatorService {

	private final SessionManagementFederatorService sessionManagementFederatorService;

	@Inject
	public IWSNFederatorServiceImpl(final SessionManagementFederatorService sessionManagementFederatorService) {
		this.sessionManagementFederatorService = checkNotNull(sessionManagementFederatorService);
	}

	@Override
	protected void doStart() {
		try {
			sessionManagementFederatorService.startAndWait();
			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {
			if (sessionManagementFederatorService.isRunning()) {
				sessionManagementFederatorService.stopAndWait();
			}
			notifyStopped();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

}
