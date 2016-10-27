package org.eclipse.kura.linux.net.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.net.WifiAccessPointImpl;
import org.eclipse.kura.core.net.util.NetworkUtil;
import org.eclipse.kura.net.wifi.WifiAccessPoint;
import org.eclipse.kura.net.wifi.WifiMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumes AP property lines from iw scan command.
 */
class IWAPParser {

    private static final Logger s_logger = LoggerFactory.getLogger(IWAPParser.class);

    // Top-level properties we'll be picking up
    private String ssid = null;
    private List<Long> bitrate = null;
    private long frequency = -1;
    private byte[] hardwareAddress = null;
    private int strength = -1;
    private List<String> capabilities = null;

    // For parsing the security sections
    private final IWSecuritySectionParser rsn = new IWSecuritySectionParser();
    private final IWSecuritySectionParser wpa = new IWSecuritySectionParser();

    // Current security section parser, or null if we're currently parsing toplevel things
    private IWSecuritySectionParser securityParser = null;

    public IWAPParser(String macAddressString) {
        try {
            this.hardwareAddress = NetworkUtil.macToBytes(macAddressString);
        } catch (KuraException e) {
            // Invalid MAC address string
            this.hardwareAddress = null;
        }
    }

    public WifiAccessPoint toWifiAccessPoint() {
        WifiAccessPointImpl wifiAccessPoint = new WifiAccessPointImpl(this.ssid);
        wifiAccessPoint.setBitrate(this.bitrate);
        wifiAccessPoint.setFrequency(this.frequency);
        wifiAccessPoint.setHardwareAddress(this.hardwareAddress);
        wifiAccessPoint.setMode(WifiMode.MASTER);				// FIME - is this right? - always MASTER - or maybe
				// AD-HOC too?
        wifiAccessPoint.setRsnSecurity(this.rsn.getWifiSecurityFlags());
        wifiAccessPoint.setStrength(this.strength);
        wifiAccessPoint.setWpaSecurity(this.wpa.getWifiSecurityFlags());
        if (this.capabilities != null && this.capabilities.size() > 0) {
            wifiAccessPoint.setCapabilities(this.capabilities);
        }
        return wifiAccessPoint;
    }

    /**
     * @param propLine
     *            A trimmed line from iw scan.
     * @throws Exception
     *             Something went wrong for this AP.
     */
    public void parsePropLine(String propLine) throws Exception {

        if (this.securityParser != null) {
            // We're parsing a section right now
            boolean done = this.securityParser.parsePropLine(propLine);

            if (done) {
                this.securityParser = null;
            }

        } else if (propLine.startsWith("freq:")) {
            StringTokenizer st = new StringTokenizer(propLine, " ");
            st.nextToken();	// eat freq:
            this.frequency = Long.parseLong(st.nextToken());

        } else if (propLine.startsWith("SSID: ")) {
            this.ssid = propLine.substring(5).trim();

        } else if (propLine.startsWith("RSN:")) {
            this.securityParser = this.rsn;
            parsePropLine(propLine);

        } else if (propLine.startsWith("WPA:")) {
            this.securityParser = this.wpa;
            parsePropLine(propLine);

        } else if (propLine.startsWith("Supported rates: ")) {
            // Supported rates: 1.0* 2.0* 5.5* 11.0* 18.0 24.0 36.0 54.0
            if (this.bitrate == null) {
                this.bitrate = new ArrayList<Long>();
            }
            String[] rateStrings = propLine.replaceFirst("Supported rates: ", "").replaceAll("\\*", "").trim()
                    .split(" ");
            for (String rateString : rateStrings) {
                this.bitrate.add((long) (Float.parseFloat(rateString) * 1000000));
            }

        } else if (propLine.startsWith("Extended supported rates: ")) {
            // Extended supported rates: 6.0 9.0 12.0 48.0
            if (this.bitrate == null) {
                this.bitrate = new ArrayList<Long>();
            }

            String[] rateStrings = propLine.replaceFirst("Extended supported rates: ", "").replaceAll("\\*", "").trim()
                    .split(" ");
            for (String rateString : rateStrings) {
                this.bitrate.add((long) (Float.parseFloat(rateString) * 1000000));
            }

        } else if (propLine.startsWith("signal:")) {
            // signal: -56.00 dBm
            StringTokenizer st = new StringTokenizer(propLine, " ");
            st.nextToken(); // eat signal:
            final String strengthRaw = st.nextToken();
            if (strengthRaw.contains("/")) {
                // Could also be of format 39/100
                final String[] parts = strengthRaw.split("/");
                this.strength = (int) Float.parseFloat(parts[0]);
                this.strength = SignalStrengthConversion.getRssi(this.strength);
            } else {
                this.strength = Math.abs((int) Float.parseFloat(strengthRaw));
            }
        } else if (propLine.startsWith("capability:")) {
            this.capabilities = new ArrayList<String>();
            String newLine = propLine.substring("capability:".length()).trim();
            StringTokenizer st = new StringTokenizer(newLine, " ");
            while (st.hasMoreTokens()) {
                this.capabilities.add(st.nextToken());
            }
        } else {
            s_logger.debug("Ignoring line in scan result: {}", propLine);
        }
    }
}
