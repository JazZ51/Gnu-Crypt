/* NullCipherImpl.java -- implementation of NullCipher.
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

package com.distrimind.gnu.vm.jgnux.crypto;

import com.distrimind.gnu.vm.jgnu.security.AlgorithmParameters;
import com.distrimind.gnu.vm.jgnu.security.Key;
import com.distrimind.gnu.vm.jgnu.security.SecureRandom;
import com.distrimind.gnu.vm.jgnu.security.spec.AlgorithmParameterSpec;

/**
 * Implementation of the identity cipher.
 */
final class NullCipherImpl extends CipherSpi {

	// Constructor.
	// -------------------------------------------------------------------------

	NullCipherImpl() {
		super();
	}

	// Instance methods.
	// -------------------------------------------------------------------------

	@Override
	protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) {
		return engineUpdate(input, inputOffset, inputLen);
	}

	@Override
	protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
			throws ShortBufferException {
		return engineUpdate(input, inputOffset, inputLen, output, outputOffset);
	}

	@Override
	protected int engineGetBlockSize() {
		return 1;
	}

	@Override
	protected byte[] engineGetIV() {
		return null;
	}

	@Override
	protected int engineGetOutputSize(int inputLen) {
		return inputLen;
	}

	@Override
	protected AlgorithmParameters engineGetParameters() {
		return null;
	}

	@Override
	protected void engineInit(int mode, Key key, AlgorithmParameters params, SecureRandom random) {
	}

	@Override
	protected void engineInit(int mode, Key key, AlgorithmParameterSpec spec, SecureRandom random) {
	}

	@Override
	protected void engineInit(int mode, Key key, SecureRandom random) {
	}

	@Override
	protected void engineSetMode(String mode) {
	}

	@Override
	protected void engineSetPadding(String padding) {
	}

	@Override
	protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
		if (input == null)
			return new byte[0];
		if (inputOffset < 0 || inputLen < 0 || inputOffset + inputLen > input.length)
			throw new ArrayIndexOutOfBoundsException();
		byte[] output = new byte[inputLen];
		System.arraycopy(input, inputOffset, output, 0, inputLen);
		return output;
	}

	@Override
	protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
			throws ShortBufferException {
		if (input == null)
			return 0;
		if (inputOffset < 0 || inputLen < 0 || inputOffset + inputLen > input.length || outputOffset < 0)
			throw new ArrayIndexOutOfBoundsException();
		if (output.length - outputOffset < inputLen)
			throw new ShortBufferException();
		System.arraycopy(input, inputOffset, output, outputOffset, inputLen);
		return inputLen;
	}
}
