/* EncodedKeyFactory.java -- JCE Encoded key factory Adapter
   Copyright (C) 2006, 2010  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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
exception statement from your version. */

package com.distrimind.gnu.jgnu.security.jce.sig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnu.security.key.dss.DSSPrivateKey;
import com.distrimind.gnu.jgnu.security.key.dss.DSSPublicKey;
import com.distrimind.gnu.jgnu.security.key.rsa.GnuRSAPrivateKey;
import com.distrimind.gnu.jgnu.security.key.rsa.GnuRSAPublicKey;
import com.distrimind.gnu.vm.jgnu.security.InvalidKeyException;
import com.distrimind.gnu.vm.jgnu.security.InvalidParameterException;
import com.distrimind.gnu.vm.jgnu.security.Key;
import com.distrimind.gnu.vm.jgnu.security.KeyFactorySpi;
import com.distrimind.gnu.vm.jgnu.security.PrivateKey;
import com.distrimind.gnu.vm.jgnu.security.PublicKey;
import com.distrimind.gnu.vm.jgnu.security.spec.DSAPrivateKeySpec;
import com.distrimind.gnu.vm.jgnu.security.spec.DSAPublicKeySpec;
import com.distrimind.gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import com.distrimind.gnu.vm.jgnu.security.spec.KeySpec;
import com.distrimind.gnu.vm.jgnu.security.spec.PKCS8EncodedKeySpec;
import com.distrimind.gnu.vm.jgnu.security.spec.RSAPrivateCrtKeySpec;
import com.distrimind.gnu.vm.jgnu.security.spec.RSAPublicKeySpec;
import com.distrimind.gnu.vm.jgnu.security.spec.X509EncodedKeySpec;
import com.distrimind.gnu.vm.jgnux.crypto.interfaces.DHPrivateKey;
import com.distrimind.gnu.vm.jgnux.crypto.interfaces.DHPublicKey;
import com.distrimind.gnu.vm.jgnux.crypto.spec.DHPrivateKeySpec;
import com.distrimind.gnu.vm.jgnux.crypto.spec.DHPublicKeySpec;

/**
 * A factory for keys encoded in either the X.509 format (for public keys) or
 * the PKCS#8 format (for private keys).
 */
public class EncodedKeyFactory extends KeyFactorySpi {

	private static Class<?> getConcreteClass(String className) throws InvalidKeySpecException {
		try {
			Class<?> result = Class.forName(className);
			return result;
		} catch (ClassNotFoundException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		}
	}

	private static Constructor<?> getConcreteCtor(Class<?> clazz) throws InvalidKeySpecException {
		try {
			Constructor<?> result = clazz.getConstructor(
					new Class[] { int.class, BigInteger.class, BigInteger.class, BigInteger.class, BigInteger.class });
			return result;
		} catch (NoSuchMethodException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		}
	}

	private static Method getValueOfMethod(Class<?> clazz) throws InvalidKeySpecException {
		try {
			Method result = clazz.getMethod("valueOf", new Class[] { byte[].class });
			return result;
		} catch (NoSuchMethodException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		}
	}

	private static Object invokeConstructor(String className, Object[] params) throws InvalidKeySpecException {
		Class<?> clazz = getConcreteClass(className);
		try {
			Constructor<?> ctor = getConcreteCtor(clazz);
			Object result = ctor.newInstance(params);
			return result;
		} catch (InstantiationException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		} catch (IllegalAccessException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		} catch (InvocationTargetException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		}
	}

	private static Object invokeValueOf(String className, byte[] encoded) throws InvalidKeySpecException {
		Class<?> clazz = getConcreteClass(className);
		try {
			Method valueOf = getValueOfMethod(clazz);
			Object result = valueOf.invoke(null, new Object[] { encoded });
			return result;
		} catch (IllegalAccessException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		} catch (InvocationTargetException x) {
			throw new InvalidKeySpecException(x.getMessage(), x);
		}
	}

	/**
	 * @param encoded
	 *            the bytes to decode.
	 * @return an instance of a {@link DHPrivateKey} constructed from the
	 *         information in the designated key-specification.
	 * @throws InvalidKeySpecException
	 *             if no concrete implementation of the {@link DHPrivateKey}
	 *             interface exists at run-time, or if an exception occurs during
	 *             its instantiation.
	 */
	private DHPrivateKey decodeDHPrivateKey(byte[] encoded) throws InvalidKeySpecException {
		Object obj = invokeValueOf("com.distrimind.gnu.javax.crypto.key.dh.GnuDHPrivateKey", encoded);
		return (DHPrivateKey) obj;
	}

