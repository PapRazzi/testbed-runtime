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

package de.uniluebeck.itm.tr.wsn.federator;

import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.wsn.Link;
import eu.wisebed.api.v3.wsn.ReservationNotRunningFault_Exception;
import eu.wisebed.api.v3.wsn.VirtualizationNotEnabledFault_Exception;
import eu.wisebed.api.v3.wsn.WSN;

import static com.google.common.collect.Lists.newArrayList;

class DisablePhysicalLinkCallable extends AbstractRequestCallable {

	private final NodeUrn sourceNodeUrn;

	private final NodeUrn targetNodeUrn;

	DisablePhysicalLinkCallable(final FederatorController federatorController,
								final WSN wsnEndpoint,
								final long federatedRequestId,
								final long federatorRequestId,
								final NodeUrn sourceNodeUrn,
								final NodeUrn targetNodeUrn) {

		super(federatorController, wsnEndpoint, federatedRequestId, federatorRequestId);

		this.sourceNodeUrn = sourceNodeUrn;
		this.targetNodeUrn = targetNodeUrn;
	}

	@Override
	protected void executeRequestOnFederatedTestbed(final long federatedRequestId)
			throws ReservationNotRunningFault_Exception, VirtualizationNotEnabledFault_Exception {
		final Link link = new Link();
		link.setSourceNodeUrn(sourceNodeUrn);
		link.setTargetNodeUrn(targetNodeUrn);
		wsnEndpoint.disablePhysicalLinks(federatedRequestId, newArrayList(link), null);
	}
}
