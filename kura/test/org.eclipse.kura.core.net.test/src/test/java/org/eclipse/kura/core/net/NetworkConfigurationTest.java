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
import org.eclipse.kura.core.net.util.NetworkUtil;
import org.eclipse.kura.core.testutil.TestUtil;
import org.eclipse.kura.net.IP4Address;
import org.eclipse.kura.net.IP6Address;
import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetConfigIP4;
import org.eclipse.kura.net.NetConfigIP6;
import org.eclipse.kura.net.NetInterfaceAddressConfig;
import org.eclipse.kura.net.NetInterfaceConfig;
import org.eclipse.kura.net.NetInterfaceState;
import org.eclipse.kura.net.NetInterfaceStatus;
import org.eclipse.kura.net.NetInterfaceType;
import org.eclipse.kura.net.dhcp.DhcpServerConfigIP4;
import org.eclipse.kura.net.firewall.FirewallAutoNatConfig;
import org.eclipse.kura.net.modem.ModemConfig;
import org.eclipse.kura.net.modem.ModemConfig.AuthType;
import org.eclipse.kura.net.modem.ModemConfig.PdpType;
import org.eclipse.kura.net.modem.ModemInterfaceAddressConfig;
import org.eclipse.kura.net.wifi.WifiConfig;
import org.eclipse.kura.net.wifi.WifiInterfaceAddressConfig;
import org.eclipse.kura.usb.UsbBlockDevice;
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
			assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
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
			MockInterfaceAddressConfigImpl interfaceAddressConfig = new MockInterfaceAddressConfigImpl();
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

	@Test
	public void testToStringEmpty() {
		NetworkConfiguration config = new NetworkConfiguration();
		
		String expected = "";
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringNull() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
		NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();
		addressConfig.setNetConfigs(null);
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		config.addNetInterfaceConfig(interfaceConfig);
		
		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false :: Virtual? false"
				+ " :: Driver: null :: Driver Version: null :: Firmware Version: null :: MTU: 0 :: State: null"
				+ " :: Type: ETHERNET :: Usb Device: null :: Prefix: 0";
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringEthernet1() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		interfaceConfig.setDriver("driver");
		interfaceConfig.setDriverVersion("driverVersion");
		interfaceConfig.setFirmwareVersion("firmwareVersion");
		interfaceConfig.setState(NetInterfaceState.ACTIVATED);
		interfaceConfig.setUsbDevice(new UsbBlockDevice("vendorId", "productId", "manufacturerName",
				"productName", "usbBusNumber", "usbDevicePath", "deviceNode"));

		List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
		NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();
		
		List<NetConfig> netConfigs = new ArrayList<NetConfig>();
		netConfigs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		addressConfig.setNetConfigs(netConfigs);
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: driver :: Driver Version: driverVersion"
				+ " :: Firmware Version: firmwareVersion :: MTU: 0 :: State: ACTIVATED :: Type: ETHERNET"
				+ " :: Usb Device: UsbBlockDevice [getDeviceNode()=deviceNode, getVendorId()=vendorId,"
				+ " getProductId()=productId, getManufacturerName()=manufacturerName, getProductName()=productName,"
				+ " getUsbBusNumber()=usbBusNumber, getUsbDevicePath()=usbDevicePath,"
				+ " getUsbPort()=usbBusNumber-usbDevicePath] :: Prefix: 0"
				+ "\n	IPv4  :: is not configured for STATIC or DHCP";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringEthernet2() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		
		try {
			interfaceConfig.setHardwareAddress(NetworkUtil.macToBytes("11:22:33:44:55:66"));
			
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();
			addressConfig.setAddress(IP4Address.parseHostAddress("10.0.0.1"));
			addressConfig.setNetmask(IP4Address.parseHostAddress("255.255.255.0"));
			addressConfig.setBroadcast(IP4Address.parseHostAddress("10.0.0.255"));

			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			NetConfigIP4 netConfig1 = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false);
			netConfig1.setDhcp(true);
			netConfigs.add(netConfig1);

			NetConfigIP4 netConfig2 = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false);
			netConfig1.setDhcp(true);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put("key", "value");
			netConfig1.setProperties(properties);
			netConfigs.add(netConfig2);
			
			NetConfigIP4 netConfig3 = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false);
			netConfig3.setAddress(null);
			netConfigs.add(netConfig3);

			NetConfigIP4 netConfig4 = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false);
			netConfig4.setAddress((IP4Address) IP4Address.parseHostAddress("10.0.0.2"));
			netConfig4.setNetworkPrefixLength((short) 24);
			netConfig4.setDnsServers(null);
			netConfig4.setWinsServers(null);
			netConfig4.setDomains(null);
			netConfigs.add(netConfig4);

			NetConfigIP4 netConfig5 = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false);
			netConfig5.setAddress((IP4Address) IP4Address.parseHostAddress("10.0.0.2"));
			netConfig5.setNetworkPrefixLength((short) 24);
			netConfig5.setGateway((IP4Address) IP4Address.parseHostAddress("10.0.0.1"));
			
			List<IP4Address> dnsServers = new ArrayList<IP4Address>();
			dnsServers.add((IP4Address) IP4Address.parseHostAddress("10.0.1.1"));
			netConfig5.setDnsServers(dnsServers);

			List<IP4Address> winsServers = new ArrayList<IP4Address>();
			winsServers.add((IP4Address) IP4Address.parseHostAddress("10.0.1.1"));
			netConfig5.setWinsServers(winsServers);

			List<String> domains = new ArrayList<String>();
			domains.add("example.com");
			netConfig5.setDomains(domains);
			
			netConfigs.add(netConfig5);
			
			addressConfig.setNetConfigs(netConfigs);
			
			interfaceAddresses.add(addressConfig);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: null :: Driver Version: null :: Firmware Version: null"
				+ " :: MTU: 0 :: Hardware Address: 11:22:33:44:55:66 :: State: null :: Type: ETHERNET"
				+ " :: Usb Device: null :: Address: 10.0.0.1 :: Prefix: 0 :: Netmask: 255.255.255.0"
				+ " :: Broadcast: 10.0.0.255"
				+ "\n	IPv4  :: is DHCP client :: key: value"
				+ "\n	IPv4  :: is not configured for STATIC or DHCP"
				+ "\n	IPv4  :: is not configured for STATIC or DHCP"
				+ "\n	IPv4  :: is STATIC client :: Address: 10.0.0.2 :: Prefix: 24"
				+ "\n	IPv4  :: is STATIC client :: Address: 10.0.0.2 :: Prefix: 24"
				+ " :: Gateway: 10.0.0.1 :: DNS : 10.0.1.1 :: WINS Server : 10.0.1.1 :: Domains : example.com";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringEthernet3() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		
		try {
			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();

			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			NetConfigIP6 netConfig1 = new NetConfigIP6(NetInterfaceStatus.netIPv6StatusDisabled, false);
			netConfig1.setDhcp(true);
			netConfigs.add(netConfig1);

			NetConfigIP6 netConfig2 = new NetConfigIP6(NetInterfaceStatus.netIPv6StatusDisabled, false);
			netConfig1.setDhcp(true);
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put("key", "value");
			netConfig1.setProperties(properties);
			netConfigs.add(netConfig2);
			
			NetConfigIP6 netConfig3 = new NetConfigIP6(NetInterfaceStatus.netIPv6StatusDisabled, false);
			netConfig3.setAddress(null);
			netConfigs.add(netConfig3);

			NetConfigIP6 netConfig4 = new NetConfigIP6(NetInterfaceStatus.netIPv6StatusDisabled, false);
			netConfig4.setAddress((IP6Address) IP6Address.parseHostAddress("0:0:0:0:0:0:0:1"));
			
			List<IP6Address> dnsServers = new ArrayList<IP6Address>();
			dnsServers.add((IP6Address) IP6Address.parseHostAddress("0:0:0:0:0:0:0:1"));
			netConfig4.setDnsServers(dnsServers);

			List<String> domains = new ArrayList<String>();
			domains.add("example.com");
			netConfig4.setDomains(domains);
			
			netConfigs.add(netConfig4);
			
			addressConfig.setNetConfigs(netConfigs);
			
			interfaceAddresses.add(addressConfig);
			interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: null :: Driver Version: null :: Firmware Version: null"
				+ " :: MTU: 0 :: State: null :: Type: ETHERNET :: Usb Device: null :: Prefix: 0"
				+ "\n	IPv6  :: is DHCP client :: key: value"
				+ "\n	IPv6  :: is STATIC client"
				+ "\n	IPv6  :: is STATIC client"
				+ "\n	IPv6  :: is STATIC client :: Address: 0:0:0:0:0:0:0:1 :: DNS : 0:0:0:0:0:0:0:1"
				+ " :: Domains : example.com";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringWifi1() {
		NetworkConfiguration config = new NetworkConfiguration();

		WifiInterfaceConfigImpl interfaceConfig = new WifiInterfaceConfigImpl("if1");
		
		List<WifiInterfaceAddressConfig> interfaceAddresses = new ArrayList<WifiInterfaceAddressConfig>();
		WifiInterfaceAddressConfigImpl addressConfig = new WifiInterfaceAddressConfigImpl();
		
		List<NetConfig> netConfigs = new ArrayList<NetConfig>();
		WifiConfig wifiConfig = new WifiConfig();
		wifiConfig.setChannels(null);
		netConfigs.add(wifiConfig);
		addressConfig.setNetConfigs(netConfigs);
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: null :: Driver Version: null :: Firmware Version: null"
				+ " :: MTU: 0 :: State: null :: Type: WIFI :: Usb Device: null :: Prefix: 0"
				+ "\n	WifiConfig  :: SSID: null :: BgScan: null :: Broadcast: false :: Group Ciphers: null"
				+ " :: Hardware Mode: null :: Mode: null :: Pairwise Ciphers: null :: Passkey: null"
				+ " :: Security: null";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringWifi2() {
		NetworkConfiguration config = new NetworkConfiguration();

		WifiInterfaceConfigImpl interfaceConfig = new WifiInterfaceConfigImpl("if1");
		
		List<WifiInterfaceAddressConfig> interfaceAddresses = new ArrayList<WifiInterfaceAddressConfig>();
		WifiInterfaceAddressConfigImpl addressConfig = new WifiInterfaceAddressConfigImpl();
		
		List<NetConfig> netConfigs = new ArrayList<NetConfig>();
		WifiConfig wifiConfig = new WifiConfig();
		wifiConfig.setChannels(new int[0]);
		netConfigs.add(wifiConfig);
		addressConfig.setNetConfigs(netConfigs);
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: null :: Driver Version: null :: Firmware Version: null"
				+ " :: MTU: 0 :: State: null :: Type: WIFI :: Usb Device: null :: Prefix: 0"
				+ "\n	WifiConfig  :: SSID: null :: BgScan: null :: Broadcast: false :: Group Ciphers: null"
				+ " :: Hardware Mode: null :: Mode: null :: Pairwise Ciphers: null :: Passkey: null"
				+ " :: Security: null";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringWifi3() {
		NetworkConfiguration config = new NetworkConfiguration();

		WifiInterfaceConfigImpl interfaceConfig = new WifiInterfaceConfigImpl("if1");
		
		List<WifiInterfaceAddressConfig> interfaceAddresses = new ArrayList<WifiInterfaceAddressConfig>();
		WifiInterfaceAddressConfigImpl addressConfig = new WifiInterfaceAddressConfigImpl();
		
		List<NetConfig> netConfigs = new ArrayList<NetConfig>();
		WifiConfig wifiConfig = new WifiConfig();
		wifiConfig.setChannels(new int[] { 1, 2 });
		netConfigs.add(wifiConfig);
		addressConfig.setNetConfigs(netConfigs);
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: null :: Driver Version: null :: Firmware Version: null"
				+ " :: MTU: 0 :: State: null :: Type: WIFI :: Usb Device: null :: Prefix: 0"
				+ "\n	WifiConfig  :: SSID: null :: BgScan: null :: Broadcast: false1,2 :: Group Ciphers: null"
				+ " :: Hardware Mode: null :: Mode: null :: Pairwise Ciphers: null :: Passkey: null"
				+ " :: Security: null";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringModem() {
		NetworkConfiguration config = new NetworkConfiguration();

		ModemInterfaceConfigImpl interfaceConfig = new ModemInterfaceConfigImpl("if1");
		
		List<ModemInterfaceAddressConfig> interfaceAddresses = new ArrayList<ModemInterfaceAddressConfig>();
		ModemInterfaceAddressConfigImpl addressConfig = new ModemInterfaceAddressConfigImpl();
		
		try {
			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			ModemConfig modemConfig = new ModemConfig();
			modemConfig.setApn("apn");
			modemConfig.setDataCompression(0);
			modemConfig.setDialString("dialString");
			modemConfig.setHeaderCompression(0);
			modemConfig.setPassword("password");
			modemConfig.setPppNumber(0);
			modemConfig.setProfileID(0);
			modemConfig.setUsername("username");
			modemConfig.setAuthType(AuthType.AUTO);
			modemConfig.setIpAddress((IP4Address) IP4Address.parseHostAddress("10.0.0.2"));
			modemConfig.setPdpType(PdpType.PPP);
			netConfigs.add(modemConfig);
			addressConfig.setNetConfigs(netConfigs);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		}
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false"
				+ " :: Virtual? false :: Driver: null :: Driver Version: null :: Firmware Version: null"
				+ " :: MTU: 0 :: State: null :: Type: MODEM :: Usb Device: null :: Prefix: 0"
				+ "\n	ModemConfig  :: APN: apn :: Data Compression: 0 :: Dial String: dialString"
				+ " :: Header Compression: 0 :: Password: password :: PPP number: 0 :: Profile ID: 0"
				+ " :: Username: username :: Auth Type: AUTO :: IP Address: 10.0.0.2 :: PDP Type: PPP";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testToStringOther() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		
		List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
		NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();

		List<NetConfig> netConfigs = new ArrayList<NetConfig>();
		netConfigs.add(new DhcpServerConfigIP4(null, false, null, null, null, 0, 0, (short) 0,
				null, null, false, null));
		netConfigs.add(new FirewallAutoNatConfig());
		netConfigs.add(null);
		netConfigs.add(new MockConfig());
		
		addressConfig.setNetConfigs(netConfigs);
		
		interfaceAddresses.add(addressConfig);
		interfaceConfig.setNetInterfaceAddresses(interfaceAddresses);
		
		config.addNetInterfaceConfig(interfaceConfig);

		String expected = "\nname: if1 :: Loopback? false :: Point to Point? false :: Up? false :: Virtual? false"
				+ " :: Driver: null :: Driver Version: null :: Firmware Version: null :: MTU: 0 :: State: null"
				+ " :: Type: ETHERNET :: Usb Device: null :: Prefix: 0"
				+ "\n	DhcpServerConfig "
				+ "\n	FirewallAutoNatConfig "
				+ "\n	NULL NETCONFIG PRESENT?!?"
				+ "\n	UNKNOWN CONFIG TYPE???: org.eclipse.kura.core.net.MockConfig";
		
		assertEquals(expected, config.toString());
	}

	@Test
	public void testGetModifiedNetInterfaceConfigsNull() {
		// Prepare configuration
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		config.addNetInterfaceConfig(interfaceConfig);
		
		// Get modified net interface configs (all net interface configs are expected)
		List<NetInterfaceConfig<? extends NetInterfaceAddressConfig>> modifiedNetInterfaceConfigs =
				config.getModifiedNetInterfaceConfigs();
		
		assertEquals(1, modifiedNetInterfaceConfigs.size());
		assertEquals(interfaceConfig, modifiedNetInterfaceConfigs.get(0));
	}

	@Test
	public void testGetModifiedNetInterfaceConfigsEmpty() {
		try {
			// Prepare configuration
			NetworkConfiguration config = new NetworkConfiguration(new HashMap<String, Object>());
			
			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			config.addNetInterfaceConfig(interfaceConfig);

			// Get modified net interface configs (all net interface configs are expected)
			List<NetInterfaceConfig<? extends NetInterfaceAddressConfig>> modifiedNetInterfaceConfigs =
					config.getModifiedNetInterfaceConfigs();
			
			assertEquals(1, modifiedNetInterfaceConfigs.size());
			assertEquals(interfaceConfig, modifiedNetInterfaceConfigs.get(0));
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testGetModifiedNetInterfaceConfigsNonEmpty1() {
		try {
			// Prepare configuration
			NetworkConfiguration config = new NetworkConfiguration(new HashMap<String, Object>());
			
			EthernetInterfaceConfigImpl interfaceConfig1 = new EthernetInterfaceConfigImpl("if1");
			config.addNetInterfaceConfig(interfaceConfig1);

			List<String> modifiedInterfaceNames = new ArrayList<String>();
			modifiedInterfaceNames.add("if2");
			config.setModifiedInterfaceNames(modifiedInterfaceNames);
			
			// Get modified net interface configs (all net interface configs are expected)
			List<NetInterfaceConfig<? extends NetInterfaceAddressConfig>> modifiedNetInterfaceConfigs =
					config.getModifiedNetInterfaceConfigs();
			
			assertEquals(0, modifiedNetInterfaceConfigs.size());
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testGetModifiedNetInterfaceConfigsNonEmpty2() {
		try {
			// Prepare configuration
			NetworkConfiguration config = new NetworkConfiguration(new HashMap<String, Object>());
			
			EthernetInterfaceConfigImpl interfaceConfig1 = new EthernetInterfaceConfigImpl("if1");
			config.addNetInterfaceConfig(interfaceConfig1);

			EthernetInterfaceConfigImpl interfaceConfig2 = new EthernetInterfaceConfigImpl("if2");
			config.addNetInterfaceConfig(interfaceConfig2);
			
			List<String> modifiedInterfaceNames = new ArrayList<String>();
			modifiedInterfaceNames.add("if2");
			config.setModifiedInterfaceNames(modifiedInterfaceNames);
			
			// Get modified net interface configs (all net interface configs are expected)
			List<NetInterfaceConfig<? extends NetInterfaceAddressConfig>> modifiedNetInterfaceConfigs =
					config.getModifiedNetInterfaceConfigs();
			
			assertEquals(1, modifiedNetInterfaceConfigs.size());
			assertEquals(interfaceConfig2, modifiedNetInterfaceConfigs.get(0));
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}
	
	@Test
	public void testGetNetInterfaceConfig() {
		NetworkConfiguration config = new NetworkConfiguration();

		assertNull(config.getNetInterfaceConfig("if1"));
		
		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		config.addNetInterfaceConfig(interfaceConfig);

		assertNull(config.getNetInterfaceConfig("if2"));
		assertEquals(interfaceConfig, config.getNetInterfaceConfig("if1"));
	}

	@Test
	public void testGetConfigurationPropertiesNull() {
		NetworkConfiguration config = new NetworkConfiguration();

		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		assertNull(config.getConfigurationProperties());
	}

	@Test
	public void testGetConfigurationProperties() {
		NetworkConfiguration config = new NetworkConfiguration();

		EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
		config.addNetInterfaceConfig(interfaceConfig);

		assertEquals(true, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		Map<String, Object> properties = config.getConfigurationProperties();
		assertEquals(false, TestUtil.getFieldValue(config, "m_recomputeProperties"));
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("net.interfaces", "if1");
		expected.put("net.interface.if1.mac", "N/A");
		expected.put("net.interface.if1.loopback", false);
		expected.put("net.interface.if1.ptp", false);
		expected.put("net.interface.if1.up", false);
		expected.put("net.interface.if1.virtual", false);
		expected.put("net.interface.if1.type", "ETHERNET");
		expected.put("net.interface.if1.driver", null);
		expected.put("net.interface.if1.driver.version", null);
		expected.put("net.interface.if1.firmware.version", null);
		expected.put("net.interface.if1.config.name", "if1");
		expected.put("net.interface.if1.config.autoconnect", false);
		expected.put("net.interface.if1.config.mtu", 0);
		expected.put("net.interface.if1.eth.link.up", false);
		
		assertEquals(expected.size(), properties.size());
		
		for (Map.Entry<String, Object> entry: expected.entrySet()) {
			assertEquals(entry.getValue(), properties.get(entry.getKey()));
		}
	}

	@Test
	public void testIsValidEmpty() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();
			
			assertTrue(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidNonEmpty() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			config.addNetInterfaceConfig(interfaceConfig);
			
			assertTrue(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidValidMTU() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setMTU(0);
			config.addNetInterfaceConfig(interfaceConfig);

			interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setMTU(1);
			config.addNetInterfaceConfig(interfaceConfig);
			
			assertTrue(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidInvalidMTU() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			EthernetInterfaceConfigImpl interfaceConfig = new EthernetInterfaceConfigImpl("if1");
			interfaceConfig.setMTU(-1);
			config.addNetInterfaceConfig(interfaceConfig);
			
			assertFalse(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidInvalidType() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			MockInterfaceConfigImpl icMock = new MockInterfaceConfigImpl("ifMock");
			icMock.setType(NetInterfaceType.ADSL);
			config.addNetInterfaceConfig(icMock);
			
			assertFalse(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidValidType() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			EthernetInterfaceConfigImpl icEthernet = new EthernetInterfaceConfigImpl("if1");
			config.addNetInterfaceConfig(icEthernet);

			WifiInterfaceConfigImpl icWifi = new WifiInterfaceConfigImpl("if2");
			config.addNetInterfaceConfig(icWifi);

			ModemInterfaceConfigImpl icModem = new ModemInterfaceConfigImpl("if3");
			config.addNetInterfaceConfig(icModem);

			LoopbackInterfaceConfigImpl icLoopback = new LoopbackInterfaceConfigImpl("if4");
			config.addNetInterfaceConfig(icLoopback);
			
			assertTrue(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidValidConfig() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			NetInterfaceAddressConfigImpl addressConfig1 = new NetInterfaceAddressConfigImpl();
			addressConfig1.setNetConfigs(null);

			NetConfigIP4 netConfig = new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false);
			netConfig.setDhcp(true);
			
			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			netConfigs.add(netConfig);
			
			NetInterfaceAddressConfigImpl addressConfig2 = new NetInterfaceAddressConfigImpl();
			addressConfig2.setNetConfigs(netConfigs);

			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(addressConfig1);
			interfaceAddresses.add(addressConfig2);
			
			EthernetInterfaceConfigImpl icEthernet = new EthernetInterfaceConfigImpl("if1");
			icEthernet.setNetInterfaceAddresses(interfaceAddresses);
			config.addNetInterfaceConfig(icEthernet);

			assertTrue(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testIsValidInvalidConfig() {
		try {
			NetworkConfiguration config = new NetworkConfiguration();

			List<NetConfig> netConfigs = new ArrayList<NetConfig>();
			netConfigs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
			
			NetInterfaceAddressConfigImpl addressConfig = new NetInterfaceAddressConfigImpl();
			addressConfig.setNetConfigs(netConfigs);

			List<NetInterfaceAddressConfig> interfaceAddresses = new ArrayList<NetInterfaceAddressConfig>();
			interfaceAddresses.add(addressConfig);
			
			EthernetInterfaceConfigImpl icEthernet = new EthernetInterfaceConfigImpl("if1");
			icEthernet.setNetInterfaceAddresses(interfaceAddresses);
			config.addNetInterfaceConfig(icEthernet);

			assertFalse(config.isValid());
		} catch (KuraException e) {
			fail("unexpected exception");
		}
	}
}
