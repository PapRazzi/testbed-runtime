package de.uniluebeck.itm.tr.snaa;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.uniluebeck.itm.util.propconf.PropConf;
import de.uniluebeck.itm.util.propconf.converters.PropertiesTypeConverter;
import de.uniluebeck.itm.util.propconf.converters.URITypeConverter;
import org.apache.shiro.crypto.hash.Sha512Hash;

import java.net.URI;
import java.util.Properties;

public class SNAAServiceConfig {

	@PropConf(
			usage = "The authentication backend of the SNAA service",
			example = "DUMMY/JAAS/SHIRO/REMOTE"
	)
	public static final String SNAA_TYPE = "snaa.type";

	@Inject
	@Named(SNAA_TYPE)
	private SNAAType snaaType;

	@PropConf(
			usage = "The URI of the remote SNAA server (only if REMOTE is used)",
			example = "http://localhost:8890/soap/v3/snaa",
			typeConverter = URITypeConverter.class
	)
	public static final String SNAA_REMOTE_URI = "snaa.remote.uri";

	@Inject(optional = true)
	@Named(SNAA_REMOTE_URI)
	private URI snaaRemoteUri;

	@PropConf(
			usage = "The login module for the JAAS backend (only if JAAS is used)",
			example = "edu.internet2.middleware.shibboleth.jaas.htpasswd.HtpasswdLoginModule"
	)
	public static final String JAAS_LOGINMODULE = "snaa.jaas.loginmodule";

	@Inject(optional = true)
	@Named(JAAS_LOGINMODULE)
	private String jaasLoginModule;

	@PropConf(
			usage = "The JAAS backends configuration file (only if JAAS is used)"
	)
	public static final String JAAS_CONFIG_FILE = "snaa.jaas.config_file";

	@Inject(optional = true)
	@Named(JAAS_CONFIG_FILE)
	private String jaasConfigFile;

	@PropConf(
			usage = "The JPA (Hibernate) .properties file for the Shiro authentication and authorization backend (only if SHIRO is used)",
			typeConverter = PropertiesTypeConverter.class
	)
	public static final String SHIRO_JPA_PROPERTIES_FILE = "snaa.shiro.jpa.properties_file";

	@Inject(optional=true)
	@Named(SHIRO_JPA_PROPERTIES_FILE)
	private Properties shiroJpaProperties;

	@PropConf(
			usage = "The name of the hash algorithm to be used with Shiro authentication and authorization backend (only if SHIRO is used)",
			example = Sha512Hash.ALGORITHM_NAME,
			defaultValue = Sha512Hash.ALGORITHM_NAME
	)
	public static final String SHIRO_HASH_ALGORITHM_NAME = "snaa.shiro.hash_algorithm.name";

	@Inject(optional=true)
	@Named(SHIRO_HASH_ALGORITHM_NAME)
	private String shiroHashAlgorithmName;

	@PropConf(
			usage = "The number of iterations to be run for the hash algorithm to be used with Shiro authentication and authorization backend (only if SHIRO is used)",
			example = "1000",
			defaultValue = "1000"
	)
	public static final String SHIRO_HASH_ALGORITHM_ITERATIONS = "snaa.shiro.hash_algorithm.iterations";

	@Inject
	@Named(SHIRO_HASH_ALGORITHM_ITERATIONS)
	private int shiroHashAlgorithmIterations;

	@PropConf(
			usage = "Comma-separated list of roles that shall be automatically assigned to users registering themselves",
			example = "EXPERIMENTER,SERVICE_PROVIDER",
			defaultValue = "EXPERIMENTER"
	)
	public static final String SHIRO_USER_REGISTRATION_AUTO_ROLES = "snaa.shiro.user_registration.auto_roles";

	@Inject
	@Named(SHIRO_USER_REGISTRATION_AUTO_ROLES)
	private String getShiroUserRegistrationAutoRoles;

	public String getGetShiroUserRegistrationAutoRoles() {
		return getShiroUserRegistrationAutoRoles;
	}

	public String getShiroHashAlgorithmName() {
		return shiroHashAlgorithmName;
	}

	public int getShiroHashAlgorithmIterations() {
		return shiroHashAlgorithmIterations;
	}

	public Properties getShiroJpaProperties() {
		return shiroJpaProperties;
	}

	public String getJaasConfigFile() {
		return jaasConfigFile;
	}

	public String getJaasLoginModule() {
		return jaasLoginModule;
	}

	public SNAAType getSnaaType() {
		return snaaType;
	}

	public URI getSnaaRemoteUri() {
		return snaaRemoteUri;
	}
}
