/* KeyPairCodecFactory.java --
   Copyright 2001, 2002, 2006 Free Software Foundation, Inc.

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

package com.distrimind.gnu.jgnu.security.key;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnu.security.key.dss.DSSKeyPairPKCS8Codec;
import com.distrimind.gnu.jgnu.security.key.dss.DSSKeyPairRawCodec;
import com.distrimind.gnu.jgnu.security.key.dss.DSSKeyPairX509Codec;
import com.distrimind.gnu.jgnu.security.key.dss.DSSPrivateKey;
import com.distrimind.gnu.jgnu.security.key.dss.DSSPublicKey;
import com.distrimind.gnu.jgnu.security.key.rsa.GnuRSAPrivateKey;
import com.distrimind.gnu.jgnu.security.key.rsa.GnuRSAPublicKey;
import com.distrimind.gnu.jgnu.security.key.rsa.RSAKeyPairPKCS8Codec;
import com.distrimind.gnu.jgnu.security.key.rsa.RSAKeyPairRawCodec;
import com.distrimind.gnu.jgnu.security.key.rsa.RSAKeyPairX509Codec;
import com.distrimind.gnu.jgnu.security.util.FormatUtil;
import com.distrimind.gnu.vm.jgnu.security.Key;

/**
 * A <i>Factory</i> class to instantiate key encoder/decoder instances.
 */
public class KeyPairCodecFactory {
	private static Set<String> names;

	/**
	 * Returns an instance of a keypair codec given a key.
	 *
	 * @param key
	 *            the key to encode.
	 * @return an instance of the keypair codec, or <code>null</code> if none found.
	 */
	public static IKeyPairCodec getInstance(Key key) {
		if (key == null)
			return null;

		String format = key.getFormat();
		int formatID = FormatUtil.getFormatID(format);
		if (formatID == 0)
			return null;

		switch (formatID) {
		case Registry.RAW_ENCODING_ID:
			return getRawCodec(key);
		case Registry.X509_ENCODING_ID:
			return getX509Codec(key);
		case Registry.PKCS8_ENCODING_ID:
			return getPKCS8Codec(key);
		}

		return null;
	}

	/**
	 * Returns the appropriate codec given a composed key-pair generator algorithm
	 * and an encoding format. A composed name is formed by the concatenation of the
	 * canonical key-pair algorithm name, the forward slash character <code>/</code>
	 * and the canonical name of the encoding format.
	 * <p>
	 * <b>IMPORTANT</b>: For backward compatibility, when the encoding format name
	 * is missing, the Raw encoding format is assumed. When this is the case the
	 * trailing forward slash is discarded from the name.
	 *
	 * @param name
	 *            the case-insensitive key codec name.
	 * @return an instance of the keypair codec, or <code>null</code> if none found.
	 */
	public static IKeyPairCodec getInstance(String name) {
		if (name == null)
			return null;

		name = name.trim();
		if (name.length() == 0)
			return null;

		if (name.startsWith("/"))
			return null;

		if (name.endsWith("/"))
			return getInstance(name.substring(0, name.length() - 1), Registry.RAW_ENCODING_ID);

		int i = name.indexOf("/");
		if (i == -1)
			return getInstance(name, Registry.RAW_ENCODING_ID);

		String kpgName = name.substring(0, i);
		String formatName = name.substring(i + 1);
		return getInstance(kpgName, formatName);
	}

	/**
	 * Returns an instance of a keypair codec given the canonical name of the
	 * key-pair algorithm, and the identifier of the format to use when
	 * externalizing the keys.
	 *
	 * @param name
	 *            the case-insensitive key-pair algorithm name.
	 * @param formatID
	 *            the identifier of the format to use when externalizing the keys
	 *            generated by the key-pair algorithm.
	 * @return an instance of the key-pair codec, or <code>null</code> if none
	 *         found.
	 */
	public static IKeyPairCodec getInstance(String name, int formatID) {
		if (name == null)
			return null;

		name = name.trim();
		switch (formatID) {
		case Registry.RAW_ENCODING_ID:
			return getRawCodec(name);
		case Registry.X509_ENCODING_ID:
			return getX509Codec(name);
		case Registry.PKCS8_ENCODING_ID:
			return getPKCS8Codec(name);
		}

		return null;
	}

