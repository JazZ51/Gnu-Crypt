/* CSPRNG.java -- continuously-seeded pseudo-random number generator.
   Copyright (C) 2004, 2006, 2010  Free Software Foundation, Inc.

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

package com.distrimind.gnu.jgnux.crypto.prng;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.distrimind.gnu.jgnu.security.Properties;
import com.distrimind.gnu.jgnu.security.Registry;
import com.distrimind.gnu.jgnu.security.hash.HashFactory;
import com.distrimind.gnu.jgnu.security.hash.IMessageDigest;
import com.distrimind.gnu.jgnu.security.prng.BasePRNG;
import com.distrimind.gnu.jgnu.security.prng.EntropySource;
import com.distrimind.gnu.jgnu.security.prng.IRandom;
import com.distrimind.gnu.jgnu.security.prng.LimitReachedException;
import com.distrimind.gnu.jgnu.security.util.SimpleList;
import com.distrimind.gnu.jgnux.crypto.cipher.CipherFactory;
import com.distrimind.gnu.jgnux.crypto.cipher.IBlockCipher;
import com.distrimind.gnu.vm.jgnu.security.InvalidKeyException;

/**
 * An entropy pool-based pseudo-random number generator based on the PRNG in
 * Peter Gutmann's cryptlib (<a href=
 * "http://www.cs.auckland.ac.nz/~pgut001/cryptlib/">http://www.cs.auckland.ac.nz/~pgut001/cryptlib/</a>).
 * <p>
 * The basic properties of this generator are:
 * <ol>
 * <li>The internal state cannot be determined by knowledge of the input.</li>
 * <li>It is resistant to bias introduced by specific inputs.</li>
 * <li>The output does not reveal the state of the generator.</li>
 * </ol>
 */
public class CSPRNG extends BasePRNG {

	private final class Poller implements Runnable {
		private final List<Object> files;

		private final List<Object> urls;

		private final List<Object> progs;

		private final List<Object> other;

		private final CSPRNG pool;

		private boolean running;

		Poller(List<Object> files, List<Object> urls, List<Object> progs, List<Object> other, CSPRNG pool) {
			super();
			this.files = Collections.unmodifiableList(files);
			this.urls = Collections.unmodifiableList(urls);
			this.progs = Collections.unmodifiableList(progs);
			this.other = Collections.unmodifiableList(other);
			this.pool = pool;
		}

