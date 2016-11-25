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

import org.eclipse.kura.net.modem.ModemConnectionType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModemInterfaceAddressConfigImplTest {

//	@Test
//	public void testEqualsObject() {
//		fail("Not yet implemented");
//	}

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

//	@Test
//	public void testModemInterfaceAddressConfigImplModemInterfaceAddress() {
//		ArrayList<IPAddress> dnsServers = new ArrayList<IPAddress>();
//		dnsServers.add(IPAddress.parseHostAddress("10.0.1.1"));
//		dnsServers.add(IPAddress.parseHostAddress("10.0.1.2"));
//
//		ModemAccessPointImpl ModemAccessPoint = new ModemAccessPointImpl("ssid");
//		
//		ModemInterfaceAddressImpl address = new ModemInterfaceAddressImpl();
//		address.setAddress(IPAddress.parseHostAddress("10.0.0.100"));
//		address.setBitrate(42);
//		address.setBroadcast(IPAddress.parseHostAddress("10.0.0.255"));
//		address.setDnsServers(dnsServers);
//		address.setGateway(IPAddress.parseHostAddress("10.0.0.1"));
//		address.setMode(ModemMode.MASTER);
//		address.setNetmask(IPAddress.parseHostAddress("255.255.255.0"));
//		address.setNetworkPrefixLength((short)24);
//		address.setModemAccessPoint(ModemAccessPoint);
//		
//		ModemInterfaceAddressConfigImpl value = new ModemInterfaceAddressConfigImpl(address);
//
//		assertEquals(ModemMode.MASTER, value.getMode());
//		assertEquals(42, value.getBitrate());
//		assertEquals(ModemAccessPoint, value.getModemAccessPoint());
//		assertEquals(IPAddress.parseHostAddress("10.0.0.100"), value.getAddress());
//		assertEquals(24, value.getNetworkPrefixLength());
//		assertEquals(IPAddress.parseHostAddress("255.255.255.0"), value.getNetmask());
//		assertEquals(IPAddress.parseHostAddress("10.0.0.1"), value.getGateway());
//		assertEquals(IPAddress.parseHostAddress("10.0.0.255"), value.getBroadcast());
//		assertEquals(dnsServers, value.getDnsServers());
//	}
//
//	@Test
//	public void testGetConfigs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetNetConfigs() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testToString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testModemInterfaceAddressImpl() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testModemInterfaceAddressImplModemInterfaceAddress() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetSignalStrength() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetSignalStrength() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testIsRoaming() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetIsRoaming() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetConnectionStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetConnectionStatus() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetBytesTransmitted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetBytesTransmitted() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetBytesReceived() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetBytesReceived() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetConnectionType() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetConnectionType() {
//		fail("Not yet implemented");
//	}
}
