package de.uniluebeck.itm.tr.iwsn.common;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ResponseTrackerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ResponseTrackerCache.class).to(ResponseTrackerCacheImpl.class);
		install(new FactoryModuleBuilder()
				.implement(ResponseTracker.class, ResponseTrackerImpl.class)
				.build(ResponseTrackerFactory.class)
		);
	}
}
