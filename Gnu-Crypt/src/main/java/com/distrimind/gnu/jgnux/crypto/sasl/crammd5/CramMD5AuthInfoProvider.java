/* CramMD5AuthInfoProvider.java --
   Copyright (C) 2003, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.  */

package com.distrimind.gnu.jgnux.crypto.sasl.crammd5;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.sasl.AuthenticationException;

import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnux.crypto.sasl.IAuthInfoProvider;
import com.distrimind.gnu.jgnux.crypto.sasl.NoSuchUserException;

/**
 * The CRAM-MD5 mechanism authentication information provider implementation.
 */
public class CramMD5AuthInfoProvider implements IAuthInfoProvider {
	private PasswordFile passwordFile = null;

	// implicit 0-args constrcutor

	@Override
	public void activate(Map<String, Object> context) throws AuthenticationException {
		try {
			if (context == null)
				passwordFile = new PasswordFile();
			else {
				String pfn = (String) context.get(CramMD5Registry.PASSWORD_FILE);
				if (pfn == null)
					passwordFile = new PasswordFile();
				else
					passwordFile = new PasswordFile(pfn);
			}
		} catch (IOException x) {
			throw new AuthenticationException("activate()", x);
		}
	}

	@Override
	public boolean contains(String userName) throws AuthenticationException {
		if (passwordFile == null)
			throw new AuthenticationException("contains()", new IllegalStateException());
		boolean result = false;
		try {
			result = passwordFile.contains(userName);
		} catch (IOException x) {
			throw new AuthenticationException("contains()", x);
		}
		return result;
	}

	@Override
	public Map<String, String> getConfiguration(String mode) throws AuthenticationException {
		throw new AuthenticationException("", new UnsupportedOperationException());
	}

	@Override
	public Map<String, String> lookup(Map<String, String> userID) throws AuthenticationException {
		if (passwordFile == null)
			throw new AuthenticationException("lookup()", new IllegalStateException());
		Map<String, String> result = new HashMap<>();
		try {
			String userName = userID.get(Registry.SASL_USERNAME);
			if (userName == null)
				throw new NoSuchUserException("");
			String[] data = passwordFile.lookup(userName);
			result.put(Registry.SASL_USERNAME, data[0]);
			result.put(Registry.SASL_PASSWORD, data[1]);
			result.put(CramMD5Registry.UID_FIELD, data[2]);
			result.put(CramMD5Registry.GID_FIELD, data[3]);
			result.put(CramMD5Registry.GECOS_FIELD, data[4]);
			result.put(CramMD5Registry.DIR_FIELD, data[5]);
			result.put(CramMD5Registry.SHELL_FIELD, data[6]);
		} catch (Exception x) {
			if (x instanceof AuthenticationException)
				throw (AuthenticationException) x;
			throw new AuthenticationException("lookup()", x);
		}
		return result;
	}

	@Override
	public void passivate() {
		passwordFile = null;
	}

	@Override
	public void update(Map<String, String> userCredentials) throws AuthenticationException {
		if (passwordFile == null)
			throw new AuthenticationException("update()", new IllegalStateException());
		try {
			String userName = userCredentials.get(Registry.SASL_USERNAME);
			String password = userCredentials.get(Registry.SASL_PASSWORD);
			String uid = userCredentials.get(CramMD5Registry.UID_FIELD);
			String gid = userCredentials.get(CramMD5Registry.GID_FIELD);
			String gecos = userCredentials.get(CramMD5Registry.GECOS_FIELD);
			String dir = userCredentials.get(CramMD5Registry.DIR_FIELD);
			String shell = userCredentials.get(CramMD5Registry.SHELL_FIELD);
			if (uid == null || gid == null || gecos == null || dir == null || shell == null)
				passwordFile.changePasswd(userName, password);
			else {
				String[] attributes = new String[] { uid, gid, gecos, dir, shell };
				passwordFile.add(userName, password, attributes);
			}
		} catch (Exception x) {
			if (x instanceof AuthenticationException)
				throw (AuthenticationException) x;
			throw new AuthenticationException("update()", x);
		}
	}
}
