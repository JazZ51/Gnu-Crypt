/* CRLNumber.java -- CRL number extension.
   Copyright (C) 2004  Free Software Foundation, Inc.

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
import java.math.BigInteger;

import com.distrimind.gnu.jgnu.security.OID;
import com.distrimind.gnu.jgnu.security.der.DER;
import com.distrimind.gnu.jgnu.security.der.DERReader;
import com.distrimind.gnu.jgnu.security.der.DERValue;

public class CRLNumber extends Extension.Value {

	// Constants and fields.
	// -------------------------------------------------------------------------

	public static final OID ID = new OID("2.5.29.20");

	private final BigInteger number;

	// Constructor.
	// -------------------------------------------------------------------------

	public CRLNumber(final BigInteger number) {
		this.number = number;
	}

	public CRLNumber(final byte[] encoded) throws IOException {
		super(encoded);
		DERValue val = DERReader.read(encoded);
		if (val.getTag() != DER.INTEGER)
			throw new IOException("malformed CRLNumber");
		number = (BigInteger) val.getValue();
	}

	// Instance method.
	// -------------------------------------------------------------------------

	@Override
	public byte[] getEncoded() {
		if (encoded == null) {
			encoded = new DERValue(DER.INTEGER, number).getEncoded();
		}
		return encoded.clone();
	}

	public BigInteger getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return CRLNumber.class.getName() + " [ " + number + " ]";
	}
}
