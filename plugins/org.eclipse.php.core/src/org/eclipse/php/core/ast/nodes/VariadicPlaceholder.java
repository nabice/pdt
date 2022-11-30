/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.core.ast.nodes;

import org.eclipse.php.core.PHPVersion;
import org.eclipse.php.core.ast.match.ASTMatcher;
import org.eclipse.php.core.ast.visitor.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an empty expression, mostly used for empty list
 * elements.
 * 
 * <pre>
 * e.g.
 * 
 * list($a, , ) = null; - all empty list() elements
 * array($a, ); - the empty element after trailing comma
 * foo($a, ); - the empty element after trailing comma
 * </pre>
 */
public class VariadicPlaceholder extends Expression {

	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> properyList = new ArrayList<>(0);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(properyList);
	}

	public VariadicPlaceholder(AST ast) {
		super(ast);
	}

	public VariadicPlaceholder(int start, int end, AST target) {
		super(start, end, target);
	}

	@Override
	public void accept0(Visitor visitor) {
		final boolean visit = visitor.visit(this);
		if (visit) {
			childrenAccept(visitor);
		}
		visitor.endVisit(this);
	}

	@Override
	public void childrenAccept(Visitor visitor) {
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		accept(visitor);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<VariadicPlaceholder"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append("/>"); //$NON-NLS-1$
	}

	@Override
	public int getType() {
		return ASTNode.VARIADIC_PLACEHOLDER;
	}

	/*
	 * Method declared on ASTNode.
	 */
	@Override
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		final ASTNode result = new VariadicPlaceholder(this.getStart(), this.getEnd(), target);
		return result;
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
}
