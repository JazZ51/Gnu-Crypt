/* KeyPairGeneratorAdapter.java --
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

package com.distrimind.gnu.jgnu.security.jce.sig;

import com.distrimind.gnu.jgnu.security.key.IKeyPairGenerator;
import com.distrimind.gnu.jgnu.security.key.KeyPairGeneratorFactory;
import com.distrimind.gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import com.distrimind.gnu.vm.jgnu.security.KeyPair;
import com.distrimind.gnu.vm.jgnu.security.KeyPairGenerator;
import com.distrimind.gnu.vm.jgnu.security.SecureRandom;
import com.distrimind.gnu.vm.jgnu.security.spec.AlgorithmParameterSpec;

/**
 * The implementation of a generic {@link com.distrimind.gnu.vm.jgnu.security.KeyPairGenerator}
 * adapter class to wrap GNU keypair generator instances.
 * <p>
 * This class defines the <i>Service Provider Interface</i> (<b>SPI</b>) for the
 * {@link com.distrimind.gnu.vm.jgnu.security.KeyPairGenerator} class, which is used to
 * generate pairs of public and private keys.
 * <p>
 * All the abstract methods in the
 * {@link com.distrimind.gnu.vm.jgnu.security.KeyPairGeneratorSpi} class are implemented by
 * this class and all its sub-classes.
 * <p>
 * In case the client does not explicitly initialize the KeyPairGenerator (via a
 * call to an <code>initialize()</code> method), the GNU provider supplies (and
 * document) default values to be used. For example, the GNU provider uses a
 * default <i>modulus</i> size (keysize) of 1024 bits for the DSS (Digital
 * Signature Standard) a.k.a <i>DSA</i>.
 */
public abstract class KeyPairGeneratorAdapter extends KeyPairGenerator {
	/** Our underlying keypair instance. */
	protected IKeyPairGenerator adaptee;

	/**
	 * Trivial protected constructor.
	 *
	 * @param kpgName
	 *            the canonical name of the keypair generator algorithm.
	 */
	protected KeyPairGeneratorAdapter(String kpgName) {
		super(kpgName);

		this.adaptee = KeyPairGeneratorFactory.getInstance(kpgName);
	}

	@Override
	public KeyPair generateKeyPair() {
		return adaptee.generate();
	}

	@Override
	public abstract void initialize(AlgorithmParameterSpec params, SecureRandom random)
			throws InvalidAlgorithmParameterException;

	@Override
	public abstract void initialize(int keysize, SecureRandom random);
}