	/**
	 * Returns an instance of a keypair codec given the canonical name of the
	 * key-pair algorithm, and the name of the encoding format to use when
	 * externalizing the keys.
	 *
	 * @param name
	 *            the case-insensitive key-pair algorithm name.
	 * @param format
	 *            the name of the encoding format to use when externalizing the keys
	 *            generated by the key-pair algorithm.
	 * @return an instance of the key-pair codec, or <code>null</code> if none
	 *         found.
	 */
	public static IKeyPairCodec getInstance(String name, String format) {
		int formatID = FormatUtil.getFormatID(format);
		if (formatID == 0)
			return null;

		return getInstance(name, formatID);
	}

	/**
	 * Returns a {@link Set} of supported key-pair codec names.
	 *
	 * @return a {@link Set} of the names of supported key-pair codec (Strings).
	 */
	public static synchronized final Set<String> getNames() {
		if (names == null) {
			HashSet<String> hs = new HashSet<>();
			hs.add(Registry.DSS_KPG + "/" + Registry.RAW_ENCODING_SHORT_NAME);
			hs.add(Registry.DSS_KPG + "/" + Registry.X509_ENCODING_SORT_NAME);
			hs.add(Registry.DSS_KPG + "/" + Registry.PKCS8_ENCODING_SHORT_NAME);
			hs.add(Registry.RSA_KPG + "/" + Registry.RAW_ENCODING_SHORT_NAME);
			hs.add(Registry.RSA_KPG + "/" + Registry.X509_ENCODING_SORT_NAME);
			hs.add(Registry.RSA_KPG + "/" + Registry.PKCS8_ENCODING_SHORT_NAME);
			hs.add(Registry.DH_KPG + "/" + Registry.RAW_ENCODING_SHORT_NAME);
			hs.add(Registry.SRP_KPG + "/" + Registry.RAW_ENCODING_SHORT_NAME);
			names = Collections.unmodifiableSet(hs);
		}
		return names;
	}

	/**
	 * @param key
	 *            a {@link Key} for which we want to return a PKCS#8 codec.
	 * @return the PKCS#8 codec corresponding to the key, or <code>null</code> if
	 *         none exists for this key.
	 */
	private static IKeyPairCodec getPKCS8Codec(Key key) {
		IKeyPairCodec result = null;
		if (key instanceof DSSPrivateKey)
			result = new DSSKeyPairPKCS8Codec();
		else if (key instanceof GnuRSAPrivateKey)
			result = new RSAKeyPairPKCS8Codec();

		return result;
	}

	/**
	 * @param name
	 *            the trimmed name of a key-pair algorithm.
	 * @return a PKCS#8 format codec for the designated key-pair algorithm, or
	 *         <code>null</code> if none exists.
	 */
	private static IKeyPairCodec getPKCS8Codec(String name) {
		IKeyPairCodec result = null;
		if (name.equalsIgnoreCase(Registry.DSA_KPG) || name.equals(Registry.DSS_KPG))
			result = new DSSKeyPairPKCS8Codec();
		else if (name.equalsIgnoreCase(Registry.RSA_KPG))
			result = new RSAKeyPairPKCS8Codec();
		else if (name.equalsIgnoreCase(Registry.DH_KPG))
			result = makeInstance("com.distrimind.gnu.javax.crypto.key.dh.DHKeyPairPKCS8Codec");

		return result;
	}