	/**
	 * @param spec
	 *            an instance of {@link DHPrivateKeySpec} to decode.
	 * @return an instance of a {@link DHPrivateKey} constructed from the
	 *         information in the designated key-specification.
	 * @throws InvalidKeySpecException
	 *             if no concrete implementation of the {@link DHPrivateKey}
	 *             interface exists at run-time, or if an exception occurs during
	 *             its instantiation.
	 */
	private DHPrivateKey decodeDHPrivateKey(DHPrivateKeySpec spec) throws InvalidKeySpecException {
		BigInteger p = spec.getP();
		BigInteger g = spec.getG();
		BigInteger x = spec.getX();
		Object[] params = new Object[] { Integer.valueOf(Registry.PKCS8_ENCODING_ID), null, p, g, x };
		Object obj = invokeConstructor("com.distrimind.gnu.javax.crypto.key.dh.GnuDHPrivateKey", params);
		return (DHPrivateKey) obj;
	}

	/**
	 * @param encoded
	 *            the bytes to decode.
	 * @return an instance of a {@link DHPublicKey} constructed from the information
	 *         in the designated key-specification.
	 * @throws InvalidKeySpecException
	 *             if no concrete implementation of the {@link DHPublicKey}
	 *             interface exists at run-time, or if an exception occurs during
	 *             its instantiation.
	 */
	private DHPublicKey decodeDHPublicKey(byte[] encoded) throws InvalidKeySpecException {
		Object obj = invokeValueOf("com.distrimind.gnu.javax.crypto.key.dh.GnuDHPublicKey", encoded);
		return (DHPublicKey) obj;
	}

	/**
	 * @param spec
	 *            an instance of {@link DHPublicKeySpec} to decode.
	 * @return an instance of a {@link DHPublicKey} constructed from the information
	 *         in the designated key-specification.
	 * @throws InvalidKeySpecException
	 *             if no concrete implementation of the {@link DHPublicKey}
	 *             interface exists at run-time, or if an exception occurs during
	 *             its instantiation.
	 */
	private DHPublicKey decodeDHPublicKey(DHPublicKeySpec spec) throws InvalidKeySpecException {
		BigInteger p = spec.getP();
		BigInteger g = spec.getG();
		BigInteger y = spec.getY();
		Object[] params = new Object[] { Integer.valueOf(Registry.X509_ENCODING_ID), null, p, g, y };
		Object obj = invokeConstructor("com.distrimind.gnu.javax.crypto.key.dh.GnuDHPublicKey", params);
		return (DHPublicKey) obj;
	}

	/**
	 * @param spec
	 *            an instance of {@link DSAPrivateKeySpec} to decode.
	 * @return an instance of {@link DSSPrivateKey} constructed from the information
	 *         in the designated key-specification.
	 */
	private PrivateKey decodeDSSPrivateKey(DSAPrivateKeySpec spec) {
		BigInteger p = spec.getP();
		BigInteger q = spec.getQ();
		BigInteger g = spec.getG();
		BigInteger x = spec.getX();
		return new DSSPrivateKey(Registry.PKCS8_ENCODING_ID, p, q, g, x);
	}

	/**
	 * @param spec
	 *            an instance of {@link DSAPublicKeySpec} to decode.
	 * @return an instance of {@link DSSPublicKey} constructed from the information
	 *         in the designated key-specification.
	 */
	private DSSPublicKey decodeDSSPublicKey(DSAPublicKeySpec spec) {
		BigInteger p = spec.getP();
		BigInteger q = spec.getQ();
		BigInteger g = spec.getG();
		BigInteger y = spec.getY();
		return new DSSPublicKey(Registry.X509_ENCODING_ID, p, q, g, y);
	}

