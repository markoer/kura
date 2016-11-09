package org.eclipse.kura.core.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.net.util.NetworkUtil;
import org.eclipse.kura.net.IPAddress;
import org.eclipse.kura.net.NetInterfaceState;
import org.eclipse.kura.net.NetInterfaceType;
import org.eclipse.kura.net.wifi.WifiInterface.Capability;
import org.eclipse.kura.usb.UsbNetDevice;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WifiInterfaceConfigImplTest {

	@Test
	public void testWifiInterfaceConfigImplString() {
		WifiInterfaceConfigImpl config = new WifiInterfaceConfigImpl("name1");
        assertEquals("name1", config.getName());
        assertTrue(config.getNetInterfaceAddresses().isEmpty());

        config = new WifiInterfaceConfigImpl("name2");
        assertEquals("name2", config.getName());
        assertTrue(config.getNetInterfaceAddresses().isEmpty());
	}

	@Test
	public void testWifiInterfaceConfigImplWifiInterfaceOfQextendsWifiInterfaceAddress() {
        try {
        	WifiInterfaceConfigImpl config = createConfig(0);

            assertEquals("wifiInterface", config.getName());
            assertEquals(1, config.getNetInterfaceAddresses().size());
        } catch (UnknownHostException e) {
            fail("unexpected exception");
        }
	}

    @Test
    public void testToString1() {
        try {
        	WifiInterfaceConfigImpl config = createConfig(0);
        
        String expected = "name=wifiInterface :: hardwareAddress= :: loopback=false" +
                " :: pointToPoint=false :: virtual=false :: supportsMulticast=false" +
                " :: up=false :: mtu=0 :: driver=null :: driverVersion=null" +
                " :: firmwareVersion=null :: state=null :: autoConnect=false" +
                " :: InterfaceAddress=NetConfig: no configurations  :: capabilities=null";

        assertEquals(expected, config.toString());
        } catch (UnknownHostException e) {
            fail("unexpected exception");
        }
    }

	@Test
	public void testToString2() {
		try {
			WifiInterfaceConfigImpl config = createConfig(2);
	        
	        config.setHardwareAddress(NetworkUtil.macToBytes("12:34:56:78:90:AB"));
	        config.setLoopback(true);
	        config.setPointToPoint(true);
	        config.setVirtual(true);
	        config.setSupportsMulticast(true);
	        config.setUp(true);
	        config.setMTU(42);
	            config.setUsbDevice(new UsbNetDevice("vendorId", "productId", "manufacturerName", "productName",
	                    "usbBusNumber", "usbDevicePath", "interfaceName"));
	        config.setDriver("driverName");
	        config.setDriverVersion("driverVersion");
	        config.setFirmwareVersion("firmwareVersion");
	        config.setState(NetInterfaceState.ACTIVATED);
	        config.setAutoConnect(true);
	        
	        config.setCapabilities(EnumSet.of(Capability.WPA, Capability.CIPHER_WEP40));
	        
	        String expected = "name=wifiInterface :: hardwareAddress=12:34:56:78:90:AB" +
	                " :: loopback=true :: pointToPoint=true :: virtual=true :: supportsMulticast=true" +
	                " :: up=true :: mtu=42 :: usbDevice=UsbNetDevice [getInterfaceName()=interfaceName," +
	                " getVendorId()=vendorId, getProductId()=productId, getManufacturerName()=manufacturerName," +
	                " getProductName()=productName, getUsbBusNumber()=usbBusNumber, getUsbDevicePath()=usbDevicePath," +
	                " getUsbPort()=usbBusNumber-usbDevicePath] :: driver=driverName :: driverVersion=driverVersion" +
	                " :: firmwareVersion=firmwareVersion :: state=ACTIVATED :: autoConnect=true" +
	                " :: InterfaceAddress=NetConfig: no configurations NetConfig: no configurations " +
	                " :: capabilities=CIPHER_WEP40 WPA ";
	
	        assertEquals(expected, config.toString());
        } catch (UnknownHostException e) {
            fail("unexpected exception");
        } catch (KuraException e) {
            fail("unexpected exception");
        }
	}

	@Test
	public void testGetType() {
		WifiInterfaceConfigImpl config = new WifiInterfaceConfigImpl("name");
        assertEquals(NetInterfaceType.WIFI, config.getType());
	}

	@Test
	public void testCapabilities() {
		WifiInterfaceConfigImpl config = new WifiInterfaceConfigImpl("name");
		
		EnumSet<Capability> expected = EnumSet.of(Capability.WPA, Capability.CIPHER_WEP40);
		config.setCapabilities(expected);
        assertEquals(expected, config.getCapabilities());
		
        expected = EnumSet.of(Capability.CIPHER_WEP104, Capability.CIPHER_TKIP);
		config.setCapabilities(expected);
        assertEquals(expected, config.getCapabilities());
	}

	WifiInterfaceConfigImpl createConfig(int noOfAddresses) throws UnknownHostException {
		WifiInterfaceImpl<WifiInterfaceAddressConfigImpl> interfaceImpl = new WifiInterfaceImpl<WifiInterfaceAddressConfigImpl>(
				"wifiInterface");

		if (noOfAddresses > 0) {
			List<WifiInterfaceAddressConfigImpl> interfaceAddresses = new ArrayList<WifiInterfaceAddressConfigImpl>();

			for (int i = 0; i < noOfAddresses; i++) {
				try {
					String ipAddress = "10.0.0." + Integer.toString(i + 1);
					WifiInterfaceAddressConfigImpl interfaceAddress = createAddress(ipAddress);
					interfaceAddresses.add(interfaceAddress);
				} catch (UnknownHostException e) {
					throw e;
				}
			}

			interfaceImpl.setNetInterfaceAddresses(interfaceAddresses);
		} else {
			interfaceImpl.setNetInterfaceAddresses(null);
		}

		WifiInterfaceConfigImpl config = new WifiInterfaceConfigImpl(interfaceImpl);
		return config;
	}

	WifiInterfaceAddressConfigImpl createAddress(String ipAddress) throws UnknownHostException {
        try {
        	WifiInterfaceAddressConfigImpl interfaceAddress = new WifiInterfaceAddressConfigImpl();

            interfaceAddress.setAddress(IPAddress.parseHostAddress(ipAddress));

            return interfaceAddress;
        } catch (UnknownHostException e) {
            throw e;
        }
    }
}