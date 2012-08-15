/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                  *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote *
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

package de.uniluebeck.itm.tr.wsn.federator;

import com.google.common.collect.*;
import de.uniluebeck.itm.tr.federatorutils.FederationManager;
import de.uniluebeck.itm.tr.federatorutils.WebservicePublisher;
import de.uniluebeck.itm.tr.iwsn.common.WSNPreconditions;
import de.uniluebeck.itm.tr.util.SecureIdGenerator;
import eu.wisebed.api.v3.common.KeyValuePair;
import eu.wisebed.api.v3.common.Message;
import eu.wisebed.api.v3.wsn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;


@WebService(
		serviceName = "WSNService",
		targetNamespace = "urn:WSNService",
		portName = "WSNPort",
		endpointInterface = "eu.wisebed.api.v3.wsn.WSN"
)
public class FederatorWSN implements WSN {

	private static final Logger log = LoggerFactory.getLogger(FederatorWSN.class);

	private final ExecutorService executorService;

	private final SecureIdGenerator secureIdGenerator = new SecureIdGenerator();

	private final FederatorController federatorController;

	private final WebservicePublisher<WSN> webservicePublisher;

	private final WSNPreconditions wsnPreconditions;

	private final FederationManager<WSN> federationManager;

	public FederatorWSN(final FederatorController federatorController,
						final FederationManager<WSN> federationManager,
						final WebservicePublisher<WSN> webservicePublisher,
						final WSNPreconditions wsnPreconditions,
						final ExecutorService executorService) {

		this.federatorController = federatorController;
		this.webservicePublisher = webservicePublisher;
		this.federationManager = federationManager;
		this.wsnPreconditions = wsnPreconditions;
		this.executorService = executorService;
	}

	/**
	 * Starts the WSN Web Service and internal Controller Web Service endpoint.
	 *
	 * @throws Exception
	 * 		on failure
	 */
	public void start() throws Exception {
		federatorController.startAndWait();
		webservicePublisher.startAndWait();
	}

	/**
	 * Stops the WSN Web Service and internal Controller Web Service endpoint.
	 *
	 * @throws Exception
	 * 		on failure
	 */
	public void stop() throws Exception {
		webservicePublisher.stopAndWait();
		federatorController.stopAndWait();
	}

	/**
	 * Returns the endpoint URL of this WSN federator instance.
	 *
	 * @return the endpoint URL of this WSN federator instance
	 */
	public String getEndpointUrl() {
		return webservicePublisher.getEndpointUrl().toString();
	}

	@Override
	public void addController(final String controllerEndpointUrl) {

		if (!"NONE".equals(controllerEndpointUrl)) {
			log.debug("Adding controller endpoint URL {}", controllerEndpointUrl);
			federatorController.addController(controllerEndpointUrl);
		}
	}

	@Override
	public void removeController(final String controllerEndpointUrl) {

		log.debug("Removing controller endpoint URL {}", controllerEndpointUrl);
		federatorController.removeController(controllerEndpointUrl);
	}

	@Override
	public String send(final List<String> nodeUrns, final Message message) {

		wsnPreconditions.checkSendArguments(nodeUrns, message);

		String requestId = secureIdGenerator.getNextId();
		Map<WSN, List<String>> map = federationManager.getEndpointToNodeUrnMap(nodeUrns);

		log.debug("Invoking send({}, {}) on {}", new Object[]{nodeUrns, message, map.keySet()});
		for (Map.Entry<WSN, List<String>> entry : map.entrySet()) {

			WSN endpoint = entry.getKey();
			List<String> nodeIdSubset = entry.getValue();

			executorService.submit(new SendRunnable(federatorController, endpoint, requestId, nodeIdSubset, message));
		}

		return requestId;
	}

	@Override
	public String getVersion() {
		return "3.0";
	}

	@Override
	public String areNodesAlive(final List<String> nodeUrns) {

		wsnPreconditions.checkAreNodesAliveArguments(nodeUrns);

		String requestId = secureIdGenerator.getNextId();
		Map<WSN, List<String>> map = federationManager.getEndpointToNodeUrnMap(nodeUrns);

		log.debug("Invoking areNodesAlive({}) on {}", nodeUrns, map.keySet());
		for (Map.Entry<WSN, List<String>> entry : map.entrySet()) {

			WSN endpoint = entry.getKey();
			List<String> nodeUrnSubset = entry.getValue();

			executorService
					.submit(new WSNAreNodesAliveRunnable(federatorController, endpoint, requestId, nodeUrnSubset));
		}

		return requestId;
	}

