/*******************************************************************************
 * Copyright (c) 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.kura.core.net;

import java.util.List;

import org.eclipse.kura.net.IPAddress;
import org.eclipse.kura.net.NetInterfaceAddress;

public class MockInterfaceAddressImpl implements NetInterfaceAddress {

	@Override
	public IPAddress getAddress() {
		return null;
	}

	@Override
	public short getNetworkPrefixLength() {
		return 0;
	}

	@Override
	public IPAddress getNetmask() {
		return null;
	}

	@Override
	public IPAddress getGateway() {
		return null;
	}

	@Override
	public IPAddress getBroadcast() {
		return null;
	}

	@Override
	public List<? extends IPAddress> getDnsServers() {
		return null;
	}

}
