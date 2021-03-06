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

package de.uniluebeck.itm.tr.snaa.shiro;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import de.uniluebeck.itm.servicepublisher.ServicePublisher;
import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
import de.uniluebeck.itm.tr.common.Constants;
import de.uniluebeck.itm.tr.common.ServedNodeUrnPrefixesProvider;
import de.uniluebeck.itm.tr.snaa.SNAAServiceConfig;
import de.uniluebeck.itm.tr.snaa.UserAlreadyExistsException;
import de.uniluebeck.itm.tr.snaa.UserPwdMismatchException;
import de.uniluebeck.itm.tr.snaa.UserUnknownException;
import de.uniluebeck.itm.tr.snaa.shiro.entity.Role;
import de.uniluebeck.itm.tr.snaa.shiro.entity.UrnResourceGroup;
import de.uniluebeck.itm.tr.snaa.shiro.entity.User;
import de.uniluebeck.itm.tr.snaa.shiro.rest.ShiroSNAARestService;
import de.uniluebeck.itm.util.TimedCache;
import eu.wisebed.api.v3.common.NodeUrn;
import eu.wisebed.api.v3.common.NodeUrnPrefix;
import eu.wisebed.api.v3.common.SecretAuthenticationKey;
import eu.wisebed.api.v3.common.UsernameNodeUrnsMap;
import eu.wisebed.api.v3.snaa.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static de.uniluebeck.itm.tr.snaa.common.SNAAHelper.*;

/**
 * This authentication and authorization component is responsible for
 * <ol>
 * <li>authenticating users which intend to access nodes which urns' feature a certain prefix and
 * <li>authorizing their access to the nodes.
 * <li>
 * </ol>
 * The authentication and authorization is performed for a certain set of nodes. These nodes are
 * grouped by a shared uniform resource locator prefix.
 */
@WebService(
		endpointInterface = "eu.wisebed.api.v3.snaa.SNAA",
		portName = "SNAAPort",
		serviceName = "SNAAService",
		targetNamespace = "http://wisebed.eu/api/v3/snaa/"
)
public class ShiroSNAA extends AbstractService implements de.uniluebeck.itm.tr.snaa.SNAAService {

	/**
	 * Logs messages
	 */
	private static final Logger log = LoggerFactory.getLogger(ShiroSNAA.class);

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern
			.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	/**
	 * Access authorization for users is performed for nodes which uniform resource locator starts
	 * with these prefixes.
	 */
	protected final Provider<Set<NodeUrnPrefix>> servedNodeUrnPrefixesProvider;

	/**
	 * A security component that can access application-specific security entities such as users,
	 * roles, and permissions to determine authentication and authorization operations.
	 */
	private final Realm realm;

	/**
	 * Used to generate {@link SecretAuthenticationKey}s
	 */
	private final Random r = new SecureRandom();

	/**
	 * This cache keeps tack of all authenticated sessions for a certain amount of time.<br/>
	 * It maps a secret String which is only known to one authenticated user to the user's
	 * authentication details.<br/>
	 * This cache may be used to check whether the user was authenticated by this server recently
	 * whenever this secret String is provided.
	 */
	private final TimedCache<String, AuthenticationTriple> authenticatedSessions =
			new TimedCache<String, AuthenticationTriple>(30, TimeUnit.MINUTES);

	private final ServicePublisher servicePublisher;

	private final SecurityManager securityManager;

	private final Provider<Subject> currentUserProvider;

	private final SNAAServiceConfig snaaServiceConfig;

	private final ShiroSNAARestService restService;

	private final Provider<EntityManager> emProvider;

	private final String hashAlgorithmName;

	private final int hashIterations;

	private ServicePublisherService jaxWsService;