	@Override
	public List<ChannelPipelinesMap> getChannelPipelines(final List<String> nodeUrns) {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public String getNetwork() {

		final BiMap<String, Callable<String>> endpointUrlToCallableMap = HashBiMap.create();
		final Set<String> endpointUrls = federationManager.getEndpointUrls();

		for (final String endpointUrl : endpointUrls) {
			endpointUrlToCallableMap.put(endpointUrl, new Callable<String>() {
				@Override
				public String call() throws Exception {
					return federationManager.getEndpointByEndpointUrl(endpointUrl).getNetwork();
				}
			}
			);
		}

		return FederatorWiseMLMerger.merge(endpointUrlToCallableMap, executorService);
	}

	@Override
	public String resetNodes(final List<String> nodeUrns) {

		wsnPreconditions.checkResetNodesArguments(nodeUrns);

		String requestId = secureIdGenerator.getNextId();
		Map<WSN, List<String>> map = federationManager.getEndpointToNodeUrnMap(nodeUrns);

		log.debug("Invoking resetNodes({}) on {}", nodeUrns, map.keySet());
		for (Map.Entry<WSN, List<String>> entry : map.entrySet()) {

			WSN endpoint = entry.getKey();
			List<String> nodeIdSubset = entry.getValue();

			executorService.submit(new ResetNodesRunnable(federatorController, endpoint, requestId, nodeIdSubset));
		}

		return requestId;
	}

	@Override
	public String setVirtualLink(final String sourceNodeUrn,
								 final String targetNodeUrn,
								 final String remoteServiceInstance,
								 final List<String> parameters,
								 final List<String> filters) {

		wsnPreconditions.checkSetVirtualLinkArguments(
				sourceNodeUrn,
				targetNodeUrn,
				remoteServiceInstance,
				parameters,
				filters
		);

		String requestId = secureIdGenerator.getNextId();
		WSN endpoint = federationManager.getEndpointByNodeUrn(sourceNodeUrn);

		log.debug("Invoking setVirtualLink({}, {}, {}, {}, {}) on {}",
				new Object[]{sourceNodeUrn, targetNodeUrn, remoteServiceInstance, parameters, filters, endpoint}
		);
		executorService.submit(
				new SetVirtualLinkRunnable(
						federatorController,
						endpoint,
						requestId,
						sourceNodeUrn,
						targetNodeUrn,
						remoteServiceInstance,
						parameters,
						filters
				)
		);

		return requestId;
	}

	@Override
	public String destroyVirtualLink(final String sourceNodeUrn, final String targetNodeUrn) {

		wsnPreconditions.checkDestroyVirtualLinkArguments(sourceNodeUrn, targetNodeUrn);

		String requestId = secureIdGenerator.getNextId();
		WSN endpoint = federationManager.getEndpointByNodeUrn(sourceNodeUrn);

		log.debug("Invoking destroyVirtualLink({}, {}) on {}", new Object[]{sourceNodeUrn, targetNodeUrn, endpoint});
		executorService.submit(
				new DestroyVirtualLinkRunnable(
						federatorController,
						endpoint,
						requestId,
						sourceNodeUrn,
						targetNodeUrn
				)
		);

		return requestId;
	}

	@Override
	public String disableNode(final String nodeUrn) {

		wsnPreconditions.checkDisableNodeArguments(nodeUrn);

		String requestId = secureIdGenerator.getNextId();
		WSN endpoint = federationManager.getEndpointByNodeUrn(nodeUrn);

		log.debug("Invoking disableNode({}) on {}", nodeUrn, endpoint);
		executorService.submit(
				new DisableNodeRunnable(
						federatorController,
						endpoint,
						requestId,
						nodeUrn
				)
		);

		return requestId;
	}

	@Override
	public String disablePhysicalLink(final String sourceNodeUrn, final String targetNodeUrn) {

		wsnPreconditions.checkDisablePhysicalLinkArguments(sourceNodeUrn, targetNodeUrn);

		String requestId = secureIdGenerator.getNextId();
		WSN endpoint = federationManager.getEndpointByNodeUrn(sourceNodeUrn);

		log.debug("Invoking disablePhysicalLink({}, {}) on {}", new Object[]{sourceNodeUrn, targetNodeUrn, endpoint});
		executorService.submit(
				new DisablePhysicalLinkRunnable(
						federatorController,
						endpoint,
						requestId,
						sourceNodeUrn,
						targetNodeUrn
				)
		);

		return requestId;
	}

	@Override
	public void disableVirtualization() throws VirtualizationNotSupported_Exception {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public void enableVirtualization() throws VirtualizationNotSupported_Exception {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public String enableNode(final String nodeUrn) {

		wsnPreconditions.checkEnableNodeArguments(nodeUrn);

		String requestId = secureIdGenerator.getNextId();
		WSN endpoint = federationManager.getEndpointByNodeUrn(nodeUrn);

		log.debug("Invoking enableNode({}) on {}", new Object[]{nodeUrn, endpoint});
		executorService.submit(
				new EnableNodeRunnable(
						federatorController,
						endpoint,
						requestId,
						nodeUrn
				)
		);

		return requestId;
	}

	@Override
	public String enablePhysicalLink(final String sourceNodeUrn, final String targetNodeUrn) {

		wsnPreconditions.checkEnablePhysicalLinkArguments(sourceNodeUrn, targetNodeUrn);

		String requestId = secureIdGenerator.getNextId();
		WSN endpoint = federationManager.getEndpointByNodeUrn(sourceNodeUrn);

		log.debug("Invoking enablePhysicalLink({}, {}) on {}", new Object[]{sourceNodeUrn, targetNodeUrn, endpoint});
		executorService.submit(
				new EnablePhysicalLinkRunnable(
						federatorController,
						endpoint,
						requestId,
						sourceNodeUrn,
						targetNodeUrn
				)
		);

		return requestId;
	}

	@Override
	public String flashPrograms(final List<FlashProgramsConfiguration> flashProgramsConfigurations) {

		wsnPreconditions.checkFlashProgramsArguments(flashProgramsConfigurations);

		final String requestId = secureIdGenerator.getNextId();

		final Multimap<WSN, FlashProgramsConfiguration> federatedConfigurations = HashMultimap.create();

		for (FlashProgramsConfiguration flashProgramsConfiguration : flashProgramsConfigurations) {

			final Map<WSN, List<String>> endpointToNodeUrnMap = federationManager.getEndpointToNodeUrnMap(
					flashProgramsConfiguration.getNodeUrns()
			);

			for (Map.Entry<WSN, List<String>> entry : endpointToNodeUrnMap.entrySet()) {

				final FlashProgramsConfiguration federatedConfiguration = new FlashProgramsConfiguration();
				federatedConfiguration.setProgram(flashProgramsConfiguration.getProgram());
				federatedConfiguration.getNodeUrns().addAll(entry.getValue());

				federatedConfigurations.put(entry.getKey(), federatedConfiguration);
			}
		}


		for (final WSN wsn : federatedConfigurations.keySet()) {

			executorService.submit(new FlashProgramsRunnable(
					federatorController,
					wsn,
					requestId,
					newArrayList(federatedConfigurations.get(wsn))
			)
			);
		}

		return requestId;
	}

	@Override
	public List<ChannelHandlerDescription> getSupportedChannelHandlers() {

		log.debug("getSupportedChannelHandlers() called...");

		final ImmutableSet<FederationManager.Entry<WSN>> entries = federationManager.getEntries();
		final Map<FederationManager.Entry<WSN>, Future<List<ChannelHandlerDescription>>> entryToResultMapping =
				Maps.newHashMap();

		// fork calls to endpoints
		for (final FederationManager.Entry<WSN> entry : entries) {
			final Future<List<ChannelHandlerDescription>> future = executorService.submit(
					new GetSupportedChannelHandlersCallable(entry.endpoint)
			);
			entryToResultMapping.put(entry, future);
		}

		final Set<ChannelHandlerDescription> commonHandlers = newTreeSet(CHANNEL_HANDLER_DESCRIPTION_COMPARATOR);

		for (Map.Entry<FederationManager.Entry<WSN>, Future<List<ChannelHandlerDescription>>> outerEntry : entryToResultMapping
				.entrySet()) {

			try {

				List<ChannelHandlerDescription> outerChannelHandlers = outerEntry.getValue().get();

				for (ChannelHandlerDescription outerChannelHandler : outerChannelHandlers) {

					boolean containedInAllOthers = true;

					for (Map.Entry<FederationManager.Entry<WSN>, Future<List<ChannelHandlerDescription>>> innerEntry : entryToResultMapping
							.entrySet()) {

						if (innerEntry != outerEntry) {

							boolean outerContainedInInnerEntry = false;

							final List<ChannelHandlerDescription> innerChannelHandlers = innerEntry.getValue().get();
							for (ChannelHandlerDescription innerChannelHandler : innerChannelHandlers) {
								if (equals(outerChannelHandler, innerChannelHandler)) {
									outerContainedInInnerEntry = true;
									break;
								}
							}

							if (!outerContainedInInnerEntry) {
								containedInAllOthers = false;
								break;
							}
						}
					}

					if (containedInAllOthers) {
						commonHandlers.add(outerChannelHandler);
					}
				}

			} catch (Exception e) {
				log.error("Error while calling getFilters() on federated WSN endpoint \"{}\". Ignoring this endpoint.",
						outerEntry.getKey()
				);
			}
		}

		return newArrayList(commonHandlers);
	}

	@Override
	public List<String> getSupportedVirtualLinkFilters() {

		ImmutableSet<String> endpointUrls = federationManager.getEndpointUrls();
		Map<String, Future<ImmutableSet<String>>> endpointUrlToResultsMapping = Maps.newHashMap();

		// fork calls to endpoints
		log.debug("Invoking getFilters() on {}", endpointUrls);
		for (final String endpointUrl : endpointUrls) {
			Future<ImmutableSet<String>> future = executorService.submit(new Callable<ImmutableSet<String>>() {
				@Override
				public ImmutableSet<String> call() throws Exception {
					WSN endpoint = federationManager.getEndpointByEndpointUrl(endpointUrl);
					return ImmutableSet.copyOf(endpoint.getSupportedVirtualLinkFilters());
				}
			}
			);
			endpointUrlToResultsMapping.put(endpointUrl, future);
		}

		// join results from endpoints
		ImmutableSet<String> intersectedFilters = null;
		for (Map.Entry<String, Future<ImmutableSet<String>>> entry : endpointUrlToResultsMapping.entrySet()) {

			try {

				ImmutableSet<String> endpointFilters = entry.getValue().get();

				if (intersectedFilters == null) {
					intersectedFilters = endpointFilters;
				} else {
					intersectedFilters = ImmutableSet.copyOf(Sets.intersection(intersectedFilters, endpointFilters));
				}

			} catch (Exception e) {
				log.error("Error while calling getFilters() on federated WSN endpoint \"{}\". Ignoring this endpoint.",
						entry.getKey()
				);
			}
		}

		return Lists.newArrayList(intersectedFilters);
	}

	private static final Comparator<ChannelHandlerDescription> CHANNEL_HANDLER_DESCRIPTION_COMPARATOR =
			new Comparator<ChannelHandlerDescription>() {
				@Override
				public int compare(final ChannelHandlerDescription o1, final ChannelHandlerDescription o2) {
					return FederatorWSN.equals(o1, o2) ? 0 : -1;
				}
			};

	private static boolean equals(final ChannelHandlerDescription outerChannelHandler,
								  final ChannelHandlerDescription innerChannelHandler) {

		if (!outerChannelHandler.getName().equals(innerChannelHandler.getName())) {
			return false;
		}

		Set<String> outerConfigurationKeys = newHashSet();
		Set<String> innerConfigurationKeys = newHashSet();

		for (KeyValuePair keyValuePair : outerChannelHandler.getConfigurationOptions()) {
			outerConfigurationKeys.add(keyValuePair.getKey());
		}

		for (KeyValuePair keyValuePair : innerChannelHandler.getConfigurationOptions()) {
			innerConfigurationKeys.add(keyValuePair.getKey());
		}

		return Sets.symmetricDifference(outerConfigurationKeys, innerConfigurationKeys).size() == 0;
	}

	@Override
	public String setChannelPipeline(final List<String> nodeUrns,
									 final List<ChannelHandlerConfiguration> channelHandlerConfigurations) {

		log.debug("setChannelPipeline({}, {}) called...", nodeUrns, channelHandlerConfigurations);

		final String federatorRequestId = secureIdGenerator.getNextId();
		final Map<WSN, List<String>> endpointToNodesMapping = constructEndpointToNodesMapping(nodeUrns);

		for (WSN wsnEndpoint : endpointToNodesMapping.keySet()) {

			final SetChannelPipelineRunnable setChannelPipelineRunnable =
					new SetChannelPipelineRunnable(
							federatorController,
							wsnEndpoint,
							federatorRequestId,
							endpointToNodesMapping.get(wsnEndpoint),
							channelHandlerConfigurations
					);

			executorService.submit(setChannelPipelineRunnable);
		}

		return federatorRequestId;
	}

	private Map<WSN, List<String>> constructEndpointToNodesMapping(final List<String> nodeUrns) {

		final Map<WSN, List<String>> mapping = newHashMap();

		for (String nodeUrn : nodeUrns) {

			final WSN endpoint = federationManager.getEndpointByNodeUrn(nodeUrn);

			List<String> filteredNodeUrns = mapping.get(endpoint);
			if (filteredNodeUrns == null) {
				filteredNodeUrns = newArrayList();
				mapping.put(endpoint, filteredNodeUrns);
			}
			filteredNodeUrns.add(nodeUrn);

		}

		return mapping;
	}

}
