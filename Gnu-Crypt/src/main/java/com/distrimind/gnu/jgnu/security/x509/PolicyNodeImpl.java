/* PolicyNodeImpl.java -- An implementation of a policy tree node.
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

package com.distrimind.gnu.jgnu.security.x509;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.distrimind.gnu.vm.jgnu.security.cert.PolicyNode;
import com.distrimind.gnu.vm.jgnu.security.cert.PolicyQualifierInfo;

public final class PolicyNodeImpl implements PolicyNode {

	// Fields.
	// -------------------------------------------------------------------------

	private String policy;

	private final Set<String> expectedPolicies;

	private final Set<PolicyQualifierInfo> qualifiers;

	private final Set<PolicyNodeImpl> children;

	private PolicyNodeImpl parent;

	private int depth;

	private boolean critical;

	private boolean readOnly;

	// Constructors.
	// -------------------------------------------------------------------------

	public PolicyNodeImpl() {
		expectedPolicies = new HashSet<>();
		qualifiers = new HashSet<>();
		children = new HashSet<>();
		readOnly = false;
		critical = false;
	}

	// Instance methods.
	// -------------------------------------------------------------------------

	public void addAllExpectedPolicies(Set<String> policies) {
		if (readOnly)
			throw new IllegalStateException("read only");
		expectedPolicies.addAll(policies);
	}

	public void addAllPolicyQualifiers(Collection<? extends PolicyQualifierInfo> qualifiers) {
		/*
		 * for (Iterator<? extends PolicyQualifierInfo> it = qualifiers.iterator();
		 * it.hasNext(); ) { if (!(it.next() instanceof PolicyQualifierInfo)) throw new
		 * IllegalArgumentException ("can only add PolicyQualifierInfos"); }
		 */
		this.qualifiers.addAll(qualifiers);
	}

	public void addChild(PolicyNodeImpl node) {
		if (readOnly)
			throw new IllegalStateException("read only");
		if (node.getParent() != null)
			throw new IllegalStateException("already a child node");
		node.parent = this;
		node.setDepth(depth + 1);
		children.add(node);
	}

	public void addExpectedPolicy(String policy) {
		if (readOnly)
			throw new IllegalStateException("read only");
		expectedPolicies.add(policy);
	}

	public void addPolicyQualifier(PolicyQualifierInfo qualifier) {
		if (readOnly)
			throw new IllegalStateException("read only");
		qualifiers.add(qualifier);
	}

	@Override
	public Iterator<PolicyNodeImpl> getChildren() {
		return Collections.unmodifiableSet(children).iterator();
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public Set<String> getExpectedPolicies() {
		return Collections.unmodifiableSet(expectedPolicies);
	}

	@Override
	public PolicyNode getParent() {
		return parent;
	}

	@Override
	public Set<PolicyQualifierInfo> getPolicyQualifiers() {
		return Collections.unmodifiableSet(qualifiers);
	}

	@Override
	public String getValidPolicy() {
		return policy;
	}

	@Override
	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		if (readOnly)
			throw new IllegalStateException("read only");
		this.critical = critical;
	}

	public void setDepth(int depth) {
		if (readOnly)
			throw new IllegalStateException("read only");
		this.depth = depth;
	}

	public void setReadOnly() {
		if (readOnly)
			return;
		readOnly = true;
		for (Iterator<PolicyNodeImpl> it = getChildren(); it.hasNext();)
			it.next().setReadOnly();
	}

	public void setValidPolicy(String policy) {
		if (readOnly)
			throw new IllegalStateException("read only");
		this.policy = policy;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < depth; i++)
			buf.append("  ");
		buf.append("(");
		buf.append(PolicyNodeImpl.class.getName());
		buf.append(" (oid ");
		buf.append(policy);
		buf.append(") (depth ");
		buf.append(depth);
		buf.append(") (qualifiers ");
		buf.append(qualifiers);
		buf.append(") (critical ");
		buf.append(critical);
		buf.append(") (expectedPolicies ");
		buf.append(expectedPolicies);
		buf.append(") (children (");
		final String nl = System.getProperty("line.separator");
		for (Iterator<PolicyNodeImpl> it = getChildren(); it.hasNext();) {
			buf.append(nl);
			buf.append(it.next().toString());
		}
		buf.append(")))");
		return buf.toString();
	}
}