	@Inject
	public ShiroSNAA(final Provider<EntityManager> emProvider,
					 final ServicePublisher servicePublisher,
					 final SecurityManager securityManager,
					 final ServedNodeUrnPrefixesProvider servedNodeUrnPrefixesProvider,
					 final SNAAServiceConfig snaaServiceConfig,
					 final Provider<Subject> currentUserProvider,
					 final ShiroSNAARestService restService,
					 @Named("shiro.hashAlgorithmName") final String hashAlgorithmName,
					 @Named("shiro.hashIterations") final int hashIterations) {
		this.emProvider = emProvider;
		this.currentUserProvider = currentUserProvider;
		this.servicePublisher = servicePublisher;
		this.securityManager = securityManager;
		this.servedNodeUrnPrefixesProvider = servedNodeUrnPrefixesProvider;
		this.snaaServiceConfig = snaaServiceConfig;
		this.restService = restService;
		this.hashAlgorithmName = hashAlgorithmName;
		this.hashIterations = hashIterations;

		Collection<Realm> realms = ((RealmSecurityManager) securityManager).getRealms();
		checkArgument(realms.size() == 1, "Exactly one realm must be configured");
		this.realm = realms.iterator().next();
	}

	@Override
	protected void doStart() {
		try {
			SecurityUtils.setSecurityManager(securityManager);
			jaxWsService = servicePublisher.createJaxWsService(Constants.SOAP_API_V3.SNAA_CONTEXT_PATH, this, null);
			jaxWsService.startAsync().awaitRunning();
			restService.startAsync().awaitRunning();
			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {
			if (restService != null && restService.isRunning()) {
				restService.stopAsync().awaitTerminated();
			}
			if (jaxWsService != null && jaxWsService.isRunning()) {
				jaxWsService.stopAsync().awaitTerminated();
			}
			notifyStopped();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	public AuthenticateResponse authenticate(
			@WebParam(name = "authenticate", targetNamespace = "http://wisebed.eu/api/v3/snaa", partName = "parameters")
			Authenticate parameters)
			throws AuthenticationFault, SNAAFault_Exception {

		final List<AuthenticationTriple> authenticationTriples = parameters.getAuthenticationData();
		assertAuthenticationCount(authenticationTriples, 1, 1);
		assertAllUrnPrefixesServed(servedNodeUrnPrefixesProvider.get(), authenticationTriples);

		AuthenticationTriple authenticationTriple = authenticationTriples.get(0);
		checkNotNull(authenticationTriple.getUsername(), "The user could not be authenticated: username is null.");
		checkNotNull(authenticationTriple.getPassword(), "The user could not be authenticated: password is null.");

		/* Authentication */
		Subject currentUser = currentUserProvider.get();
		try {

			final UsernamePasswordToken token = new UsernamePasswordToken(
					authenticationTriple.getUsername(),
					authenticationTriple.getPassword()
			);

			currentUser.login(token);
			currentUser.logout();

		} catch (AuthenticationException e) {
			throw createAuthenticationFault(
					"The user could not be authenticated: Wrong username and/or password."
			);
		}

		String randomLongAsString = Long.toString(r.nextLong());
		authenticatedSessions.put(randomLongAsString, authenticationTriple);

		/* Create a secret authentication key for the authenticated user */
		SecretAuthenticationKey secretAuthenticationKey = new SecretAuthenticationKey();
		secretAuthenticationKey.setUrnPrefix(authenticationTriple.getUrnPrefix());
		secretAuthenticationKey.setKey(randomLongAsString);
		secretAuthenticationKey.setUsername(authenticationTriple.getUsername());

		/* Return the single secret authentication key in a list (due to the federator) */
		final AuthenticateResponse authenticateResponse = new AuthenticateResponse();
		authenticateResponse.getSecretAuthenticationKey().add(secretAuthenticationKey);
		return authenticateResponse;
	}

	@Override
	public List<ValidationResult> isValid(final List<SecretAuthenticationKey> secretAuthenticationKeys)
			throws SNAAFault_Exception {

		// check whether the urn prefix associated to the key is served at all
		assertAllUrnPrefixesInSAKsAreServed(servedNodeUrnPrefixesProvider.get(), secretAuthenticationKeys);

		final SecretAuthenticationKey secretAuthenticationKey = secretAuthenticationKeys.get(0);

		// Get the session from the cache of authenticated sessions
		AuthenticationTriple authTriple = authenticatedSessions.get(secretAuthenticationKey.getKey());

		ValidationResult result = new ValidationResult();

		if (authTriple == null) {
			result.setValid(false);
			result.setMessage("The provided secret authentication key is not found. It is either invalid or expired.");
		} else if (secretAuthenticationKey.getUsername() == null) {
			result.setValid(false);
			result.setMessage("The user name comprised in the secret authentication key must not be 'null'.");
		} else if (!secretAuthenticationKey.getUsername().equals(authTriple.getUsername())) {
			result.setValid(false);
			result.setMessage(
					"The user name which was provided by the original authentication does not match the one in the secret authentication key."
			);
		} else if (!secretAuthenticationKey.getUrnPrefix().equals(authTriple.getUrnPrefix())) {
			result.setValid(false);
			result.setMessage(
					"The urn prefix which was provided by the original authentication does not match the one in the secret authentication key."
			);
		} else {
			result.setValid(true);
		}

		return newArrayList(result);
	}

	@Override
	public AuthorizationResponse isAuthorized(
			@WebParam(name = "usernameNodeUrnsMapList", targetNamespace = "")
			List<UsernameNodeUrnsMap> usernameNodeUrnsMaps,
			@WebParam(name = "action", targetNamespace = "")
			Action action)
			throws SNAAFault_Exception {

		checkNotNull(action, "action parameter must be non-null (one of " + Joiner.on(", ").join(Action.values()) + ")"
		);
		checkArgument(usernameNodeUrnsMaps.size() == 1,
				"The number of username and node urn mappings must be 1 but is " + usernameNodeUrnsMaps.size()
		);

		UsernameNodeUrnsMap usernameNodeUrnsMapping = usernameNodeUrnsMaps.get(0);
		String userName = usernameNodeUrnsMapping.getUsername();

		checkArgument(
				servedNodeUrnPrefixesProvider.get().contains(usernameNodeUrnsMapping.getUrnPrefix()),
				"The prefix provided along with the user is not served!"
		);

		assertAllNodeUrnPrefixesServed(servedNodeUrnPrefixesProvider.get(), usernameNodeUrnsMapping.getNodeUrns());

		if (!((ShiroSNAAJPARealm) realm).doesUserExist(userName)) {
			AuthorizationResponse authorizationResponse = new AuthorizationResponse();
			authorizationResponse.setAuthorized(false);
			authorizationResponse.setMessage("User \"" + userName + "\" is unknown!");
			return authorizationResponse;
		}

		PrincipalCollection principals = new SimplePrincipalCollection(userName, realm.getName());
		Subject subject = new Subject.Builder().principals(principals).buildSubject();

		Set<String> nodeGroups = getNodeGroupsForNodeURNs(usernameNodeUrnsMapping.getNodeUrns());

		AuthorizationResponse authorizationResponse = new AuthorizationResponse();
		StringBuilder reason = new StringBuilder();
		boolean allAuthorized = true;
		for (String nodeGroup : nodeGroups) {
			if (!subject.isPermittedAll(action.name() + ":" + nodeGroup)) {
				authorizationResponse.setAuthorized(false);
				allAuthorized = false;
				reason.append("The action '")
						.append(action.name())
						.append("' is not allowed for node group '")
						.append(nodeGroup)
						.append("' and user '")
						.append(userName)
						.append("'. ");
			}
		}
		authorizationResponse.setAuthorized(allAuthorized);
		subject.logout();

		if (!authorizationResponse.isAuthorized()) {
			authorizationResponse.setMessage(reason.toString());
			log.debug("User requested unauthorized action(s): " + reason.toString());
		} else {
			log.debug("The requested actions were authorized successfully.");
		}

		return authorizationResponse;
	}

	/**
	 * Iterates over a collection or node urns and returns the groups of these nodes.
	 *
	 * @param nodeUrns
	 * 		A collection of node urns
	 *
	 * @return A set of those groups at least one of the provided node belongs to
	 *
	 * @throws SNAAFault_Exception
	 * 		Thrown if the provided collection of node urns contains node urns with prefixes which
	 * 		are not served by this SNAA server
	 */
	@Transactional
	protected Set<String> getNodeGroupsForNodeURNs(final Collection<NodeUrn> nodeUrns) throws SNAAFault_Exception {

		assertAllNodeUrnPrefixesServed(servedNodeUrnPrefixesProvider.get(), Lists.newLinkedList(nodeUrns));

		final Set<String> nodeGroups = new HashSet<String>();
		final List<String> nodeUrnStringList = new ArrayList<String>();
		for (NodeUrn nodeUrn : nodeUrns) {
			nodeUrnStringList.add(nodeUrn.toString());
		}

		final EntityManager em = emProvider.get();
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<UrnResourceGroup> query = criteriaBuilder.createQuery(UrnResourceGroup.class);
		query.from(UrnResourceGroup.class);
		final List<UrnResourceGroup> nodeUrnResourceGroup = em.createQuery(query).getResultList();

		for (UrnResourceGroup grp : nodeUrnResourceGroup) {
			if (nodeUrnStringList.contains(grp.getId().getUrn())) {
				nodeGroups.add(grp.getId().getResourcegroup());
			}
		}

		return nodeGroups;
	}

	@Override
	public boolean isUserRegistrationSupported() {
		return true;
	}

	@Override
	@Transactional
	public void add(final String email, final String password) throws UserAlreadyExistsException {

		checkNotNull(email, "User email may not be null");
		checkNotNull(email, "User password may not be null");
		checkArgument(VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches(), "User email is not a valid email address");
		checkArgument(password.length() >= 6, "User password needs to be at least 6 characters long");

		final EntityManager em = emProvider.get();

		if (em.find(User.class, email) != null) {
			throw new UserAlreadyExistsException(email);
		}

		final Set<Role> autoRoles = newHashSet();
		final Iterable<String> autoRoleStrings = Splitter.on(",").trimResults().omitEmptyStrings().split(
				snaaServiceConfig.getGetShiroUserRegistrationAutoRoles()
		);

		for (String autoRoleString : autoRoleStrings) {
			Role autoRole = em.find(Role.class, autoRoleString);
			if (autoRole == null) {
				autoRole = new Role(autoRoleString);
				em.persist(autoRole);
			}
			autoRoles.add(autoRole);
		}

		final String salt = new SecureRandomNumberGenerator().nextBytes().toHex();
		final String hash = new SimpleHash(hashAlgorithmName, password, salt, hashIterations).toHex();
		final User newUser = new User(email, hash, salt, autoRoles);

		em.persist(newUser);
	}

	@Override
	@Transactional
	public void update(final String email, final String oldPassword, final String newPassword)
			throws UserUnknownException, UserPwdMismatchException {

		final User user = getUserAndAssurePwdMatches(email, oldPassword);

		final String newSalt = new SecureRandomNumberGenerator().nextBytes().toHex();
		final String newHash = new SimpleHash(hashAlgorithmName, newPassword, newSalt, hashIterations).toHex();

		user.setPassword(newHash);
		user.setSalt(newSalt);
	}

	@Override
	@Transactional
	public void delete(final String email, final String password)
			throws UserUnknownException, UserPwdMismatchException {

		final User user = getUserAndAssurePwdMatches(email, password);
		emProvider.get().remove(user);
	}

	private User getUserAndAssurePwdMatches(final String email, final String password)
			throws UserUnknownException, UserPwdMismatchException {

		final EntityManager em = emProvider.get();
		final User user = em.find(User.class, email);

		if (user == null) {
			throw new UserUnknownException(email);
		}

		final String oldSalt = user.getSalt();
		final String oldHash = new SimpleHash(hashAlgorithmName, password, oldSalt, hashIterations).toHex();

		if (!oldHash.equals(user.getPassword())) {
			throw new UserPwdMismatchException(email);
		}

		return user;
	}
}