		@Override
		public void run() {
			running = true;
			Iterator<Object> files_it = files.iterator();
			Iterator<Object> urls_it = urls.iterator();
			Iterator<Object> prog_it = progs.iterator();
			Iterator<Object> other_it = other.iterator();

			while (files_it.hasNext() || urls_it.hasNext() || prog_it.hasNext() || other_it.hasNext()) {
				// There is enough random data. Go away.
				if (pool.getQuality() >= 100.0 || !running)
					return;
				if (files_it.hasNext())
					try {
						List<?> l = (List<?>) files_it.next();
						double qual = ((Double) l.get(0)).doubleValue();
						int offset = ((Integer) l.get(1)).intValue();
						int count = ((Integer) l.get(2)).intValue();
						String src = (String) l.get(3);
						try (InputStream in = new FileInputStream(src)) {
							byte[] buf = new byte[count];
							if (offset > 0)
								in.skip(offset);
							int len = in.read(buf);
							if (len >= 0) {
								pool.addRandomBytes(buf, 0, len);
								pool.addQuality(qual * ((double) len / (double) count));
							}
						}
					} catch (Exception x) {
					}
				if (pool.getQuality() >= 100.0 || !running)
					return;
				if (urls_it.hasNext())
					try {
						List<?> l = (List<?>) urls_it.next();
						double qual = ((Double) l.get(0)).doubleValue();
						int offset = ((Integer) l.get(1)).intValue();
						int count = ((Integer) l.get(2)).intValue();
						URL src = (URL) l.get(3);
						InputStream in = src.openStream();
						byte[] buf = new byte[count];
						if (offset > 0)
							in.skip(offset);
						int len = in.read(buf);
						if (len >= 0) {
							pool.addRandomBytes(buf, 0, len);
							pool.addQuality(qual * ((double) len / (double) count));
						}
					} catch (Exception x) {
					}
				if (pool.getQuality() >= 100.0 || !running)
					return;
				Process proc = null;
				if (prog_it.hasNext())
					try {
						List<?> l = (List<?>) prog_it.next();
						double qual = ((Double) l.get(0)).doubleValue();
						int offset = ((Integer) l.get(1)).intValue();
						int count = ((Integer) l.get(2)).intValue();
						String src = (String) l.get(3);
						proc = null;
						proc = Runtime.getRuntime().exec(src);
						InputStream in = proc.getInputStream();
						byte[] buf = new byte[count];
						if (offset > 0)
							in.skip(offset);
						int len = in.read(buf);
						if (len >= 0) {
							pool.addRandomBytes(buf, 0, len);
							pool.addQuality(qual * ((double) len / (double) count));
						}
						proc.destroy();
						proc.waitFor();
					} catch (Exception x) {
						try {
							if (proc != null) {
								proc.destroy();
								proc.waitFor();
							}
						} catch (Exception ignored) {
						}
					}
				if (pool.getQuality() >= 100.0 || !running)
					return;
				if (other_it.hasNext())
					try {
						EntropySource src = (EntropySource) other_it.next();
						byte[] buf = src.nextBytes();
						if (pool == null)
							return;
						pool.addRandomBytes(buf, 0, buf.length);
						pool.addQuality(src.quality());
					} catch (Exception x) {
					}
			}
		}

		public void stopUpdating() {
			running = false;
		}
	}

	/**
	 * A simple thread that constantly updates a byte counter. This class is used in
	 * a group of lowest-priority threads and the values of their counters (updated
	 * in competition with all other threads) is used as a source of entropy bits.
	 */
	private static class Spinner implements Runnable {
		protected byte counter;

		Spinner() {
		}

