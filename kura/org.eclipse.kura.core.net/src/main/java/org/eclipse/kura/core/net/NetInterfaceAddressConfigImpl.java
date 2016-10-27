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
package org.eclipse.kura.core.net;

import java.util.List;

import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetInterfaceAddress;
import org.eclipse.kura.net.NetInterfaceAddressConfig;

public class NetInterfaceAddressConfigImpl extends NetInterfaceAddressImpl implements NetInterfaceAddressConfig {

    private List<NetConfig> m_configs;

    public NetInterfaceAddressConfigImpl() {
        super();
    }

    public NetInterfaceAddressConfigImpl(NetInterfaceAddress other) {
        super(other);
    }

    @Override
    public List<NetConfig> getConfigs() {
        return this.m_configs;
    }

    public void setNetConfigs(List<NetConfig> configs) {
        this.m_configs = configs;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        /*
         * if(!super.equals(obj)) {
         * return false;
         * }
         */
        if (!(obj instanceof NetInterfaceAddressConfigImpl)) {
            return false;
        }

        NetInterfaceAddressConfigImpl other = (NetInterfaceAddressConfigImpl) obj;

        List<NetConfig> thisNetConfigs = getConfigs();
        List<NetConfig> otherNetConfigs = other.getConfigs();
        
        if ((thisNetConfigs == null) && (otherNetConfigs == null)) {
        	// Both configurations are null
        	return true;
        }
        else if ((thisNetConfigs == null) || (otherNetConfigs == null)) {
        	// One configuration is null but the other one is not, so a null pointer exception would be thrown below!
            return false;
        }

        if (thisNetConfigs.size() != otherNetConfigs.size()) {
            return false;
        }
        if (!thisNetConfigs.containsAll(otherNetConfigs)) {
            return false;
        }
        if (!otherNetConfigs.containsAll(thisNetConfigs)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        if (this.m_configs != null) {
            StringBuffer sb = new StringBuffer();
            for (NetConfig netConfig : this.m_configs) {
                sb.append("NetConfig: ");
                if (netConfig != null) {
                    sb.append(netConfig.toString());
                } else {
                    sb.append("null");
                }
                sb.append(" - ");
            }

            return sb.toString();
        } else {
            return "NetConfig: no configurations";
        }
    }
}
