/* GnuSasl.java -- javax.security.sasl algorithms.
   Copyright (C) 2004, 2006 Free Software Foundation, Inc.

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

package com.distrimind.gnu.jgnux.crypto.jce;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnux.crypto.sasl.ClientFactory;
import com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory;
import com.distrimind.gnu.vm.jgnu.security.Provider;

public final class GnuSasl extends Provider {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1937387304609256912L;

	/**
	 * Returns a {@link Set} of names of SASL Client mechanisms available from this
	 * {@link Provider}.
	 *
	 * @return a {@link Set} of SASL Client mechanisms (Strings).
	 */
	public static final Set<String> getSaslClientMechanismNames() {
		return ClientFactory.getNames();
	}

	/**
	 * Returns a {@link Set} of names of SASL Server mechanisms available from this
	 * {@link Provider}.
	 *
	 * @return a {@link Set} of SASL Server mechanisms (Strings).
	 */
	public static final Set<String> getSaslServerMechanismNames() {
		return ServerFactory.getNames();
	}

	public GnuSasl() {
		super(Registry.GNU_SASL, 2.1, "GNU SASL Provider");

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				// SASL Client and Server mechanisms
				put("SaslClientFactory.ANONYMOUS", com.distrimind.gnu.jgnux.crypto.sasl.ClientFactory.class.getName());
				put("SaslClientFactory.PLAIN", com.distrimind.gnu.jgnux.crypto.sasl.ClientFactory.class.getName());
				put("SaslClientFactory.CRAM-MD5", com.distrimind.gnu.jgnux.crypto.sasl.ClientFactory.class.getName());
				put("SaslClientFactory.SRP", com.distrimind.gnu.jgnux.crypto.sasl.ClientFactory.class.getName());

				put("SaslServerFactory.ANONYMOUS", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.PLAIN", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.CRAM-MD5", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.SRP-MD5", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.SRP-SHA-160", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.SRP-RIPEMD128", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.SRP-RIPEMD160", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.SRP-TIGER", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());
				put("SaslServerFactory.SRP-WHIRLPOOL", com.distrimind.gnu.jgnux.crypto.sasl.ServerFactory.class.getName());

				put("Alg.Alias.SaslServerFactory.SRP-SHS", "SRP-SHA-160");
				put("Alg.Alias.SaslServerFactory.SRP-SHA", "SRP-SHA-160");
				put("Alg.Alias.SaslServerFactory.SRP-SHA1", "SRP-SHA-160");
				put("Alg.Alias.SaslServerFactory.SRP-SHA-1", "SRP-SHA-160");
				put("Alg.Alias.SaslServerFactory.SRP-SHA160", "SRP-SHA-160");
				put("Alg.Alias.SaslServerFactory.SRP-RIPEMD-128", "SRP-RIPEMD128");
				put("Alg.Alias.SaslServerFactory.SRP-RIPEMD-160", "SRP-RIPEMD160");

				return null;
			}
		});
	}
}
