package org.eclipse.kura.core.net.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.kura.KuraException;
import org.junit.FixMethodOrder;
import org.junit.Test;

@FixMethodOrder
public class NetworkUtilTest {

    // Unit tests for method: calculateNetwork
    @Test
    public void testCalculateNetwork() {
        try {
            String result = NetworkUtil.calculateNetwork("192.168.1.123", "255.255.255.0");
            assertEquals("192.168.1.0", result);

            result = NetworkUtil.calculateNetwork("10.100.250.1", "255.0.0.0");
            assertEquals("10.0.0.0", result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testCalculateNetworkNullAddress() {
        try {
            NetworkUtil.calculateNetwork(null, "255.255.255.0");
            fail("null address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkEmptyAddress() {
        try {
            NetworkUtil.calculateNetwork("", "255.255.255.0");
            fail("empty address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkTooShortAddress() {
        try {
            NetworkUtil.calculateNetwork("192.168.1", "255.255.255.0");
            fail("too short address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkTooLongAddress() {
        try {
            NetworkUtil.calculateNetwork("192.168.1.123.1", "255.255.255.0");
            fail("too long address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkInvalidAddress() {
        try {
            NetworkUtil.calculateNetwork("256.168.1.123", "255.255.255.0");
            fail("invalid address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkNullSubnet() {
        try {
            NetworkUtil.calculateNetwork("192.168.1.123", null);
            fail("null subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkEmptySubnet() {
        try {
            NetworkUtil.calculateNetwork("192.168.1.123", "");
            fail("empty subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkTooShortSubnet() {
        try {
            NetworkUtil.calculateNetwork("192.168.1.123", "255.255.255");
            fail("too short subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkTooLongSubnet() {
        try {
            NetworkUtil.calculateNetwork("192.168.1.123", "255.255.255.0.0");
            fail("too long subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateNetworkInvalidSubnet() {
        try {
            NetworkUtil.calculateNetwork("192.168.1.123", "256.255.255.0");
            fail("invalid subnet");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: calculateBroadcast
    @Test
    public void testCalculateBroadcast() {
        try {
            String result = NetworkUtil.calculateBroadcast("192.168.1.123", "255.255.255.0");
            assertEquals("192.168.1.255", result);
    
            result = NetworkUtil.calculateBroadcast("10.100.250.1", "255.0.0.0");
            assertEquals("10.255.255.255", result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testCalculateBroadcastNullAddress() {
        try {
            NetworkUtil.calculateBroadcast(null, "255.255.255.0");
            fail("null address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastEmptyAddress() {
        try {
            NetworkUtil.calculateBroadcast("", "255.255.255.0");
            fail("empty address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastTooShortAddress() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1", "255.255.255.0");
            fail("too short address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastTooLongAddress() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1.123.1", "255.255.255.0");
            fail("too long address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastInvalidAddress() {
        try {
            NetworkUtil.calculateBroadcast("256.168.1.123", "255.255.255.0");
            fail("invalid address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastNullSubnet() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1.123", null);
            fail("null subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastEmptySubnet() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1.123", "");
            fail("empty subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastTooShortSubnet() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1.123", "255.255.255");
            fail("too short subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastTooLongSubnet() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1.123", "255.255.255.0.0");
            fail("too long subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testCalculateBroadcastInvalidSubnet() {
        try {
            NetworkUtil.calculateBroadcast("192.168.1.123", "256.255.255.0");
            fail("too invalid subnet");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: getNetmaskStringForm
    @Test
    public void testGetNetmaskStringForm() {
        try {
            String result = NetworkUtil.getNetmaskStringForm(32);
            assertEquals("255.255.255.255", result);

            result = NetworkUtil.getNetmaskStringForm(24);
            assertEquals("255.255.255.0", result);

            result = NetworkUtil.getNetmaskStringForm(17);
            assertEquals("255.255.128.0", result);

            result = NetworkUtil.getNetmaskStringForm(1);
            assertEquals("128.0.0.0", result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testGetNetmaskStringFormPrefixAboveUpperLimit() {
        try {
            NetworkUtil.getNetmaskStringForm(33);
            fail("exception was expected");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testGetNetmaskStringFormPrefixBelowLowerLimit() {
        try {
            // Value of prefix has to be at least 1, value 0 is illegal
            NetworkUtil.getNetmaskStringForm(0);
            fail("exception was expected");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testGetNetmaskStringFormNegativePrefix() {
        try {
            NetworkUtil.getNetmaskStringForm(-1);
            fail("exception was expected");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: getNetmaskShortForm
    @Test
    public void testGetNetmaskShortForm() {
        try {
            short result = NetworkUtil.getNetmaskShortForm("255.255.255.255");
            assertEquals(32, result);

            result = NetworkUtil.getNetmaskShortForm("255.255.0.0");
            assertEquals(16, result);

            result = NetworkUtil.getNetmaskShortForm("255.128.0.0");
            assertEquals(9, result);

            result = NetworkUtil.getNetmaskShortForm("128.0.0.0");
            assertEquals(1, result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testGetNetmaskShortFormNullSubnet() {
        try {
            NetworkUtil.getNetmaskShortForm(null);
            fail("null subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testGetNetmaskShortFormEmptySubnet() {
        try {
            NetworkUtil.getNetmaskShortForm("");
            fail("empty subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testGetNetmaskShortFormTooShortSubnet() {
        try {
            NetworkUtil.getNetmaskShortForm("255.255.255");
            fail("too short subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testGetNetmaskShortFormTooLongSubnet() {
        try {
            NetworkUtil.getNetmaskShortForm("255.255.255.0.0");
            fail("too long subnet");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testGetNetmaskShortFormInvalidSubnet() {
        try {
            NetworkUtil.getNetmaskShortForm("256.255.255.0");
            fail("invalid subnet");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: dottedQuad
    @Test
    public void testDottedQuad() {
        String result = NetworkUtil.dottedQuad(0xFFFFFFFF);
        assertEquals("255.255.255.255", result);

        result = NetworkUtil.dottedQuad(0xFFFFFF00);
        assertEquals("255.255.255.0", result);

        result = NetworkUtil.dottedQuad(0xFF120000);
        assertEquals("255.18.0.0", result);

        result = NetworkUtil.dottedQuad(0x80000000);
        assertEquals("128.0.0.0", result);

        result = NetworkUtil.dottedQuad(0x00000000);
        assertEquals("0.0.0.0", result);
    }

    // Unit tests for method: convertIp4Address
    @Test
    public void testConvertIp4Address() {
        try {
            int result = NetworkUtil.convertIp4Address("255.255.255.255");
            assertEquals(0xFFFFFFFF, result);
    
            result = NetworkUtil.convertIp4Address("255.255.255.0");
            assertEquals(0xFFFFFF00, result);
    
            result = NetworkUtil.convertIp4Address("255.18.0.0");
            assertEquals(0xFF120000, result);
    
            result = NetworkUtil.convertIp4Address("128.0.0.0");
            assertEquals(0x80000000, result);
    
            result = NetworkUtil.convertIp4Address("0.0.0.0");
            assertEquals(0x00000000, result);
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIp4AddressNullAddress() {
        try {
            NetworkUtil.convertIp4Address(null);
            fail("null address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIp4AddressEmptyAddress() {
        try {
            NetworkUtil.convertIp4Address("");
            fail("empty address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIp4AddressTooShortAddress() {
        try {
            NetworkUtil.convertIp4Address("192.168.1");
            fail("too short address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIp4AddressTooLongAddress() {
        try {
            NetworkUtil.convertIp4Address("192.168.1.123.1");
            fail("too long address");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIp4AddressInvalidAddress() {
        try {
            NetworkUtil.convertIp4Address("256.168.1.123");
            fail("invalid address");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: packIp4AddressBytes
    @Test
    public void testPackIp4AddressBytes() {
        try {
            int result = NetworkUtil.packIp4AddressBytes(new short[] { 255, 255, 255, 255 });
            assertEquals(0xFFFFFFFF, result);
    
            result = NetworkUtil.packIp4AddressBytes(new short[] { 255, 255, 255, 0 });
            assertEquals(0xFFFFFF00, result);
    
            result = NetworkUtil.packIp4AddressBytes(new short[] { 255, 255, 18, 0 });
            assertEquals(0xFFFF1200, result);
    
            result = NetworkUtil.packIp4AddressBytes(new short[] { 128, 0, 0, 0 });
            assertEquals(0x80000000, result);
    
            result = NetworkUtil.packIp4AddressBytes(new short[] { 0, 0, 0, 0 });
            assertEquals(0x00000000, result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testPackIp4AddressBytesNullValue() {
        try {
            NetworkUtil.packIp4AddressBytes(null);
            fail("null value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testPackIp4AddressBytesEmptyValue() {
        try {
            NetworkUtil.packIp4AddressBytes(new short[] {});
            fail("empty value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testPackIp4AddressBytesTooShortValue() {
        try {
            NetworkUtil.packIp4AddressBytes(new short[] { 192, 168, 1 });
            fail("too short value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testPackIp4AddressBytesTooLongValue() {
        try {
            NetworkUtil.packIp4AddressBytes(new short[] { 192, 168, 1, 123, 1 });
            fail("too long value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testPackIp4AddressBytesInvalidValue() {
        try {
            NetworkUtil.packIp4AddressBytes(new short[] { 256, 168, 1, 123 });
            fail("invalid value");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: unpackIP4AddressInt
    @Test
    public void testUnpackIP4AddressInt() {
        short[] result = NetworkUtil.unpackIP4AddressInt(0xFFFFFFFF);
        assertArrayEquals(new short[] { 255, 255, 255, 255 }, result);

        result = NetworkUtil.unpackIP4AddressInt(0xFFFFFF00);
        assertArrayEquals(new short[] { 255, 255, 255, 0 }, result);

        result = NetworkUtil.unpackIP4AddressInt(0xFFFF1200);
        assertArrayEquals(new short[] { 255, 255, 18, 0 }, result);

        result = NetworkUtil.unpackIP4AddressInt(0x80000000);
        assertArrayEquals(new short[] { 128, 0, 0, 0 }, result);

        result = NetworkUtil.unpackIP4AddressInt(0x00000000);
        assertArrayEquals(new short[] { 0, 0, 0, 0 }, result);
    }

    // Unit tests for method: convertIP6Address (string parameter)
    @Test
    public void testConvertIP6AddressStringFullFormat() {
        try {
            byte[] result = NetworkUtil.convertIP6Address("2001:db8:85a3:0:0:8a2e:370:7334");
            byte[] expected = { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x00, 0x00, 0x00, 0x00,
                    (byte) 0x8a, 0x2e, 0x03, 0x70, 0x73, 0x34 };
            assertArrayEquals(expected, result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testConvertIP6AddressStringNullValue() {
        try {
            String input = null;
            NetworkUtil.convertIP6Address(input);
            fail("null value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressStringEmptyValue() {
        try {
            NetworkUtil.convertIP6Address("");
            fail("empty value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressStringInvalidValue() {
        try {
            NetworkUtil.convertIP6Address("g001:db8:85a3:0:0:8a2e:370:7334");
            fail("invalid value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressStringOutOfRangeValue() {
        try {
            NetworkUtil.convertIP6Address("12345:db8:85a3:0:0:8a2e:370:7334");
            fail("out of range value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressStringNotEnoughGroups() {
        try {
            NetworkUtil.convertIP6Address("2001:db8:85a3:0:0:8a2e:370");
            fail("not enough groups");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressStringToManyGroups() {
        try {
            NetworkUtil.convertIP6Address("2001:db8:85a3:0:0:8a2e:370:7334:7334");
            fail("too many groups");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: convertIP6Address (byte array parameter)
    @Test
    public void testConvertIP6AddressByteArray() {
        try {
            byte[] input = { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x00, 0x00, 0x00, 0x00, (byte) 0x8a,
                    0x2e, 0x03, 0x70, 0x73, 0x34 };
            String result = NetworkUtil.convertIP6Address(input);
            assertEquals("2001:db8:85a3:0:0:8a2e:370:7334", result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testConvertIP6AddressByteArrayNullValue() {
        try {
            byte[] input = null;
            NetworkUtil.convertIP6Address(input);
            fail("null value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressByteArrayEmptyValue() {
        try {
            NetworkUtil.convertIP6Address(new byte[] {});
            fail("empty value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressByteArrayNotEnoughElements() {
        try {
            byte[] input = { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x00, 0x00, 0x00, 0x00,
                    (byte) 0x8a, 0x2e, 0x03, 0x70, 0x73 };
            NetworkUtil.convertIP6Address(input);
            fail("not enough elements");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testConvertIP6AddressByteArrayTooManyElements() {
        try {
            byte[] input = { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x00, 0x00, 0x00, 0x00, (byte) 0x8a,
                    0x2e, 0x03, 0x70, 0x73, 0x34, 0x34 };
            NetworkUtil.convertIP6Address(input);
            fail("too many elements");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: macToString
    @Test
    public void testMacToString() {
        try {
            byte[] input = { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB };
            String result = NetworkUtil.macToString(input);
            assertEquals("01:23:45:67:89:AB", result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testMacToStringNullValue() {
        try {
            NetworkUtil.macToString(null);
            fail("null value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToStringEmptyValue() {
        try {
            NetworkUtil.macToString(new byte[] {});
            fail("empty value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToStringNotEnoughElements() {
        try {
            byte[] input = { 0x01, 0x23, 0x45, 0x67, (byte) 0x89 };
            NetworkUtil.macToString(input);
            fail("not enough elements");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToStringTooManyElements() {
        try {
            byte[] input = { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, 0x42 };
            NetworkUtil.macToString(input);
            fail("too many elements");
        } catch (KuraException e) {
        }
    }

    // Unit tests for method: macToBytes
    @Test
    public void testMacToBytes() {
        try {
            byte[] result = NetworkUtil.macToBytes("01:23:45:67:89:AB");
            byte[] expected = { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB };
            assertArrayEquals(expected, result);
        } catch (KuraException e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void testMacToBytesNullValue() {
        try {
            NetworkUtil.macToBytes(null);
            fail("null value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToBytesEmptyValue() {
        try {
            NetworkUtil.macToBytes("");
            fail("empty value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToBytesInvalidValue() {
        try {
            NetworkUtil.macToBytes("g1:23:45:67:89:AB");
            fail("invalid value");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToBytesNotEnoughElements() {
        try {
            NetworkUtil.macToBytes("01:23:45:67:89");
            fail("not enough elements");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToBytesTooManyElements() {
        try {
            NetworkUtil.macToBytes("01:23:45:67:89:AB:42");
            fail("too many elements");
        } catch (KuraException e) {
        }
    }

    @Test
    public void testMacToBytesOutOfRangeValue() {
        try {
            NetworkUtil.macToBytes("101:23:45:67:89:AB");
            fail("out of range value");
        } catch (KuraException e) {
        }
    }
}