		@Override
		public void run() {
			while (true) {
				counter++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
				}
			}
		}
	}

	/**
	 * Property name for the list of files to read for random values. The mapped
	 * value is a list with the following values:
	 * <ol>
	 * <li>A {@link Double}, indicating the suggested <i>quality</i> of this source.
	 * This value must be between 0 and 100.</li>
	 * <li>An {@link Integer}, indicating the number of bytes to skip in the file
	 * before reading bytes. This can be any nonnegative value.</li>
	 * <li>An {@link Integer}, indicating the number of bytes to read.</li>
	 * <li>A {@link String}, indicating the path to the file.</li>
	 * </ol>
	 *
	 * @see com.distrimind.gnu.jgnu.security.util.SimpleList
	 */
	public static final String FILE_SOURCES = "com.distrimind.gnu.crypto.prng.pool.files";

	/**
	 * Property name for the list of URLs to poll for random values. The mapped
	 * value is a list formatted similarly as in {@link #FILE_SOURCES}, but the
	 * fourth member is a {@link URL}.
	 */
	public static final String URL_SOURCES = "com.distrimind.gnu.crypto.prng.pool.urls";

	/**
	 * Property name for the list of programs to execute, and use the output as new
	 * random bytes. The mapped property is formatted similarly an in
	 * {@link #FILE_SOURCES} and {@link #URL_SOURCES}, except the fourth member is a
	 * {@link String} of the program to execute.
	 */
	public static final String PROGRAM_SOURCES = "com.distrimind.gnu.crypto.prng.pool.programs";

	/**
	 * Property name for a list of other sources of entropy. The mapped value must
	 * be a list of {@link EntropySource} objects.
	 */
	public static final String OTHER_SOURCES = "com.distrimind.gnu.crypto.prng.pool.other";

	/**
	 * Property name for whether or not to wait for the slow poll to complete,
	 * passed as a {@link Boolean}. The default value is true.
	 */
	public static final String BLOCKING = "com.distrimind.gnu.crypto.prng.pool.blocking";

	private static final String FILES = "com.distrimind.gnu.crypto.csprng.file.";

	private static final String URLS = "com.distrimind.gnu.crypto.csprng.url.";

	private static final String PROGS = "com.distrimind.gnu.crypto.csprng.program.";

	private static final String OTHER = "com.distrimind.gnu.crypto.csprng.other.";

	private static final String BLOCK = "com.distrimind.gnu.crypto.csprng.blocking";

	private static final int POOL_SIZE = 256;

	private static final int ALLOC_SIZE = 260;

	private static final int OUTPUT_SIZE = POOL_SIZE / 2;

	private static final int X917_POOL_SIZE = 16;

	private static final String HASH_FUNCTION = Registry.SHA160_HASH;

	private static final String CIPHER = Registry.AES_CIPHER;

	private static final int MIX_COUNT = 10;

	private static final int X917_LIFETIME = 8192;

	// FIXME this should be configurable.
	private static final int SPINNER_COUNT = 8;
	/**
	 * The spinner group singleton. We use this to add a small amount of randomness
	 * (in addition to the current time and the amount of free memory) based on the
	 * randomness (if any) present due to system load and thread scheduling.
	 */
	private static final Spinner[] SPINNERS = new Spinner[SPINNER_COUNT];

	private static final Thread[] SPINNER_THREADS = new Thread[SPINNER_COUNT];

	static {
		for (int i = 0; i < SPINNER_COUNT; i++) {
			SPINNER_THREADS[i] = new Thread(SPINNERS[i] = new Spinner(), "spinner-" + i);
			SPINNER_THREADS[i].setDaemon(true);
			SPINNER_THREADS[i].setPriority(Thread.MIN_PRIORITY);
			SPINNER_THREADS[i].start();
		}
	}

	private static String getProperty(final String name) {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				return Properties.getProperty(name);
			}
		});
	}

	/**
	 * Create and initialize a CSPRNG instance with the "system" parameters; the
	 * files, URLs, programs, and {@link EntropySource} sources used by the instance
	 * are derived from properties set in the system {@link Properties}.
	 * <p>
	 * All properties are of the from <i>name</i>.</i>N</i>, where <i>name</i> is
	 * the name of the source, and <i>N</i> is an integer (staring at 1) that
	 * indicates the preference number for that source.
	 * <p>
	 * The following vales for <i>name</i> are used here:
	 * <dl>
	 * <dt>com.distrimind.gnu.crypto.csprng.file</dt>
	 * <dd>
	 * <p>
	 * These properties are file sources, passed as the {@link #FILE_SOURCES}
	 * parameter of the instance. The property value is a 4-tuple formatted as:
	 * </p>
	 * <blockquote><i>quality</i> ; <i>offset</i> ; <i>count</i> ;
	 * <i>path</i></blockquote>
	 * <p>
	 * The parameters are mapped to the parameters defined for
	 * {@link #FILE_SOURCES}. Leading or trailing spaces on any item are trimmed
	 * off.
	 * </p>
	 * </dd>
	 * <dt>com.distrimind.gnu.crypto.csprng.url</dt>
	 * <dd>
	 * <p>
	 * These properties are URL sources, passed as the {@link #URL_SOURCES}
	 * parameter of the instance. The property is formatted the same way as file
	 * sources, but the <i>path</i> argument must be a valid URL.
	 * </p>
	 * </dd>
	 * <dt>com.distrimind.gnu.crypto.csprng.program</dt>
	 * <dd>
	 * <p>
	 * These properties are program sources, passed as the {@link #PROGRAM_SOURCES}
	 * parameter of the instance. This property is formatted the same way as file
	 * and URL sources, but the last argument is a program and its arguments.
	 * </p>
	 * </dd>
	 * <dt>com.distrimind.gnu.crypto.cspring.other</dt>
	 * <dd>
	 * <p>
	 * These properties are other sources, passed as the {@link #OTHER_SOURCES}
	 * parameter of the instance. The property value must be the full name of a
	 * class that implements the {@link EntropySource} interface and has a public
	 * no-argument constructor.
	 * </p>
	 * </dd>
	 * </dl>
	 * <p>
	 * Finally, a boolean property "com.distrimind.gnu.crypto.csprng.blocking" can be set to the
	 * desired value of {@link #BLOCKING}.
	 * <p>
	 * An example of valid properties would be:
	 * 
	 * <pre>
	 *  com.distrimind.gnu.crypto.csprng.blocking=true
	 *
	 *  com.distrimind.gnu.crypto.csprng.file.1=75.0;0;256;/dev/random
	 *  com.distrimind.gnu.crypto.csprng.file.2=10.0;0;100;/home/user/file
	 *
	 *  com.distrimind.gnu.crypto.csprng.url.1=5.0;0;256;http://www.random.org/cgi-bin/randbyte?nbytes=256
	 *  com.distrimind.gnu.crypto.csprng.url.2=0;256;256;http://slashdot.org/
	 *
	 *  com.distrimind.gnu.crypto.csprng.program.1=0.5;0;10;last -n 50
	 *  com.distrimind.gnu.crypto.csprng.program.2=0.5;0;10;tcpdump -c 5
	 *
	 *  com.distrimind.gnu.crypto.csprng.other.1=foo.bar.MyEntropySource
	 *  com.distrimind.gnu.crypto.csprng.other.2=com.company.OtherEntropySource
	 * </pre>
	 */
	public static IRandom getSystemInstance() throws NumberFormatException {
		CSPRNG instance = new CSPRNG();
		HashMap<Object, Object> attrib = new HashMap<>();
		attrib.put(BLOCKING, Boolean.valueOf(getProperty(BLOCK)));
		String s = null;
		// Get each file source "com.distrimind.gnu.crypto.csprng.file.N".
		List<Object> l = new LinkedList<>();
		for (int i = 0; (s = getProperty(FILES + i)) != null; i++)
			try {
				l.add(parseString(s.trim()));
			} catch (NumberFormatException nfe) {
			}
		attrib.put(FILE_SOURCES, l);
		l = new LinkedList<>();
		for (int i = 0; (s = getProperty(URLS + i)) != null; i++)
			try {
				l.add(parseURL(s.trim()));
			} catch (NumberFormatException nfe) {
			} catch (MalformedURLException mue) {
			}
		attrib.put(URL_SOURCES, l);
		l = new LinkedList<>();
		for (int i = 0; (s = getProperty(PROGS + i)) != null; i++)
			try {
				l.add(parseString(s.trim()));
			} catch (NumberFormatException nfe) {
			}
		attrib.put(PROGRAM_SOURCES, l);
		l = new LinkedList<>();
		for (int i = 0; (s = getProperty(OTHER + i)) != null; i++) {
			try {
				l.add(Class.forName(s.trim()).newInstance());
			} catch (ClassNotFoundException cnfe) {
				// ignore
			} catch (InstantiationException ie) {
				// ignore
			} catch (IllegalAccessException iae) {
				// ignore
			}
		}
		attrib.put(OTHER_SOURCES, l);
		instance.init(attrib);
		return instance;
	}

	private static List<Object> parseString(String s) throws NumberFormatException {
		StringTokenizer tok = new StringTokenizer(s, ";");
		if (tok.countTokens() != 4)
			throw new IllegalArgumentException("malformed property");
		Double quality = new Double(tok.nextToken());
		Integer offset = new Integer(tok.nextToken());
		Integer length = new Integer(tok.nextToken());
		String str = tok.nextToken();
		return new SimpleList(quality, offset, length, str);
	}

	private static List<Object> parseURL(String s) throws MalformedURLException, NumberFormatException {
		StringTokenizer tok = new StringTokenizer(s, ";");
		if (tok.countTokens() != 4)
			throw new IllegalArgumentException("malformed property");
		Double quality = new Double(tok.nextToken());
		Integer offset = new Integer(tok.nextToken());
		Integer length = new Integer(tok.nextToken());
		URL url = new URL(tok.nextToken());
		return new SimpleList(quality, offset, length, url);
	}

	/** The message digest (SHA-1) used in the mixing function. */
	private final IMessageDigest hash;

	/** The cipher (AES) used in the output masking function. */
	private final IBlockCipher cipher;

	/** The number of times the pool has been mixed. */
	private int mixCount;

	/** The entropy pool. */
	private final byte[] pool;

	/** The quality of the random pool (percentage). */
	private double quality;

	/** The index of the next byte in the entropy pool. */
	private int index;

	/** The pool for the X9.17-like generator. */
	private byte[] x917pool;

	/** The number of iterations of the X9.17-like generators. */
	private int x917count;

	/** Whether or not the X9.17-like generator is initialized. */
	private boolean x917init;

	/** The list of file soures. */
	private final List<Object> files;

	/** The list of URL sources. */
	private final List<Object> urls;

	/** The list of program sources. */
	private final List<Object> progs;

	/** The list of other sources. */
	private final List<Object> other;

	/** Whether or not to wait for the slow poll to complete. */
	private boolean blocking;

	/** The thread that polls for random data. */
	private Poller poller;

	private Thread pollerThread;

	public CSPRNG() {
		super("CSPRNG");
		pool = new byte[ALLOC_SIZE];
		x917pool = new byte[X917_POOL_SIZE];
		x917count = 0;
		x917init = false;
		quality = 0.0;
		hash = HashFactory.getInstance(HASH_FUNCTION);
		cipher = CipherFactory.getInstance(CIPHER);
		buffer = new byte[OUTPUT_SIZE];
		ndx = 0;
		initialised = false;
		files = new LinkedList<>();
		urls = new LinkedList<>();
		progs = new LinkedList<>();
		other = new LinkedList<>();
	}

	synchronized void addQuality(double quality) {
		if (this.quality < 100)
			this.quality += quality;
	}

	/**
	 * Add a single random byte to the randomness pool. Note that this method will
	 * <i>not</i> increment the pool's quality counter (this can only be done via a
	 * source provided to the setup method).
	 *
	 * @param b
	 *            The byte to add.
	 */
	@Override
	public synchronized void addRandomByte(byte b) {
		pool[index++] ^= b;
		if (index >= pool.length) {
			mixRandomPool();
			index = 0;
		}
	}

	/**
	 * Add an array of bytes into the randomness pool. Note that this method will
	 * <i>not</i> increment the pool's quality counter (this can only be done via a
	 * source provided to the setup method).
	 *
	 * @param buf
	 *            The byte array.
	 * @param off
	 *            The offset from whence to start reading bytes.
	 * @param len
	 *            The number of bytes to add.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If <i>off</i> or <i>len</i> are out of the range of <i>buf</i>.
	 */
	@Override
	public synchronized void addRandomBytes(byte[] buf, int off, int len) {
		if (off < 0 || len < 0 || off + len > buf.length)
			throw new ArrayIndexOutOfBoundsException();
		final int count = off + len;
		for (int i = off; i < count; i++) {
			pool[index++] ^= buf[i];
			if (index == pool.length) {
				mixRandomPool();
				index = 0;
			}
		}
	}

	@Override
	public Object clone() {
		return new CSPRNG();
	}

	/**
	 * Add random data always immediately available into the random pool, such as
	 * the values of the eight asynchronous counters, the current time, the current
	 * memory usage, the calling thread name, and the current stack trace.
	 * <p>
	 * This method does not alter the quality counter, and is provided more to
	 * maintain randomness, not to seriously improve the current random state.
	 */
	private void fastPoll() {
		byte b = 0;
		for (int i = 0; i < SPINNER_COUNT; i++)
			b ^= SPINNERS[i].counter;
		addRandomByte(b);
		addRandomByte((byte) System.currentTimeMillis());
		addRandomByte((byte) Runtime.getRuntime().freeMemory());
		String s = Thread.currentThread().getName();
		if (s != null) {
			byte[] buf = s.getBytes();
			addRandomBytes(buf, 0, buf.length);
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
		PrintStream pout = new PrintStream(bout);
		Throwable t = new Throwable();
		t.printStackTrace(pout);
		pout.flush();
		byte[] buf = bout.toByteArray();
		addRandomBytes(buf, 0, buf.length);
	}

	@Override
	public void fillBlock() throws LimitReachedException {
		if (getQuality() < 100.0) {
			slowPoll();
		}
		do {
			fastPoll();
			mixRandomPool();
		} while (mixCount < MIX_COUNT);
		if (!x917init || x917count >= X917_LIFETIME) {
			mixRandomPool(pool);
			Map<Object, Object> attr = new HashMap<>();
			byte[] key = new byte[32];
			System.arraycopy(pool, 0, key, 0, 32);
			cipher.reset();
			attr.put(IBlockCipher.KEY_MATERIAL, key);
			try {
				cipher.init(attr);
			} catch (InvalidKeyException ike) {
				throw new Error(ike.toString());
			}
			mixRandomPool(pool);
			generateX917(pool);
			mixRandomPool(pool);
			generateX917(pool);
			if (x917init)
				quality = 0.0;
			x917init = true;
			x917count = 0;
		}
		byte[] export = new byte[ALLOC_SIZE];
		for (int i = 0; i < ALLOC_SIZE; i++)
			export[i] = (byte) (pool[i] ^ 0xFF);
		mixRandomPool();
		mixRandomPool(export);
		generateX917(export);
		for (int i = 0; i < OUTPUT_SIZE; i++)
			buffer[i] = (byte) (export[i] ^ export[i + OUTPUT_SIZE]);
		Arrays.fill(export, (byte) 0);
	}

	@Override
	protected void finalize() throws Throwable {
		if (poller != null && pollerThread != null && pollerThread.isAlive()) {
			pollerThread.interrupt();
			poller.stopUpdating();
			pollerThread.interrupt();
		}
		Arrays.fill(pool, (byte) 0);
		Arrays.fill(x917pool, (byte) 0);
		Arrays.fill(buffer, (byte) 0);
	}

	private void generateX917(byte[] buf) {
		int off = 0;
		for (int i = 0; i < buf.length; i += X917_POOL_SIZE) {
			int copy = Math.min(buf.length - i, X917_POOL_SIZE);
			for (int j = 0; j < copy; j++)
				x917pool[j] ^= pool[off + j];
			cipher.encryptBlock(x917pool, 0, x917pool, 0);
			System.arraycopy(x917pool, 0, buf, off, copy);
			cipher.encryptBlock(x917pool, 0, x917pool, 0);
			off += copy;
			x917count++;
		}
	}

	synchronized double getQuality() {
		return quality;
	}

	private void mixRandomPool() {
		mixRandomPool(pool);
		mixCount++;
	}

	/**
	 * The mix operation. This method will, for every 20-byte block in the random
	 * pool, hash that block, the previous 20 bytes, and the next 44 bytes with
	 * SHA-1, writing the result back into that block.
	 */
	private void mixRandomPool(byte[] buf) {
		int hashSize = hash.hashSize();
		for (int i = 0; i < buf.length; i += hashSize) {
			// First update the bytes [p-19..p-1].
			if (i == 0)
				hash.update(buf, buf.length - hashSize, hashSize);
			else
				hash.update(buf, i - hashSize, hashSize);
			// Now the next 64 bytes.
			if (i + 64 < buf.length)
				hash.update(buf, i, 64);
			else {
				hash.update(buf, i, buf.length - i);
				hash.update(buf, 0, 64 - (buf.length - i));
			}
			byte[] digest = hash.digest();
			System.arraycopy(digest, 0, buf, i, hashSize);
		}
	}

	@Override
	public void setup(Map<Object, ?> attrib) {
		List<?> list = null;
		try {
			list = (List<?>) attrib.get(FILE_SOURCES);
			if (list != null) {
				files.clear();
				for (Iterator<?> it = list.iterator(); it.hasNext();) {
					List<?> l = (List<?>) it.next();
					if (l.size() != 4) {
						throw new IllegalArgumentException("invalid file list");
					}
					Double quality = (Double) l.get(0);
					Integer offset = (Integer) l.get(1);
					Integer length = (Integer) l.get(2);
					String source = (String) l.get(3);
					files.add(new SimpleList(quality, offset, length, source));
				}
			}
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("invalid file list");
		}
		try {
			list = (List<?>) attrib.get(URL_SOURCES);
			if (list != null) {
				urls.clear();
				for (Iterator<?> it = list.iterator(); it.hasNext();) {
					List<?> l = (List<?>) it.next();
					if (l.size() != 4) {
						throw new IllegalArgumentException("invalid URL list");
					}
					Double quality = (Double) l.get(0);
					Integer offset = (Integer) l.get(1);
					Integer length = (Integer) l.get(2);
					URL source = (URL) l.get(3);
					urls.add(new SimpleList(quality, offset, length, source));
				}
			}
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("invalid URL list");
		}
		try {
			list = (List<?>) attrib.get(PROGRAM_SOURCES);
			if (list != null) {
				progs.clear();
				for (Iterator<?> it = list.iterator(); it.hasNext();) {
					List<?> l = (List<?>) it.next();
					if (l.size() != 4) {
						throw new IllegalArgumentException("invalid program list");
					}
					Double quality = (Double) l.get(0);
					Integer offset = (Integer) l.get(1);
					Integer length = (Integer) l.get(2);
					String source = (String) l.get(3);
					progs.add(new SimpleList(quality, offset, length, source));
				}
			}
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("invalid program list");
		}
		try {
			list = (List<?>) attrib.get(OTHER_SOURCES);
			if (list != null) {
				other.clear();
				for (Iterator<?> it = list.iterator(); it.hasNext();) {
					EntropySource src = (EntropySource) it.next();
					if (src == null)
						throw new NullPointerException("null source in source list");
					other.add(src);
				}
			}
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("invalid source list");
		}

		try {
			Boolean block = (Boolean) attrib.get(BLOCKING);
			if (block != null)
				blocking = block.booleanValue();
			else
				blocking = true;
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("invalid blocking parameter");
		}
		poller = new Poller(files, urls, progs, other, this);
		try {
			fillBlock();
		} catch (LimitReachedException lre) {
			throw new RuntimeException("bootstrapping CSPRNG failed");
		}
	}

	private void slowPoll() throws LimitReachedException {
		if (pollerThread == null || !pollerThread.isAlive()) {
			boolean interrupted = false;
			pollerThread = new Thread(poller);
			pollerThread.setDaemon(true);
			pollerThread.setPriority(Thread.NORM_PRIORITY - 1);
			pollerThread.start();
			if (blocking)
				try {
					pollerThread.join();
				} catch (InterruptedException ie) {
					interrupted = true;
				}
			// If the full slow poll has completed after we waited for it,
			// and there in insufficient randomness, throw an exception.
			if (!interrupted && blocking && quality < 100.0) {
				throw new LimitReachedException("insufficient randomness was polled");
			}
		}
	}
}
