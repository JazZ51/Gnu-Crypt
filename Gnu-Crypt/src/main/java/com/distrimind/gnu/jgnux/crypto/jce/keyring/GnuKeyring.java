/* GnuKeyring.java -- KeyStore adapter for a pair of private and public Keyrings
   Copyright (C) 2003, 2006, 2010  Free Software Foundation, Inc.

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

package com.distrimind.gnu.jgnux.crypto.jce.keyring;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnux.crypto.keyring.Entry;
import com.distrimind.gnu.jgnux.crypto.keyring.GnuPrivateKeyring;
import com.distrimind.gnu.jgnux.crypto.keyring.GnuPublicKeyring;
import com.distrimind.gnu.jgnux.crypto.keyring.IKeyring;
import com.distrimind.gnu.jgnux.crypto.keyring.IPrivateKeyring;
import com.distrimind.gnu.jgnux.crypto.keyring.IPublicKeyring;
import com.distrimind.gnu.jgnux.crypto.keyring.MalformedKeyringException;
import com.distrimind.gnu.jgnux.crypto.keyring.PrimitiveEntry;
import com.distrimind.gnu.vm.jgnu.security.Key;
import com.distrimind.gnu.vm.jgnu.security.KeyStoreException;
import com.distrimind.gnu.vm.jgnu.security.KeyStoreSpi;
import com.distrimind.gnu.vm.jgnu.security.PrivateKey;
import com.distrimind.gnu.vm.jgnu.security.PublicKey;
import com.distrimind.gnu.vm.jgnu.security.UnrecoverableKeyException;
import com.distrimind.gnu.vm.jgnu.security.cert.Certificate;
import com.distrimind.gnu.vm.jgnux.crypto.SecretKey;

/**
 * An <i>Adapter</i> over a pair of one private, and one public keyrings to
 * emulate the keystore operations.
 */
public class GnuKeyring extends KeyStoreSpi {
	private static final String NOT_LOADED = "not loaded";

	/** TRUE if the keystore is loaded; FALSE otherwise. */
	private boolean loaded;

	/** our underlying private keyring. */
	private IPrivateKeyring privateKR;

	/** our underlying public keyring. */
	private IPublicKeyring publicKR;

	// default 0-arguments constructor

	/** Create empty keyrings. */
	private void createNewKeyrings() {
		privateKR = new GnuPrivateKeyring("HMAC-SHA-1", 20, "AES", "OFB", 16);
		publicKR = new GnuPublicKeyring("HMAC-SHA-1", 20);
	}

	@Override
	public Enumeration<String> engineAliases() {
		ensureLoaded();
		Enumeration<String> result;
		if (privateKR == null)
			result = Collections.enumeration(Collections.<String>emptySet());
		else {
			Set<String> aliases = new HashSet<>();
			for (Enumeration<Object> e = privateKR.aliases(); e.hasMoreElements();) {
				String alias = (String) e.nextElement();
				if (alias != null) {
					alias = alias.trim();
					if (alias.length() > 0) {
						aliases.add(alias);
					}
				}
			}
			for (Enumeration<Object> e = publicKR.aliases(); e.hasMoreElements();) {
				String alias = (String) e.nextElement();
				if (alias != null) {
					alias = alias.trim();
					if (alias.length() > 0) {
						aliases.add(alias);
					}
				}
			}
			result = Collections.enumeration(aliases);
		}
		return result;
	}

	@Override
	public boolean engineContainsAlias(String alias) {
		ensureLoaded();
		boolean inPrivateKR = privateKR.containsAlias(alias);
		boolean inPublicKR = publicKR.containsAlias(alias);
		boolean result = inPrivateKR || inPublicKR;
		return result;
	}

	@Override
	public void engineDeleteEntry(String alias) {
		ensureLoaded();
		if (privateKR.containsAlias(alias))
			privateKR.remove(alias);
		else if (publicKR.containsAlias(alias))
			publicKR.remove(alias);
	}

	@Override
	public Certificate engineGetCertificate(String alias) {
		ensureLoaded();
		Certificate result = publicKR.getCertificate(alias);
		return result;
	}

	@Override
	public String engineGetCertificateAlias(Certificate cert) {
		ensureLoaded();
		String result = null;
		for (Enumeration<Object> aliases = publicKR.aliases(); aliases.hasMoreElements();) {
			String alias = (String) aliases.nextElement();
			Certificate cert2 = publicKR.getCertificate(alias);
			if (cert.equals(cert2)) {
				result = alias;
				break;
			}
		}
		return result;
	}

	@Override
	public Certificate[] engineGetCertificateChain(String alias) {
		ensureLoaded();
		Certificate[] result = privateKR.getCertPath(alias);
		return result;
	}

	@Override
	public Date engineGetCreationDate(String alias) {
		ensureLoaded();
		Date result = getCreationDate(alias, privateKR);
		if (result == null)
			result = getCreationDate(alias, publicKR);

		return result;
	}

	@Override
	public Key engineGetKey(String alias, char[] password) throws UnrecoverableKeyException {
		ensureLoaded();
		Key result = null;
		if (password == null) {
			if (privateKR.containsPublicKey(alias))
				result = privateKR.getPublicKey(alias);
		} else if (privateKR.containsPrivateKey(alias))
			result = privateKR.getPrivateKey(alias, password);

		return result;
	}

	@Override
	public boolean engineIsCertificateEntry(String alias) {
		ensureLoaded();
		boolean result = publicKR.containsCertificate(alias);
		return result;
	}

