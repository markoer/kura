package org.eclipse.kura.core.configuration;

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.kura.configuration.ConfigurationService;
import org.junit.Test;

public class ConfigurationServiceTest {

	@Test
	public void testGetFactoryComponentPids() {
		ConfigurationService cs = new ConfigurationServiceImpl();
		
		Set<String> factoryComponentPids = cs.getFactoryComponentPids();
	}

	@Test
	public void testCreateFactoryConfiguration() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteFactoryConfiguration() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConfigurableComponentPids() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetComponentConfigurations() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetComponentConfiguration() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDefaultComponentConfiguration() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateConfigurationStringMapOfStringObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateConfigurationStringMapOfStringObjectBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateConfigurationsListOfComponentConfiguration() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateConfigurationsListOfComponentConfigurationBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshots() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshot() {
		fail("Not yet implemented");
	}

	@Test
	public void testSnapshot() {
		fail("Not yet implemented");
	}

	@Test
	public void testRollback() {
		fail("Not yet implemented");
	}

	@Test
	public void testRollbackLong() {
		fail("Not yet implemented");
	}

}
