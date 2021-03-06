/* DHKeyPairX509Codec.java -- X.509 DER encoder/decoder for DH keys
   Copyright (C) 2006 Free Software Foundation, Inc.

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

package com.distrimind.gnu.jgnux.crypto.key.dh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import com.distrimind.gnu.jgnu.security.OID;
import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnu.security.der.BitString;
import com.distrimind.gnu.jgnu.security.der.DER;
import com.distrimind.gnu.jgnu.security.der.DERReader;
import com.distrimind.gnu.jgnu.security.der.DERValue;
import com.distrimind.gnu.jgnu.security.der.DERWriter;
import com.distrimind.gnu.jgnu.security.key.IKeyPairCodec;
import com.distrimind.gnu.jgnu.security.util.DerUtil;
import com.distrimind.gnu.vm.jgnu.security.InvalidParameterException;
import com.distrimind.gnu.vm.jgnu.security.PrivateKey;
import com.distrimind.gnu.vm.jgnu.security.PublicKey;

public class DHKeyPairX509Codec implements IKeyPairCodec {
	private static final OID DH_ALG_OID = new OID(Registry.DH_OID_STRING);

	// implicit 0-arguments constructor

	/**
	 * @throws InvalidParameterException
	 *             ALWAYS.
	 */
	@Override
	public PrivateKey decodePrivateKey(byte[] input) {
		throw new InvalidParameterException("Wrong format for private keys");
	}

	/**
	 * @param input
	 *            the byte array to unmarshall into a valid DH {@link PublicKey}
	 *            instance. MUST NOT be null.
	 * @return a new instance of a {@link GnuDHPublicKey} decoded from the
	 *         <i>SubjectPublicKeyInfo</i> material in an X.509 certificate.
	 * @throw InvalidParameterException if an exception occurs during the
	 *        unmarshalling process.
	 */
	@Override
	public PublicKey decodePublicKey(byte[] input) {
		if (input == null)
			throw new InvalidParameterException("Input bytes MUST NOT be null");

		BigInteger p, g, q, y;
		DERReader der = new DERReader(input);
		try {
			DERValue derSPKI = der.read();
			DerUtil.checkIsConstructed(derSPKI, "Wrong SubjectPublicKeyInfo field");

			DERValue derAlgorithmID = der.read();
			DerUtil.checkIsConstructed(derAlgorithmID, "Wrong AlgorithmIdentifier field");

			DERValue derOID = der.read();
			if (!(derOID.getValue() instanceof OID))
				throw new InvalidParameterException("Wrong Algorithm field");

			OID algOID = (OID) derOID.getValue();
			if (!algOID.equals(DH_ALG_OID))
				throw new InvalidParameterException("Unexpected OID: " + algOID);

			DERValue derParams = der.read();
			DerUtil.checkIsConstructed(derParams, "Wrong DH Parameters field");

			DERValue val = der.read();
			DerUtil.checkIsBigInteger(val, "Wrong P field");
			p = (BigInteger) val.getValue();
			val = der.read();
			DerUtil.checkIsBigInteger(val, "Wrong G field");
			g = (BigInteger) val.getValue();
			val = der.read();
			DerUtil.checkIsBigInteger(val, "Wrong Q field");
			q = (BigInteger) val.getValue();
			if (q.compareTo(BigInteger.ZERO) == 0)
				q = null;

			val = der.read();
			if (!(val.getValue() instanceof BitString))
				throw new InvalidParameterException("Wrong SubjectPublicKey field");

			byte[] yBytes = ((BitString) val.getValue()).toByteArray();

			DERReader dhPub = new DERReader(yBytes);
			val = dhPub.read();
			DerUtil.checkIsBigInteger(val, "Wrong Y field");
			y = (BigInteger) val.getValue();
		} catch (IOException x) {
			InvalidParameterException e = new InvalidParameterException();
			e.initCause(x);
			throw e;
		}

		return new GnuDHPublicKey(Registry.X509_ENCODING_ID, q, p, g, y);
	}

	/**
	 * @throws InvalidParameterException
	 *             ALWAYS.
	 */
	@Override
	public byte[] encodePrivateKey(PrivateKey key) {
		throw new InvalidParameterException("Wrong format for private keys");
	}

	/**
	 * Returns the DER-encoded form of the X.509 ASN.1 <i>SubjectPublicKeyInfo</i>
	 * representation of a DH public key. The ASN.1 specification, as defined in
	 * RFC-3280, and RFC-2459, is as follows:
	 *
	 * <pre>
	 *   SubjectPublicKeyInfo ::= SEQUENCE {
	 *     algorithm         AlgorithmIdentifier,
	 *     subjectPublicKey  BIT STRING
	 *   }
	 *
	 *   AlgorithmIdentifier ::= SEQUENCE {
	 *     algorithm   OBJECT IDENTIFIER,
	 *     parameters  ANY DEFINED BY algorithm OPTIONAL
	 *   }
	 *
	 *   DhParams ::= SEQUENCE {
	 *     p  INTEGER, -- odd prime, p=jq +1
	 *     g  INTEGER, -- generator, g
	 *     q  INTEGER  -- factor of p-1
	 *   }
	 * </pre>
	 *
	 * <p>
	 * The <i>subjectPublicKey</i> field, which is a BIT STRING, contains the
	 * DER-encoded form of the DH public key as an INTEGER.
	 * </p>
	 *
	 * <pre>
	 *       DHPublicKey ::= INTEGER -- public key, y = g^x mod p
	 * </pre>
	 * <p>
	 * <b>IMPORTANT</b>: with RI's
	 * {@link com.distrimind.gnu.vm.jgnux.crypto.spec.DHGenParameterSpec} and
	 * {@link com.distrimind.gnu.vm.jgnux.crypto.spec.DHParameterSpec} classes, we may end up with
	 * Diffie-Hellman keys that have a <code>null</code> for the <code>q</code>
	 * parameter. RFC-2631 DOES NOT allow for an <i>optional</i> value for that
	 * parameter, hence we replace such null values with <code>0</code>, and do the
	 * reverse in the corresponding decode method.
	 *
	 * @param key
	 *            the {@link PublicKey} instance to encode. MUST be an instance of
	 *            {@link GnuDHPublicKey}.
	 * @return the DER-encoded form of the ASN.1 representation of the
	 *         <i>SubjectPublicKeyInfo</i> in an X.509 certificate.
	 * @throw InvalidParameterException if <code>key</code> is not an instance of
	 *        {@link GnuDHPublicKey} or if an exception occurs during the
	 *        marshalling process.
	 */
	@Override
	public byte[] encodePublicKey(PublicKey key) {
		if (!(key instanceof GnuDHPublicKey))
			throw new InvalidParameterException("Wrong key type");

		DERValue derOID = new DERValue(DER.OBJECT_IDENTIFIER, DH_ALG_OID);

		GnuDHPublicKey dhKey = (GnuDHPublicKey) key;
		BigInteger p = dhKey.getParams().getP();
		BigInteger g = dhKey.getParams().getG();
		BigInteger q = dhKey.getQ();
		if (q == null)
			q = BigInteger.ZERO;
		BigInteger y = dhKey.getY();

		DERValue derP = new DERValue(DER.INTEGER, p);
		DERValue derG = new DERValue(DER.INTEGER, g);
		DERValue derQ = new DERValue(DER.INTEGER, q);

		ArrayList<DERValue> params = new ArrayList<>(3);
		params.add(derP);
		params.add(derG);
		params.add(derQ);
		DERValue derParams = new DERValue(DER.CONSTRUCTED | DER.SEQUENCE, params);

		ArrayList<DERValue> algorithmID = new ArrayList<>(2);
		algorithmID.add(derOID);
		algorithmID.add(derParams);
		DERValue derAlgorithmID = new DERValue(DER.CONSTRUCTED | DER.SEQUENCE, algorithmID);

		DERValue derDHPublicKey = new DERValue(DER.INTEGER, y);
		byte[] yBytes = derDHPublicKey.getEncoded();
		DERValue derSPK = new DERValue(DER.BIT_STRING, new BitString(yBytes));

		ArrayList<DERValue> spki = new ArrayList<>(2);
		spki.add(derAlgorithmID);
		spki.add(derSPK);
		DERValue derSPKI = new DERValue(DER.CONSTRUCTED | DER.SEQUENCE, spki);

		byte[] result;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			DERWriter.write(baos, derSPKI);
			result = baos.toByteArray();
		} catch (IOException x) {
			InvalidParameterException e = new InvalidParameterException();
			e.initCause(x);
			throw e;
		}

		return result;
	}

	@Override
	public int getFormatID() {
		return X509_FORMAT;
	}
}
