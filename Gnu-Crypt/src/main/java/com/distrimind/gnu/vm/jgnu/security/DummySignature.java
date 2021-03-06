/* DummySignature.java - Signature wrapper for SignatureSpi.
   Copyright (C) 1999, 2002 Free Software Foundation, Inc.

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

package com.distrimind.gnu.vm.jgnu.security;

final class DummySignature extends Signature {
	private SignatureSpi sigSpi = null;

	public DummySignature(SignatureSpi sigSpi, String algorithm) {
		super(algorithm);
		this.sigSpi = sigSpi;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Signature result = new DummySignature((SignatureSpi) sigSpi.clone(), this.getAlgorithm());
		result.provider = this.getProvider();
		return result;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected Object engineGetParameter(String param) throws InvalidParameterException {
		return sigSpi.engineGetParameter(param);
	}

	@Override
	protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
		sigSpi.engineInitSign(privateKey);
	}

	@Override
	protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
		sigSpi.engineInitVerify(publicKey);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
		sigSpi.engineSetParameter(param, value);
	}

	@Override
	protected byte[] engineSign() throws SignatureException {
		return sigSpi.engineSign();
	}

	@Override
	protected void engineUpdate(byte b) throws SignatureException {
		sigSpi.engineUpdate(b);
	}

	@Override
	protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
		sigSpi.engineUpdate(b, off, len);
	}

	@Override
	protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
		return sigSpi.engineVerify(sigBytes);
	}
}
