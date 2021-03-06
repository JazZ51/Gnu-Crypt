/* Extension.java -- an X.509 certificate or CRL extension.
   Copyright (C) 2004, 2006, 2010  Free Software Foundation, Inc.

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

package com.distrimind.gnu.jgnu.security.x509.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.distrimind.gnu.jgnu.security.OID;
import com.distrimind.gnu.jgnu.security.der.DER;
import com.distrimind.gnu.jgnu.security.der.DERReader;
import com.distrimind.gnu.jgnu.security.der.DERValue;
import com.distrimind.gnu.jgnu.security.x509.Util;

public class Extension {
	public static class Value {

		// Fields.
		// -----------------------------------------------------------------------

		protected byte[] encoded;

		// Constructor.
		// -----------------------------------------------------------------------

		protected Value() {
		}

		public Value(byte[] encoded) {
			this.encoded = encoded.clone();
		}

		// Instance methods.
		// -----------------------------------------------------------------------

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Value))
				return false;
			return Arrays.equals(encoded, ((Value) o).encoded);
		}

		public byte[] getEncoded() {
			return encoded;
		}

		@Override
		public int hashCode() {
			int result = 0;
			for (int i = 0; i < encoded.length; ++i)
				result = result * 31 + encoded[i];
			return result;
		}

		@Override
		public String toString() {
			return Util.toHexString(encoded, ':');
		}
	}

	/**
	 * This extension's object identifier.
	 */
	protected final OID oid;

	/**
	 * The criticality flag.
	 */
	protected final boolean critical;

	/**
	 * Whether or not this extension is locally supported.
	 */
	protected boolean isSupported;

	/**
	 * The extension value.
	 */
	protected final Value value;

	// Constructors.
	// -------------------------------------------------------------------------

	/**
	 * The DER encoded form.
	 */
	protected byte[] encoded;

	public Extension(byte[] encoded) throws IOException {
		this.encoded = encoded.clone();
		DERReader der = new DERReader(encoded);

		// Extension ::= SEQUENCE {
		DERValue val = der.read();
		if (!val.isConstructed())
			throw new IOException("malformed Extension");

		// extnID OBJECT IDENTIFIER,
		val = der.read();
		if (val.getTag() != DER.OBJECT_IDENTIFIER)
			throw new IOException("expecting OBJECT IDENTIFIER");
		oid = (OID) val.getValue();

		// critical BOOLEAN DEFAULT FALSE,
		val = der.read();
		if (val.getTag() == DER.BOOLEAN) {
			critical = ((Boolean) val.getValue()).booleanValue();
			val = der.read();
		} else
			critical = false;

		// extnValue OCTET STRING }
		if (val.getTag() != DER.OCTET_STRING)
			throw new IOException("expecting OCTET STRING");
		byte[] encval = (byte[]) val.getValue();
		isSupported = true;
		if (oid.equals(AuthorityKeyIdentifier.ID)) {
			value = new AuthorityKeyIdentifier(encval);
		} else if (oid.equals(SubjectKeyIdentifier.ID)) {
			value = new SubjectKeyIdentifier(encval);
		} else if (oid.equals(KeyUsage.ID)) {
			value = new KeyUsage(encval);
		} else if (oid.equals(PrivateKeyUsagePeriod.ID)) {
			value = new PrivateKeyUsagePeriod(encval);
		} else if (oid.equals(CertificatePolicies.ID)) {
			value = new CertificatePolicies(encval);
		} else if (oid.equals(PolicyConstraint.ID)) {
			value = new PolicyConstraint(encval);
		} else if (oid.equals(PolicyMappings.ID)) {
			value = new PolicyMappings(encval);
		} else if (oid.equals(SubjectAlternativeNames.ID)) {
			value = new SubjectAlternativeNames(encval);
		} else if (oid.equals(IssuerAlternativeNames.ID)) {
			value = new IssuerAlternativeNames(encval);
		} else if (oid.equals(BasicConstraints.ID)) {
			value = new BasicConstraints(encval);
		} else if (oid.equals(ExtendedKeyUsage.ID)) {
			value = new ExtendedKeyUsage(encval);
		} else if (oid.equals(CRLNumber.ID)) {
			value = new CRLNumber(encval);
		} else if (oid.equals(ReasonCode.ID)) {
			value = new ReasonCode(encval);
		} else if (oid.equals(NameConstraints.ID)) {
			value = new NameConstraints(encval);
		} else {
			value = new Value(encval);
			isSupported = false;
		}
	}

	// Instance methods.
	// -------------------------------------------------------------------------

	public Extension(final OID oid, final Value value, final boolean critical) {
		this.oid = oid;
		this.value = value;
		this.critical = critical;
		isSupported = true;
	}

	private void encode() {
		encoded = getDerValue().getEncoded();
	}

	public DERValue getDerValue() {
		List<DERValue> ext = new ArrayList<DERValue>(3);
		ext.add(new DERValue(DER.OBJECT_IDENTIFIER, oid));
		ext.add(new DERValue(DER.BOOLEAN, Boolean.valueOf(critical)));
		ext.add(new DERValue(DER.OCTET_STRING, value.getEncoded()));
		return new DERValue(DER.CONSTRUCTED | DER.SEQUENCE, ext);
	}

	public byte[] getEncoded() {
		if (encoded == null)
			encode();
		return encoded.clone();
	}

	public OID getOid() {
		return oid;
	}

	public Value getValue() {
		return value;
	}

	public boolean isCritical() {
		return critical;
	}

	// Own methods.
	// -------------------------------------------------------------------------

	public boolean isSupported() {
		return isSupported;
	}

	// Inner class.
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		return Extension.class.getName() + " [ id=" + oid + " critical=" + critical + " value=" + value + " ]";
	}
}
