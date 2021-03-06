package de.uniluebeck.itm.tr.iwsn.portal.eventstore;

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.uniluebeck.itm.tr.iwsn.portal.eventstore.adminui.EventStoreAdminService;
import de.uniluebeck.itm.tr.iwsn.portal.eventstore.adminui.EventStoreAdminServiceImpl;

public class PortalEventStoreModule extends PrivateModule {

	@Override
	protected void configure() {

		bind(PortalEventStore.class).to(PortalEventStoreImpl.class).in(Singleton.class);
		bind(PortalEventStoreHelper.class).to(PortalEventStoreHelperImpl.class).in(Singleton.class);
		bind(EventStoreAdminService.class).to(EventStoreAdminServiceImpl.class).in(Singleton.class);
		install(new FactoryModuleBuilder()
						.implement(ReservationEventStore.class, ReservationEventStoreImpl.class)
						.build(ReservationEventStoreFactory.class)
		);

		expose(PortalEventStore.class);
		expose(ReservationEventStoreFactory.class);
	}
}

