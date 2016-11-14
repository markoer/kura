package org.eclipse.kura.core.net;

import java.util.List;

import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetInterfaceAddressConfig;

public interface MockInterfaceAddressConfig extends NetInterfaceAddressConfig {

    /**
     * Returns a List of NetConfig Objects associated with a given WifiInterfaceAddressConfig
     * for a given WifiInterface
     *
     * @return the NetConfig Objects associated with the WifiInterfaceAddressConfig
     */
    @Override
    public List<NetConfig> getConfigs();
}
