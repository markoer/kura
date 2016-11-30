/*******************************************************************************
 * Copyright (c) 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.kura.core.net.modem;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.eclipse.kura.net.IPAddress;
import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetConfigIP4;
import org.eclipse.kura.net.NetInterfaceStatus;
import org.eclipse.kura.net.modem.ModemConnectionStatus;
import org.eclipse.kura.net.modem.ModemConnectionType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModemInterfaceAddressConfigImplTest {

	@Test
	public void testEqualsObject() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		assertEquals(a, a);
		
		ModemInterfaceAddressConfigImpl b = createConfig();
		assertEquals(a, b);

		a.setNetConfigs(null);
		b.setNetConfigs(null);

		assertEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentNetConfigs() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		ArrayList<NetConfig> configsA = new ArrayList<NetConfig>();
		configsA.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true));
		configsA.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		a.setNetConfigs(configsA);

		ArrayList<NetConfig> configsB = new ArrayList<NetConfig>();
		configsB.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		configsB.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		b.setNetConfigs(configsB);

		assertNotEquals(a, b);
		
		a.setNetConfigs(null);
		assertNotEquals(a, b);

		a.setNetConfigs(configsA);
		b.setNetConfigs(null);
		assertNotEquals(a, b);

		ArrayList<NetConfig> configsA2 = new ArrayList<NetConfig>();
		configsA2.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		configsA2.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		a.setNetConfigs(configsA2);

		ArrayList<NetConfig> configsB2 = new ArrayList<NetConfig>();
		configsB2.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		b.setNetConfigs(configsB2);
		
		assertNotEquals(a, b);

		configsB2.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		b.setNetConfigs(configsB2);
		
		assertNotEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentBytesReceived() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		a.setBytesReceived(111);
		b.setBytesReceived(112);
		
		assertNotEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentBytesTransmitted() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		a.setBytesTransmitted(111);
		b.setBytesTransmitted(112);
		
		assertNotEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentConnectionStatus() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		a.setConnectionStatus(ModemConnectionStatus.CONNECTED);
		b.setConnectionStatus(ModemConnectionStatus.DISCONNECTED);
		
		assertNotEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentConnectionType() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		a.setConnectionType(ModemConnectionType.DirectIP);
		b.setConnectionType(ModemConnectionType.PPP);
		
		assertNotEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentIsRoaming() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		a.setIsRoaming(false);
		b.setIsRoaming(true);
		
		assertNotEquals(a, b);
	}
	
	@Test
	public void testEqualsObjectDifferentMode() {
		ModemInterfaceAddressConfigImpl a = createConfig();
		ModemInterfaceAddressConfigImpl b = createConfig();

		a.setSignalStrength(111);
		b.setSignalStrength(112);
		
		assertNotEquals(a, b);
	}

	@Test
	public void testModemInterfaceAddressConfigImpl() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		assertEquals(0, value.getSignalStrength());
		assertEquals(false, value.isRoaming());
		assertNull(value.getConnectionStatus());
		assertEquals(0, value.getBytesTransmitted());
		assertEquals(0, value.getBytesReceived());
		assertEquals(ModemConnectionType.PPP, value.getConnectionType());
	}

	@Test
	public void testModemInterfaceAddressConfigImplModemInterfaceAddress() {
		ModemInterfaceAddressImpl address = new ModemInterfaceAddressImpl();
		address.setSignalStrength(100);
		address.setIsRoaming(true);
		address.setConnectionStatus(ModemConnectionStatus.CONNECTING);
		address.setBytesTransmitted(200);
		address.setBytesReceived(300);
		address.setConnectionType(ModemConnectionType.DirectIP);
		
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl(address);

		assertEquals(100, value.getSignalStrength());
		assertEquals(true, value.isRoaming());
		assertEquals(ModemConnectionStatus.CONNECTING, value.getConnectionStatus());
		assertEquals(200, value.getBytesTransmitted());
		assertEquals(300, value.getBytesReceived());
		assertEquals(ModemConnectionType.DirectIP, value.getConnectionType());
	}

	@Test
	public void testNetConfigs() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		ArrayList<NetConfig> configs = new ArrayList<NetConfig>();
		configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true));
		configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		value.setNetConfigs(configs);
		assertEquals(value.getConfigs(), configs);

		configs = new ArrayList<NetConfig>();
		configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		value.setNetConfigs(configs);
		assertEquals(value.getConfigs(), configs);
	}

	@Test
	public void testToStringNoConfigs() {
		ModemInterfaceAddressConfigImpl value = createConfig();
		value.setNetConfigs(null);

		String expected = "NetConfig: no configurations";
		
		assertEquals(expected, value.toString());
	}

	@Test
	public void testToStringEmptyConfigs() {
		ModemInterfaceAddressConfigImpl value = createConfig();
		value.setNetConfigs(new ArrayList<NetConfig>());

		String expected = "";
		
		assertEquals(expected, value.toString());
	}

	@Test
	public void testToStringNullConfig() {
		ModemInterfaceAddressConfigImpl value = createConfig();
		
		ArrayList<NetConfig> configs = new ArrayList<NetConfig>();
		configs.add(null);
		value.setNetConfigs(configs);

		String expected = "NetConfig: null - ";
		
		assertEquals(expected, value.toString());
	}

	@Test
	public void testToStringWithConfigs() {
		ModemInterfaceAddressConfigImpl value = createConfig();

		String expected = "NetConfig: NetConfigIP4 [winsServers=[], super.toString()=NetConfigIP" +
				" [m_status=netIPv4StatusEnabledLAN, m_autoConnect=true, m_dhcp=false, m_address=null," +
				" m_networkPrefixLength=-1, m_subnetMask=null, m_gateway=null, m_dnsServers=[]," +
				" m_domains=[], m_properties={}]] - " +
				"NetConfig: NetConfigIP4 [winsServers=[], super.toString()=NetConfigIP" +
				" [m_status=netIPv4StatusEnabledWAN, m_autoConnect=false, m_dhcp=false, m_address=null," +
				" m_networkPrefixLength=-1, m_subnetMask=null, m_gateway=null, m_dnsServers=[]," +
				" m_domains=[], m_properties={}]] - ";
		
		assertEquals(expected, value.toString());
	}
	
	@Test
	public void testSignalStrength() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		value.setSignalStrength(100);
		assertEquals(100, value.getSignalStrength());
		
		value.setSignalStrength(200);
		assertEquals(200, value.getSignalStrength());
	}

	@Test
	public void testIsRoaming() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		value.setIsRoaming(false);
		assertEquals(false, value.isRoaming());
		
		value.setIsRoaming(true);
		assertEquals(true, value.isRoaming());
	}

	@Test
	public void testConnectionStatus() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		value.setConnectionStatus(ModemConnectionStatus.CONNECTED);
		assertEquals(ModemConnectionStatus.CONNECTED, value.getConnectionStatus());
		
		value.setConnectionStatus(ModemConnectionStatus.DISCONNECTED);
		assertEquals(ModemConnectionStatus.DISCONNECTED, value.getConnectionStatus());
	}

	@Test
	public void testBytesTransmitted() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		value.setBytesTransmitted(100);
		assertEquals(100, value.getBytesTransmitted());
		
		value.setBytesTransmitted(200);
		assertEquals(200, value.getBytesTransmitted());
	}

	@Test
	public void testBytesReceived() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		value.setBytesReceived(100);
		assertEquals(100, value.getBytesReceived());
		
		value.setBytesReceived(200);
		assertEquals(200, value.getBytesReceived());
	}

	@Test
	public void testConnectionType() {
		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();

		value.setConnectionType(ModemConnectionType.DirectIP);
		assertEquals(ModemConnectionType.DirectIP, value.getConnectionType());
		
		value.setConnectionType(ModemConnectionType.PPP);
		assertEquals(ModemConnectionType.PPP, value.getConnectionType());
	}

	private ModemInterfaceAddressConfigImpl createConfig() {
		try {
			ArrayList<IPAddress> dnsServers = new ArrayList<IPAddress>();
			dnsServers.add(IPAddress.parseHostAddress("10.0.1.1"));
			dnsServers.add(IPAddress.parseHostAddress("10.0.1.2"));
			
			ArrayList<NetConfig> configs = new ArrayList<NetConfig>();
			configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true));
			configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));

			ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl();
			value.setAddress(IPAddress.parseHostAddress("10.0.0.100"));
			value.setBroadcast(IPAddress.parseHostAddress("10.0.0.255"));
			value.setBytesReceived(100);
			value.setBytesTransmitted(200);
			value.setConnectionStatus(ModemConnectionStatus.CONNECTING);
			value.setConnectionType(ModemConnectionType.DirectIP);
			value.setDnsServers(dnsServers);
			value.setGateway(IPAddress.parseHostAddress("10.0.0.1"));
			value.setIsRoaming(true);
			value.setNetConfigs(configs);
			value.setNetmask(IPAddress.parseHostAddress("255.255.255.0"));
			value.setNetworkPrefixLength((short)24);
			value.setSignalStrength(300);
			
			return value;
		} catch (UnknownHostException e) {
			return null;
		}
	}
}
