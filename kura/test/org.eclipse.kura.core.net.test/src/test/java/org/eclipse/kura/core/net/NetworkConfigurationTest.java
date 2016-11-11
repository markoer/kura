/*******************************************************************************
 * Copyright (c) 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.kura.core.net;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.net.modem.ModemInterfaceAddressConfigImpl;
import org.eclipse.kura.core.net.modem.ModemInterfaceConfigImpl;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetConfigIP4;
import org.eclipse.kura.net.NetInterfaceAddressConfig;
import org.eclipse.kura.net.NetInterfaceState;
import org.eclipse.kura.net.NetInterfaceStatus;
import org.eclipse.kura.net.NetInterfaceType;
import org.eclipse.kura.net.modem.ModemInterfaceAddressConfig;
import org.eclipse.kura.net.wifi.WifiInterfaceAddressConfig;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NetworkConfigurationTest {

	@Test
	public void testNetworkConfiguration() {
		NetworkConfiguration config = new NetworkConfiguration();
		
		assertTrue(config.getNetInterfaceConfigs().isEmpty());
	}

	@Test
	public void testNetworkConfigurationEmpty() {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			
			NetworkConfiguration config = new NetworkConfiguration(properties);

			assertTrue(config.getNetInterfaceConfigs().isEmpty());
			assertTrue((Boolean) TestUtil.getFieldValue(config, "m_recomputeProperties"));
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testNetworkConfigurationWithAvailableInterfaces() {
		try {
			String[] interfaces = new String[] {"if1", "if2"};
			
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("net.interfaces", interfaces);
			properties.put("net.interface.if1.type", "ETHERNET");
			
			NetworkConfiguration config = new NetworkConfiguration(properties);

			assertEquals(1, config.getNetInterfaceConfigs().size());

			List<NetConfig> configs = new ArrayList<NetConfig>();
			configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
			
			NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();
			addressConfig.setNetConfigs(configs);
			
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(addressConfig);
			
			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setState(NetInterfaceState.DISCONNECTED);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
			
 			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
			assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testNetworkConfigurationWithAvailableInterfacesGWT() {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("net.interfaces", "if1,if2");
			properties.put("net.interface.if1.type", "ETHERNET");
			
			NetworkConfiguration config = new NetworkConfiguration(properties);

			assertEquals(1, config.getNetInterfaceConfigs().size());

			List<NetConfig> configs = new ArrayList<NetConfig>();
			configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
			
			NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();
			addressConfig.setNetConfigs(configs);
			
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(addressConfig);
			
			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setState(NetInterfaceState.DISCONNECTED);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
			
 			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
 			assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testNetworkConfigurationWithModifiedInterfaceNames() {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("modified.interface.names", "if1,if2");
			
			NetworkConfiguration config = new NetworkConfiguration(properties);

			assertEquals(2, config.getModifiedInterfaceNames().size());
			assertEquals("if1", config.getModifiedInterfaceNames().get(0));
			assertEquals("if2", config.getModifiedInterfaceNames().get(1));
			assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testModifiedInterfaceNames() {
		NetworkConfiguration config = new NetworkConfiguration();

		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		
		List<String> modifiedInterfaceNames = new ArrayList<String>();
		modifiedInterfaceNames.add("if1");
		modifiedInterfaceNames.add("if2");
		config.setModifiedInterfaceNames(modifiedInterfaceNames);
		assertEquals(modifiedInterfaceNames, config.getModifiedInterfaceNames());
		assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		
		modifiedInterfaceNames.clear();
		modifiedInterfaceNames.add("if3");
		config.setModifiedInterfaceNames(modifiedInterfaceNames);
		assertEquals(modifiedInterfaceNames, config.getModifiedInterfaceNames());
		assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
	}

	@Test
	public void testModifiedInterfaceNamesNull() {
		NetworkConfiguration config = new NetworkConfiguration();

		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		
		List<String> modifiedInterfaceNames = null;
		config.setModifiedInterfaceNames(modifiedInterfaceNames);
		assertNull(config.getModifiedInterfaceNames());
		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
	}

	@Test
	public void testModifiedInterfaceNamesEmpty() {
		NetworkConfiguration config = new NetworkConfiguration();

		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		
		List<String> modifiedInterfaceNames = new ArrayList<String>();
		config.setModifiedInterfaceNames(modifiedInterfaceNames);
		assertNull(config.getModifiedInterfaceNames());
		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
	}
	
	@Test
	public void testAccept() {
		NetworkConfiguration config = new NetworkConfiguration();
		NetworkConfigurationVisitor visitor = mock(NetworkConfigurationVisitor.class);
		
		try {
			Mockito.doNothing().when(visitor).visit(any(NetworkConfiguration.class));

			config.accept(visitor);
			
			verify(visitor, times(1)).visit(config);
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAcceptWithException() {
		NetworkConfiguration config = new NetworkConfiguration();
		NetworkConfigurationVisitor visitor = mock(NetworkConfigurationVisitor.class);
		
		try {
			Mockito.doThrow(new KuraException(KuraErrorCode.INTERNAL_ERROR)).when(visitor).visit(any(NetworkConfiguration.class));

			config.accept(visitor);
			fail("exception was expected");
		} catch (KuraException e) {
			assertEquals(KuraErrorCode.INTERNAL_ERROR, e.getCode());
		}
	}
	
	@Test
	public void testAddNetInterfaceConfig() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		config.addNetInterfaceConfig(interfaceConfig);

		assertEquals(1, config.getNetInterfaceConfigs().size());
		assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
		assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
	}

	@Test
	public void testAddNetConfigLoopbackEmpty() {
		try {
			// Prepare network configuration
			NetworkConfiguration config = new NetworkConfiguration();

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.LOOPBACK, netConfig);
			
			// Check for success
			assertTrue(config.getNetInterfaceConfigs().isEmpty());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigLoopback() {
		try {
			// Prepare network configuration
			NetInterfaceAddressConfigImpl interfaceAddressConfig = new NetInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(new ArrayList<NetConfig>());
			
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			
			LoopbackInterfaceConfigImpl interfaceConfig = new LoopbackInterfaceConfigImpl("if1");
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);

			NetworkConfiguration config = new NetworkConfiguration();
			config.addNetInterfaceConfig(interfaceConfig);

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.ETHERNET, netConfig);
			
			// Check for success
			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			netConfigs.add(netConfig);
			
			interfaceAddressConfig = new NetInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(netConfigs);
			
			interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
			
			assertEquals(1, config.getNetInterfaceConfigs().size());
			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}
	
	@Test
	public void testAddNetConfigEthernetEmpty() {
		try {
			// Prepare network configuration
			NetworkConfiguration config = new NetworkConfiguration();

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.ETHERNET, netConfig);
			
			// Check for success
			assertTrue(config.getNetInterfaceConfigs().isEmpty());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigEthernet() {
		try {
			// Prepare network configuration
			NetInterfaceAddressConfigImpl interfaceAddressConfig = new NetInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(new ArrayList<NetConfig>());
			
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			
			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);

			NetworkConfiguration config = new NetworkConfiguration();
			config.addNetInterfaceConfig(interfaceConfig);

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.ETHERNET, netConfig);
			
			// Check for success
			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			netConfigs.add(netConfig);
			
			interfaceAddressConfig = new NetInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(netConfigs);
			
			interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
			
			assertEquals(1, config.getNetInterfaceConfigs().size());
			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}
	
	@Test
	public void testAddNetConfigWifiEmpty() {
		try {
			// Prepare network configuration
			NetworkConfiguration config = new NetworkConfiguration();

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.WIFI, netConfig);
			
			// Check for success
			assertTrue(config.getNetInterfaceConfigs().isEmpty());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigWifi() {
		try {
			// Prepare network configuration
			WifiInterfaceAddressConfigImpl interfaceAddressConfig = new WifiInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(new ArrayList<NetConfig>());
			
			List<WifiInterfaceAddressConfig> interfaceAddresses = new ArrayList<WifiInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			
			WifiInterfaceConfigImpl interfaceConfig = new WifiInterfaceConfigImpl("if1");
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);

			NetworkConfiguration config = new NetworkConfiguration();
			config.addNetInterfaceConfig(interfaceConfig);

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.WIFI, netConfig);
			
			// Check for success
			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			netConfigs.add(netConfig);
			
			interfaceAddressConfig = new WifiInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(netConfigs);
			
			interfaceAddresses = new ArrayList<WifiInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
			
			assertEquals(1, config.getNetInterfaceConfigs().size());
			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigModemEmpty() {
		try {
			// Prepare network configuration
			NetworkConfiguration config = new NetworkConfiguration();

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.MODEM, netConfig);
			
			// Check for success
			assertTrue(config.getNetInterfaceConfigs().isEmpty());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigModem() {
		try {
			// Prepare network configuration
			ModemInterfaceAddressConfigImpl interfaceAddressConfig = new ModemInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(new ArrayList<NetConfig>());
			
			List<ModemInterfaceAddressConfig> interfaceAddresses = new ArrayList<ModemInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			
			ModemInterfaceConfigImpl interfaceConfig = new ModemInterfaceConfigImpl("if1");
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);

			NetworkConfiguration config = new NetworkConfiguration();
			config.addNetInterfaceConfig(interfaceConfig);

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.MODEM, netConfig);
			
			// Check for success
			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			netConfigs.add(netConfig);
			
			interfaceAddressConfig = new ModemInterfaceAddressConfigImpl();
			interfaceAddressConfig.setNetConfigs(netConfigs);
			
			interfaceAddresses = new ArrayList<ModemInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
			
			assertEquals(1, config.getNetInterfaceConfigs().size());
			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigInvalidType() {
		try {
			// Prepare network configuration
			NetworkConfiguration config = new NetworkConfiguration();

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.ADSL, netConfig);
			fail("NullPointerException was expected");
		} catch (NullPointerException e) {
			// Success
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testAddNetConfigUnsupportedAddressConfig() {
		try {
			// Prepare network configuration
			MockInterfaceAddressConfig interfaceAddressConfig = new MockInterfaceAddressConfig();
			interfaceAddressConfig.setNetConfigs(new ArrayList<NetConfig>());
			
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(interfaceAddressConfig);
			
			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);

			NetworkConfiguration config = new NetworkConfiguration();
			config.addNetInterfaceConfig(interfaceConfig);

			// Add net config
			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true);
			
			config.addNetConfig("if1", NetInterfaceType.ETHERNET, netConfig);
			
			// Check for success
			assertEquals(1, config.getNetInterfaceConfigs().size());
			assertEquals(interfaceConfig, config.getNetInterfaceConfigs().get(0));
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

//	@Test
//	public void testToString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetModifiedNetInterfaceConfigs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNetInterfaceConfigs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNetInterfaceConfig() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetConfigurationProperties() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsValid() {
//		fail("Not yet implemented");
//	}
}
