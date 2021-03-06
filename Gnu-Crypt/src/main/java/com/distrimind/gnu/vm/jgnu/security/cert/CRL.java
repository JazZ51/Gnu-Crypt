/* CRL.java --- Certificate Revocation List
   Copyright (C) 1999 Free Software Foundation, Inc.

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

package com.distrimind.gnu.vm.jgnu.security.cert;

/**
 * Certificate Revocation List class for managing CRLs that have different
 * formats but the same general use. They all serve as lists of revoked
 * certificates and can be queried for a given certificate.
 * 
 * Specialized CRLs extend this class.
 * 
 * @author Mark Benvenuto
 * 
 * @since JDK 1.2
 */
public abstract class CRL {

	private String type;

	/**
	 * Creates a new CRL for the specified type. An example is "X.509".
	 * 
	 * @param type
	 *            the standard name for the CRL type.
	 */
	protected CRL(String type) {
		this.type = type;
	}

	/**
	 * Returns the CRL type.
	 * 
	 * @return a string representing the CRL type
	 */
	public final String getType() {
		return type;
	}

	/**
	 * Determines whether or not the specified Certificate is revoked.
	 * 
	 * @param cert
	 *            A certificate to check if it is revoked
	 * 
	 * @return true if the certificate is revoked, false otherwise.
	 */
	public abstract boolean isRevoked(Certificate cert);

	/**
	 * Returns a string representing the CRL.
	 * 
	 * @return a string representing the CRL.
	 */
	@Override
	public abstract String toString();

}