	/**
	 * @param key
	 *            a {@link Key} for which we want to return a Raw codec.
	 * @return the Raw codec corresponding to the key, or <code>null</code> if none
	 *         exists for this key.
	 */
	private static IKeyPairCodec getRawCodec(Key key) {
		IKeyPairCodec result = null;
		if ((key instanceof DSSPublicKey) || (key instanceof DSSPrivateKey))
			result = new DSSKeyPairRawCodec();
		else if ((key instanceof GnuRSAPublicKey) || (key instanceof GnuRSAPrivateKey))
			result = new RSAKeyPairRawCodec();
		else if (matches(key, "com.distrimind.gnu.javax.crypto.key.dh.GnuDHPublicKey")
				|| matches(key, "com.distrimind.gnu.javax.crypto.key.dh.GnuDHPrivateKey"))
			result = makeInstance("com.distrimind.gnu.javax.crypto.key.dh.DHKeyPairRawCodec");
		else if (matches(key, "com.distrimind.gnu.javax.crypto.key.srp6.SRPPublicKey")
				|| matches(key, "com.distrimind.gnu.javax.crypto.key.srp6.SRPPrivateKey"))
			result = makeInstance("com.distrimind.gnu.javax.crypto.key.srp6.SRPKeyPairRawCodec");

		return result;
	}

	/**
	 * @param name
	 *            the trimmed name of a key-pair algorithm.
	 * @return a Raw format codec for the designated key-pair algorithm, or
	 *         <code>null</code> if none exists.
	 */
	private static IKeyPairCodec getRawCodec(String name) {
		IKeyPairCodec result = null;
		if (name.equalsIgnoreCase(Registry.DSA_KPG) || name.equals(Registry.DSS_KPG))
			result = new DSSKeyPairRawCodec();
		else if (name.equalsIgnoreCase(Registry.RSA_KPG))
			result = new RSAKeyPairRawCodec();
		else if (name.equalsIgnoreCase(Registry.DH_KPG))
			result = makeInstance("com.distrimind.gnu.javax.crypto.key.dh.DHKeyPairRawCodec");
		else if (name.equalsIgnoreCase(Registry.SRP_KPG))
			result = makeInstance("com.distrimind.gnu.javax.crypto.key.srp6.SRPKeyPairRawCodec");

		return result;
	}

	/**
	 * @param key
	 *            a {@link Key} for which we want to return an X.509 codec.
	 * @return the X.509 codec corresponding to the key, or <code>null</code> if
	 *         none exists for this key.
	 */
	private static IKeyPairCodec getX509Codec(Key key) {
		IKeyPairCodec result = null;
		if (key instanceof DSSPublicKey)
			result = new DSSKeyPairX509Codec();
		else if (key instanceof GnuRSAPublicKey)
			result = new RSAKeyPairX509Codec();

		return result;
	}

	/**
	 * @param name
	 *            the trimmed name of a key-pair algorithm.
	 * @return a X.509 format codec for the designated key-pair algorithm, or
	 *         <code>null</code> if none exists.
	 */
	private static IKeyPairCodec getX509Codec(String name) {
		IKeyPairCodec result = null;
		if (name.equalsIgnoreCase(Registry.DSA_KPG) || name.equals(Registry.DSS_KPG))
			result = new DSSKeyPairX509Codec();
		else if (name.equalsIgnoreCase(Registry.RSA_KPG))
			result = new RSAKeyPairX509Codec();
		else if (name.equalsIgnoreCase(Registry.DH_KPG))
			result = makeInstance("com.distrimind.gnu.javax.crypto.key.dh.DHKeyPairX509Codec");

		return result;
	}

	private static IKeyPairCodec makeInstance(String clazz) {
		try {
			Class<?> c = Class.forName(clazz);
			Constructor<?> ctor = c.getConstructor(new Class[0]);
			return (IKeyPairCodec) ctor.newInstance(new Object[0]);
		} catch (Exception x) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"strong crypto key codec not available: " + clazz);
			iae.initCause(x);
			throw iae;
		}
	}

	private static boolean matches(Object o, String clazz) {
		try {
			Class<?> c = Class.forName(clazz);
			return c.isAssignableFrom(o.getClass());
		} catch (Exception x) {
			// Can't match.
			return false;
		}
	}

	/** Trivial constructor to enforce Singleton pattern. */
	private KeyPairCodecFactory() {
		super();
	}
}
