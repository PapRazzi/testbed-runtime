package de.uniluebeck.itm.tr.common.config;

import org.kohsuke.args4j.Option;

public abstract class Config {

	@Option(name = "--help", usage = "This help message.")
	public boolean help = false;

}
