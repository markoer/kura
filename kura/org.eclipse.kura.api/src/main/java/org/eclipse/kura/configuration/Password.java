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
package org.eclipse.kura.configuration;

import java.util.Arrays;

public class Password {

    private char[] m_password;

    public Password(String password) {
        super();
        if (password != null) {
            this.m_password = password.toCharArray();
        }
    }

    public Password(char[] password) {
        super();
        this.m_password = password;
    }

    public char[] getPassword() {
        return this.m_password;
    }

    @Override
    public String toString() {
        return new String(this.m_password);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(m_password);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Password)) {
			return false;
		}
		Password other = (Password) obj;
		if (!Arrays.equals(m_password, other.m_password)) {
			return false;
		}
		return true;
	}
}