	@Override
	public boolean engineIsKeyEntry(String alias) {
		ensureLoaded();
		boolean result = privateKR.containsPublicKey(alias) || privateKR.containsPrivateKey(alias);
		return result;
	}

	@Override
	public void engineLoad(InputStream in, char[] password) throws IOException {
		if (in != null) {
			if (!in.markSupported())
				in = new BufferedInputStream(in);

			loadPrivateKeyring(in, password);
			loadPublicKeyring(in, password);
		} else
			createNewKeyrings();

		loaded = true;
	}

	@Override
	public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
		ensureLoaded();
		if (privateKR.containsAlias(alias))
			throw new KeyStoreException(
					"Alias [" + alias + "] already exists and DOES NOT identify a " + "Trusted Certificate Entry");
		if (publicKR.containsCertificate(alias)) {
			publicKR.remove(alias);
		}
		publicKR.putCertificate(alias, cert);
	}

	@Override
	public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
		KeyStoreException x = new KeyStoreException("method not supported");
		throw x;
	}

	@Override
	public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
			throws KeyStoreException {
		ensureLoaded();
		if (publicKR.containsAlias(alias))
			throw new KeyStoreException("Alias [" + alias + "] already exists and DOES NOT identify a " + "Key Entry");
		if (key instanceof PublicKey) {
			privateKR.remove(alias);
			PublicKey pk = (PublicKey) key;
			privateKR.putPublicKey(alias, pk);
		} else {
			if (!(key instanceof PrivateKey) && !(key instanceof SecretKey))
				throw new KeyStoreException("cannot store keys of type " + key.getClass().getName());
			privateKR.remove(alias);
			privateKR.putCertPath(alias, chain);
			privateKR.putPrivateKey(alias, key, password);
		}
	}

	@Override
	public int engineSize() {
		int result = 0;
		for (Enumeration<String> e = engineAliases(); e.hasMoreElements(); result++)
			e.nextElement();

		return result;
	}

	@Override
	public void engineStore(OutputStream out, char[] password) throws IOException {
		ensureLoaded();
		HashMap<String, Object> attr = new HashMap<>();
		attr.put(IKeyring.KEYRING_DATA_OUT, out);
		attr.put(IKeyring.KEYRING_PASSWORD, password);

		privateKR.store(attr);
		publicKR.store(attr);
	}

	/**
	 * Ensure that the underlying keyring pair is loaded. Throw an exception if it
	 * isn't; otherwise returns silently.
	 *
	 * @throws IllegalStateException
	 *             if the keyring is not loaded.
	 */
	private void ensureLoaded() {
		if (!loaded)
			throw new IllegalStateException(NOT_LOADED);
	}

	/**
	 * Return the creation date of a named alias in a designated keyring.
	 *
	 * @param alias
	 *            the alias to look for.
	 * @param keyring
	 *            the keyring to search.
	 * @return the creattion date of the entry named <code>alias</code>. Return
	 *         <code>null</code> if <code>alias</code> was not found in
	 *         <code>keyring</code>.
	 */
	private Date getCreationDate(String alias, IKeyring keyring) {
		Date result = null;
		if (keyring != null)
			for (Iterator<Entry> it = keyring.get(alias).iterator(); it.hasNext();) {
				Entry o = it.next();
				if (o instanceof PrimitiveEntry) {
					result = ((PrimitiveEntry) o).getCreationDate();
					break;
				}
			}
		return result;
	}

	/**
	 * Load the private keyring from the designated input stream.
	 *
	 * @param in
	 *            the input stream to process.
	 * @param password
	 *            the password protecting the keyring.
	 * @throws MalformedKeyringException
	 *             if the keyring is not a private one.
	 * @throws IOException
	 *             if an I/O related exception occurs during the process.
	 */
	private void loadPrivateKeyring(InputStream in, char[] password) throws MalformedKeyringException, IOException {
		in.mark(5);
		for (int i = 0; i < 4; i++)
			if (in.read() != Registry.GKR_MAGIC[i])
				throw new MalformedKeyringException("incorrect magic");

		int usage = in.read();
		in.reset();
		if (usage != GnuPrivateKeyring.USAGE)
			throw new MalformedKeyringException(
					"Was expecting a private keyring but got a wrong USAGE: " + Integer.toBinaryString(usage));
		HashMap<String, Object> attr = new HashMap<>();
		attr.put(IKeyring.KEYRING_DATA_IN, in);
		attr.put(IKeyring.KEYRING_PASSWORD, password);
		privateKR = new GnuPrivateKeyring();
		privateKR.load(attr);
	}

	/**
	 * Load the public keyring from the designated input stream.
	 *
	 * @param in
	 *            the input stream to process.
	 * @param password
	 *            the password protecting the keyring.
	 * @throws MalformedKeyringException
	 *             if the keyring is not a public one.
	 * @throws IOException
	 *             if an I/O related exception occurs during the process.
	 */
	private void loadPublicKeyring(InputStream in, char[] password) throws MalformedKeyringException, IOException {
		in.mark(5);
		for (int i = 0; i < 4; i++)
			if (in.read() != Registry.GKR_MAGIC[i])
				throw new MalformedKeyringException("incorrect magic");

		int usage = in.read();
		in.reset();
		if (usage != GnuPublicKeyring.USAGE)
			throw new MalformedKeyringException(
					"Was expecting a public keyring but got a wrong USAGE: " + Integer.toBinaryString(usage));
		HashMap<String, Object> attr = new HashMap<>();
		attr.put(IKeyring.KEYRING_DATA_IN, in);
		attr.put(IKeyring.KEYRING_PASSWORD, password);
		publicKR = new GnuPublicKeyring();
		publicKR.load(attr);
	}
}
