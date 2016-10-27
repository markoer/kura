/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.core.net.util;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtil {

    private static final Logger s_logger = LoggerFactory.getLogger(NetworkUtil.class);

    public static String calculateNetwork(String ipAddress, String netmask) throws KuraException {
        int ipAddressValue = NetworkUtil.convertIp4Address(ipAddress);
        int netmaskValue = NetworkUtil.convertIp4Address(netmask);
        
        int network = ipAddressValue & netmaskValue;
        return dottedQuad(network);
    }

    public static String calculateBroadcast(String ipAddress, String netmask) throws KuraException {
        int ipAddressValue = NetworkUtil.convertIp4Address(ipAddress);
        int netmaskValue = NetworkUtil.convertIp4Address(netmask);
        
        int network = ipAddressValue | ~netmaskValue;
        return dottedQuad(network);
    }

    public static String getNetmaskStringForm(int prefix) throws KuraException {
        if (prefix >= 1 && prefix <= 32) {
            int mask = ~((1 << 32 - prefix) - 1);
            return dottedQuad(mask);
        } else {
            throw new KuraException(KuraErrorCode.INTERNAL_ERROR, "invalid prefix ");
        }
    }

    public static short getNetmaskShortForm(String netmask) throws KuraException {
        if (netmask == null) {
            throw new KuraException(KuraErrorCode.INTERNAL_ERROR, "netmask is null");
        }

        int netmaskValue = NetworkUtil.convertIp4Address(netmask);

        boolean hitZero = false;
        int displayMask = 1 << 31;
        int count = 0;

        for (int c = 1; c <= 32; c++) {
            if ((netmaskValue & displayMask) == 0) {
                hitZero = true;
            } else {
                if (hitZero) {
                    s_logger.error("received invalid mask: " + netmask);
                    throw new KuraException(KuraErrorCode.INTERNAL_ERROR, "received invalid mask: " + netmask);
                }

                count++;
            }

            netmaskValue <<= 1;
        }

        return (short) count;
    }

    public static String dottedQuad(int ip) {
        String[] items = new String[4];
        for (int i = 3; i >= 0; i--) {
            int value = ip & 0xFF;
            items[i] = Integer.toString(value);
            ip = ip >>> 8;
        }
        
        String dottedValue = String.join(".", items);
        return dottedValue;
    }

    public static int convertIp4Address(String ipAddress) throws KuraException {
        if (ipAddress == null) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }
        
        String[] splitIpAddress = ipAddress.split("\\.");
        
        if (splitIpAddress.length != 4) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }
        
        short[] addressBytes = new short[4];

        for (int i = 0; i < 4; i++) {
            String octet = splitIpAddress[i];
            addressBytes[i] = Short.parseShort(octet);
        }

        return NetworkUtil.packIp4AddressBytes(addressBytes);
    }

    public static int packIp4AddressBytes(short[] bytes) throws KuraException {
        if ((bytes == null) || (bytes.length != 4)) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }
        
        int val = 0;
        for (int i = 0; i < 4; i++) {
            if ((bytes[i] < 0) || (bytes[i] > 255)) {
                throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
            }
            
            val = val << 8;
            val |= bytes[i];
        }
        return val;
    }

    public static short[] unpackIP4AddressInt(int address) {
        short[] addressBytes = new short[4];
        int value = address;
        
        for (int i = 3; i >= 0; i--) {
            addressBytes[i] = (short)(value & 0xFF);
            value = value >>> 8;
        }
        
        return addressBytes;
    }

    public static byte[] convertIP6Address(String fullFormIP6Address) throws KuraException {
        if (fullFormIP6Address == null) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }

        byte[] retVal = new byte[16];
        String[] ip6Split = fullFormIP6Address.split(":");

        if (ip6Split.length != 8) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }

        for (int i = 0; i < 8; i++) {
            try {
                String octet = ip6Split[i];
                int value = Integer.parseInt(octet, 16);
                
                if ((value < 0) || (value > 0xFFFF))
                {
                    throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
                }
                
                int k = i * 2;
                retVal[k] = (byte) ((value >>> 8) & 0xFF);
                retVal[k + 1] = (byte) (value & 0xFF);
            } catch (NumberFormatException e) {
                throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
            }
        }

        return retVal;
    }

    public static String convertIP6Address(byte[] bytes) throws KuraException {
        if ((bytes == null) || (bytes.length != 16)) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }

        String[] items = new String[8];
        for (int i = 0; i < 8; i++) {
            int k = i * 2;
            int value = (bytes[k] << 8) & 0xFF00;
            value |= bytes[k + 1] & 0xFF;
            items[i] = Integer.toHexString(value);
        }
        
        String ipAddress = String.join(":", items);
        return ipAddress;
    }

    public static String macToString(byte[] mac) throws KuraException {
        if ((mac == null) || (mac.length != 6)) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }
        
        String[] items = new String[6];
        for (int i = 0; i < 6; i++) {
            String octet = Integer.toHexString(mac[i] & 0xFF).toUpperCase();
            if (octet.length() == 1) {
                octet = "0" + octet;
            }
            
            items[i] = octet;
        }
        
        String macString = String.join(":", items);
        return macString;
    }

    public static byte[] macToBytes(String mac) throws KuraException {
        if (mac == null) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }

        String[] items = mac.split("\\:");
        
        if (items.length != 6) {
            throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
        }
        
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            String item = items[i];
            if (item.isEmpty() || (item.length() > 2)) {
                throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
            }
            
            try {
                bytes[i] = (byte) Integer.parseInt(items[i], 16);
            } catch (NumberFormatException e) {
                throw new KuraException(KuraErrorCode.INVALID_PARAMETER);
            }
        }

        return bytes;
    }
}
