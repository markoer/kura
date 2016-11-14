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

import org.eclipse.kura.net.NetConfig;

public class MockInterfaceAddressConfigImpl extends MockInterfaceAddressImpl implements MockInterfaceAddressConfig {

	private List<NetConfig> configs;

	@Override
	public List<NetConfig> getConfigs() {
		return this.configs;
	}

	public void setNetConfigs(List<NetConfig> configs) {
		this.configs = configs;
	}
}
