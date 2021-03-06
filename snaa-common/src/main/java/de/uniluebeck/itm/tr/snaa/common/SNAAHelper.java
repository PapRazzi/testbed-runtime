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

package de.uniluebeck.itm.tr.snaa.common;

import eu.wisebed.api.v3.common.*;
import eu.wisebed.api.v3.snaa.*;
import eu.wisebed.api.v3.snaa.AuthenticationFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class SNAAHelper {

	private static final Logger log = LoggerFactory.getLogger(SNAAHelper.class);

	private SNAAHelper() {
		// disallow instantiation
	}

	public static void assertMinAuthenticationCount(List<AuthenticationTriple> authenticationData,
													int minCountInclusive)
			throws SNAAFault_Exception {
		try {
			assertCollectionMinCount(authenticationData, minCountInclusive);
		} catch (Exception e) {
			throw createSNAAFault(e.getMessage());
		}
	}

	public static void assertAuthenticationCount(List<AuthenticationTriple> authenticationData, int minCountInclusive,
												 int maxCountInclusive) throws SNAAFault_Exception {

		try {
			assertCollectionMinMaxCount(authenticationData, minCountInclusive, maxCountInclusive);
		} catch (Exception e) {
			throw createSNAAFault(e.getMessage());
		}
	}

	public static void assertAuthenticationKeyCount(List<SecretAuthenticationKey> authenticationData,
													int minCountInclusive, int maxCountInclusive)
			throws SNAAFault_Exception {

		try {
			assertCollectionMinCount(authenticationData, minCountInclusive);
		} catch (Exception e) {
			throw createSNAAFault(e.getMessage());
		}

	}

	public static void assertElementCount(Collection<?> c, int minCountInclusive, int maxCountInclusive)
			throws SNAAFault_Exception {

		try {
			assertCollectionMinCount(c, minCountInclusive);
		} catch (Exception e) {
			throw createSNAAFault(e.getMessage());
		}
	}

	public static void assertUrnPrefixServed(Set<NodeUrnPrefix> servedURNPrefixes,
											 List<AuthenticationTriple> authenticationData) throws SNAAFault_Exception {
		for (NodeUrnPrefix servedURNPrefix : servedURNPrefixes) {
			assertUrnPrefixServed(servedURNPrefix, authenticationData);
		}
	}

	public static void assertUrnPrefixServed(NodeUrnPrefix servedURNPrefix,
											 List<AuthenticationTriple> authenticationData) throws SNAAFault_Exception {
		assertAuthenticationCount(authenticationData, 1, 1);
		assertAllUrnPrefixesServed(newHashSet(servedURNPrefix), authenticationData);
	}

	public static void assertSAKUrnPrefixServed(Set<NodeUrnPrefix> urnPrefixes,
												List<SecretAuthenticationKey> authenticationData)
			throws SNAAFault_Exception {
		for (NodeUrnPrefix urnPrefix : urnPrefixes) {
			assertSAKUrnPrefixServed(urnPrefix, authenticationData);
		}
	}

	public static void assertSAKUrnPrefixServed(NodeUrnPrefix urnPrefixes,
												List<SecretAuthenticationKey> authenticationData)
			throws SNAAFault_Exception {

		assertAuthenticationKeyCount(authenticationData, 1, 1);
		assertAllUrnPrefixesInSAKsAreServed(newHashSet(urnPrefixes), authenticationData);
	}

	public static void assertAllUrnPrefixesServed(Set<NodeUrnPrefix> servedURNPrefixes,
												  List<AuthenticationTriple> authenticationData)
			throws SNAAFault_Exception {

		for (AuthenticationTriple triple : authenticationData) {
			if (!servedURNPrefixes.contains(triple.getUrnPrefix())) {
				throw createSNAAFault("Not serving node URN prefix " + triple.getUrnPrefix());
			}
		}
	}

	public static void assertAllUrnPrefixesInSAKsAreServed(Set<NodeUrnPrefix> servedURNPrefixes,
														   List<SecretAuthenticationKey> authenticationData)
			throws SNAAFault_Exception {

		for (SecretAuthenticationKey key : authenticationData) {
			if (!servedURNPrefixes.contains(key.getUrnPrefix())) {
				throw createSNAAFault("Not serving node URN prefix " + key.getUrnPrefix());
			}
		}
	}
	
	public static void assertAllNodeUrnPrefixesServed(
			Set<NodeUrnPrefix> servedNodeUrnPrefixes,
			List<NodeUrn> nodeUrns)
					throws SNAAFault_Exception{
		NodeUrnPrefix prefix;
		for (NodeUrn nodeUrn : nodeUrns) {
			prefix = nodeUrn.getPrefix();
			if (!servedNodeUrnPrefixes.contains(prefix)){
				throw createSNAAFault("Not serving node URN prefix '" +prefix+"'");
			}
		}
		
	}


	public static SNAAFault_Exception createSNAAFault(String msg, Throwable cause) {
		log.warn(msg);

		SNAAFault exception = new SNAAFault();
		exception.setMessage(msg);

		return cause != null ? new SNAAFault_Exception(msg, exception, cause) : new SNAAFault_Exception(msg, exception);
	}

	public static SNAAFault_Exception createSNAAFault(String msg) {
		return createSNAAFault(msg, null);
	}

	public static AuthenticationFault createAuthenticationFault(final String msg) {
		log.warn(msg);
		eu.wisebed.api.v3.common.AuthenticationFault faultInfo = new eu.wisebed.api.v3.common.AuthenticationFault();
		faultInfo.setMessage(msg);
		return new AuthenticationFault(msg, faultInfo);
	}

	public static void assertCollectionMinMaxCount(Collection<?> collection, int minInclusive, int maxInclusive)
			throws Exception {

		if (collection == null || collection.size() < minInclusive || collection.size() > maxInclusive) {
			throw new Exception("Invalid amount authentication data supplied (min: " + minInclusive + ", max: "
					+ maxInclusive + ")");
		}

	}

	public static void assertCollectionMinCount(Collection<?> collection, int minCountInclusive) throws Exception {
		assertCollectionMinMaxCount(collection, minCountInclusive, Integer.MAX_VALUE);
	}
}