	/**
	 * @param spec
	 *            an instance of {@link RSAPrivateCrtKeySpec} to decode.
	 * @return an instance of {@link GnuRSAPrivateKey} constructed from the
	 *         information in the designated key-specification.
	 */
	private PrivateKey decodeRSAPrivateKey(RSAPrivateCrtKeySpec spec) {
		BigInteger n = spec.getModulus();
		BigInteger e = spec.getPublicExponent();
		BigInteger d = spec.getPrivateExponent();
		BigInteger p = spec.getPrimeP();
		BigInteger q = spec.getPrimeQ();
		BigInteger dP = spec.getPrimeExponentP();
		BigInteger dQ = spec.getPrimeExponentQ();
		BigInteger qInv = spec.getCrtCoefficient();
		return new GnuRSAPrivateKey(Registry.PKCS8_ENCODING_ID, n, e, d, p, q, dP, dQ, qInv);
	}

	/**
	 * @param spec
	 *            an instance of {@link RSAPublicKeySpec} to decode.
	 * @return an instance of {@link GnuRSAPublicKey} constructed from the
	 *         information in the designated key-specification.
	 */
	private GnuRSAPublicKey decodeRSAPublicKey(RSAPublicKeySpec spec) {
		BigInteger n = spec.getModulus();
		BigInteger e = spec.getPublicExponent();
		return new GnuRSAPublicKey(Registry.X509_ENCODING_ID, n, e);
	}

	@Override
	protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
		PrivateKey result = null;
		if (keySpec instanceof DSAPrivateKeySpec)
			result = decodeDSSPrivateKey((DSAPrivateKeySpec) keySpec);
		else if (keySpec instanceof RSAPrivateCrtKeySpec)
			result = decodeRSAPrivateKey((RSAPrivateCrtKeySpec) keySpec);
		else if (keySpec instanceof DHPrivateKeySpec)
			result = decodeDHPrivateKey((DHPrivateKeySpec) keySpec);
		else {
			if (!(keySpec instanceof PKCS8EncodedKeySpec))
				throw new InvalidKeySpecException("Unsupported key specification");

			byte[] input = ((PKCS8EncodedKeySpec) keySpec).getEncoded();
			boolean ok = false;
			// try DSS
			try {
				result = DSSPrivateKey.valueOf(input);
				ok = true;
			} catch (InvalidParameterException ignored) {
			}
			if (!ok) // try RSA
				try {
					result = GnuRSAPrivateKey.valueOf(input);
					ok = true;
				} catch (InvalidParameterException ignored) {
				}
			if (!ok) // try DH
				result = decodeDHPrivateKey(input);
		}
		return result;
	}

	@Override
	protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
		PublicKey result = null;
		if (keySpec instanceof DSAPublicKeySpec)
			result = decodeDSSPublicKey((DSAPublicKeySpec) keySpec);
		else if (keySpec instanceof RSAPublicKeySpec)
			result = decodeRSAPublicKey((RSAPublicKeySpec) keySpec);
		else if (keySpec instanceof DHPublicKeySpec)
			result = decodeDHPublicKey((DHPublicKeySpec) keySpec);
		else {
			if (!(keySpec instanceof X509EncodedKeySpec))
				throw new InvalidKeySpecException("Unsupported key specification");

			byte[] input = ((X509EncodedKeySpec) keySpec).getEncoded();
			boolean ok = false;
			// try DSS
			try {
				result = DSSPublicKey.valueOf(input);
				ok = true;
			} catch (InvalidParameterException ignored) {
			}
			if (!ok) // try RSA
				try {
					result = GnuRSAPublicKey.valueOf(input);
					ok = true;
				} catch (InvalidParameterException ignored) {
				}
			if (!ok) // try DH
				result = decodeDHPublicKey(input);
		}
		return result;
	}

	@Override
	protected KeySpec engineGetKeySpec(Key key, Class<? extends KeySpec> keySpec) throws InvalidKeySpecException {

		if (key instanceof PublicKey && Registry.X509_ENCODING_SORT_NAME.equalsIgnoreCase(key.getFormat())
				&& keySpec.isAssignableFrom(X509EncodedKeySpec.class))
			return new X509EncodedKeySpec(key.getEncoded());

		if (key instanceof PrivateKey && Registry.PKCS8_ENCODING_SHORT_NAME.equalsIgnoreCase(key.getFormat())
				&& keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class))
			return new PKCS8EncodedKeySpec(key.getEncoded());

		throw new InvalidKeySpecException("Unsupported format or invalid key spec class");
	}

	@Override
	protected Key engineTranslateKey(Key key) throws InvalidKeyException {
		throw new InvalidKeyException("Key translation not supported");
	}
}
