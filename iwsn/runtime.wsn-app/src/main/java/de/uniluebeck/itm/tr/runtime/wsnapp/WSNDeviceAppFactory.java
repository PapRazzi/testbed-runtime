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

package de.uniluebeck.itm.tr.runtime.wsnapp;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniluebeck.itm.gtr.TestbedRuntime;
import de.uniluebeck.itm.gtr.application.TestbedApplication;
import de.uniluebeck.itm.gtr.application.TestbedApplicationFactory;
import de.uniluebeck.itm.tr.runtime.wsnapp.xml.WsnDevice;
import de.uniluebeck.itm.tr.runtime.wsnapp.xml.Wsnapp;
import de.uniluebeck.itm.tr.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


public class WSNDeviceAppFactory implements TestbedApplicationFactory {

	private static final Logger log = LoggerFactory.getLogger(WSNDeviceAppFactory.class);

	@Override
	public TestbedApplication create(TestbedRuntime testbedRuntime, String applicationName, Object configuration) {

		try {

			JAXBContext context = JAXBContext.newInstance(Wsnapp.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Wsnapp config = (Wsnapp) unmarshaller.unmarshal((Node) configuration);

			ImmutableList.Builder<WSNDeviceApp> builder = new ImmutableList.Builder<WSNDeviceApp>();

			for (WsnDevice wsnDevice : config.getDevice()) {

				String nodeUrn, nodeType, nodeSerialInterface, nodeUSBChipID;
				Integer nodeAPITimeout;

				try {

					nodeUrn = wsnDevice.getUrn();
					nodeType = wsnDevice.getType();
					nodeSerialInterface = wsnDevice.getSerialinterface();
					nodeAPITimeout = wsnDevice.getNodeapitimeout();
                    nodeUSBChipID = wsnDevice.getUsbchipid();

					Preconditions.checkNotNull(nodeUrn);
					Preconditions.checkNotNull(nodeType);
					StringUtils.assertHexOrDecLongUrnSuffix(nodeUrn);

				} catch (Exception e) {
					log.error("Ignoring device. Reason: " + e.getMessage(), e);
					// ignore this device as it is badly configured
					continue;
				}

				WSNDeviceAppModule module =
						new WSNDeviceAppModule(nodeUrn, nodeType, nodeSerialInterface, nodeAPITimeout, nodeUSBChipID, testbedRuntime);
				Injector injector = Guice.createInjector(module);
				builder.add(injector.getInstance(WSNDeviceApp.class));

			}

			return new WSNDeviceAppWrapper(builder.build());

		} catch (JAXBException e) {
			log.error("Error unmarshalling WsnApplication config: " + e, e);
		}

		return null;

	}

}
