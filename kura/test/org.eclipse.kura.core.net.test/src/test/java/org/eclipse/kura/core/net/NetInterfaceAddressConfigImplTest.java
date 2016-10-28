package org.eclipse.kura.core.net;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.eclipse.kura.net.IPAddress;
import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetConfigIP4;
import org.eclipse.kura.net.NetInterfaceStatus;
import org.junit.FixMethodOrder;
import org.junit.Test;

@FixMethodOrder
public class NetInterfaceAddressConfigImplTest {

	@Test
	public void testEqualsObject() {
		NetInterfaceAddressConfigImpl a = createAddress();
		NetInterfaceAddressConfigImpl b = createAddress();
		assertEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentIPAddress() {
		try {
			NetInterfaceAddressConfigImpl a = createAddress();
			NetInterfaceAddressConfigImpl b = createAddress();
			
			a.setAddress(IPAddress.parseHostAddress("10.0.0.101"));
			b.setAddress(IPAddress.parseHostAddress("10.0.0.102"));
			
			assertNotEquals(a, b);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testEqualsObjectDifferentBroadcast() {
		try {
			NetInterfaceAddressConfigImpl a = createAddress();
			NetInterfaceAddressConfigImpl b = createAddress();
			
			a.setBroadcast(IPAddress.parseHostAddress("10.0.1.255"));
			b.setBroadcast(IPAddress.parseHostAddress("10.0.2.255"));
			
			assertNotEquals(a, b);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testEqualsObjectDifferentDnsServers() {
		try {
			NetInterfaceAddressConfigImpl a = createAddress();
			NetInterfaceAddressConfigImpl b = createAddress();

			ArrayList<IPAddress> dnsServersA = new ArrayList<IPAddress>();
			dnsServersA.add(IPAddress.parseHostAddress("10.0.2.1"));
			dnsServersA.add(IPAddress.parseHostAddress("10.0.2.2"));
			a.setDnsServers(dnsServersA);

			ArrayList<IPAddress> dnsServersB = new ArrayList<IPAddress>();
			dnsServersB.add(IPAddress.parseHostAddress("10.0.3.1"));
			dnsServersB.add(IPAddress.parseHostAddress("10.0.3.2"));
			b.setDnsServers(dnsServersB);
			
			assertNotEquals(a, b);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testEqualsObjectDifferentGateway() {
		try {
			NetInterfaceAddressConfigImpl a = createAddress();
			NetInterfaceAddressConfigImpl b = createAddress();
			
			a.setGateway(IPAddress.parseHostAddress("10.0.1.1"));
			b.setGateway(IPAddress.parseHostAddress("10.0.2.1"));
			
			assertNotEquals(a, b);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testEqualsObjectDifferentNetConfigs() {
		NetInterfaceAddressConfigImpl a = createAddress();
		NetInterfaceAddressConfigImpl b = createAddress();

		ArrayList<NetConfig> configsA = new ArrayList<NetConfig>();
		configsA.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true));
		configsA.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		a.setNetConfigs(configsA);

		ArrayList<NetConfig> configsB = new ArrayList<NetConfig>();
		configsB.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusDisabled, false));
		configsB.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
		b.setNetConfigs(configsB);

		assertNotEquals(a, b);
	}

	@Test
	public void testEqualsObjectDifferentNetmask() {
		try {
			NetInterfaceAddressConfigImpl a = createAddress();
			NetInterfaceAddressConfigImpl b = createAddress();
			
			a.setNetmask(IPAddress.parseHostAddress("255.255.0.0"));
			b.setNetmask(IPAddress.parseHostAddress("255.0.0.0"));
			
			assertNotEquals(a, b);
		} catch (UnknownHostException e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testEqualsObjectDifferentNetworkPrefixLength() {
		NetInterfaceAddressConfigImpl a = createAddress();
		NetInterfaceAddressConfigImpl b = createAddress();

		a.setNetworkPrefixLength((short) 20);
		b.setNetworkPrefixLength((short) 30);

		assertNotEquals(a, b);
	}
	
//	@Test
//	public void testNetInterfaceAddressConfigImpl() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testNetConfigs() {
		NetInterfaceAddressConfigImpl value = createAddress();

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

//	@Test
//	public void testToString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testNetInterfaceAddressImpl() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testNetInterfaceAddressImplNetInterfaceAddress() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetAddress() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetAddress() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNetworkPrefixLength() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetNetworkPrefixLength() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNetmask() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetNetmask() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetGateway() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetGateway() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetBroadcast() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetBroadcast() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetDnsServers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetDnsServers() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompare() {
//		fail("Not yet implemented");
//	}

	private NetInterfaceAddressConfigImpl createAddress() {
		try {
			ArrayList<IPAddress> dnsServers = new ArrayList<IPAddress>();
			dnsServers.add(IPAddress.parseHostAddress("10.0.1.1"));
			dnsServers.add(IPAddress.parseHostAddress("10.0.1.2"));
			
			ArrayList<NetConfig> configs = new ArrayList<NetConfig>();
			configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledLAN, true));
			configs.add(new NetConfigIP4(NetInterfaceStatus.netIPv4StatusEnabledWAN, false));
			
			NetInterfaceAddressConfigImpl value = new NetInterfaceAddressConfigImpl();
			value.setAddress(IPAddress.parseHostAddress("10.0.0.100"));
			value.setBroadcast(IPAddress.parseHostAddress("10.0.0.255"));
			value.setDnsServers(dnsServers);
			value.setGateway(IPAddress.parseHostAddress("10.0.0.1"));
			value.setNetConfigs(configs);
			value.setNetmask(IPAddress.parseHostAddress("255.255.255.0"));
			value.setNetworkPrefixLength((short)24);
			
			return value;
		} catch (UnknownHostException e) {
			return null;
		}
	}
}
