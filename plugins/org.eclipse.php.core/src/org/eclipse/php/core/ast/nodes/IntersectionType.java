/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
 * Represents a union type.
 * 
 * <pre>
 * e.g.
 * 
 * T1 | T2
 * </pre>
 */
public class IntersectionType extends Expression {

	private final NodeList<Identifier> types = new NodeList<>(TYPES_PROPERTY);

	/**
	 * The structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor TYPES_PROPERTY = new ChildListPropertyDescriptor(
            IntersectionType.class, "types", Identifier.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> properyList = new ArrayList<>(2);
		properyList.add(TYPES_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(properyList);
	}

	public IntersectionType(AST ast) {
		super(ast);
	}

	public IntersectionType(int start, int end, AST ast, List<Identifier> types) {
		super(start, end, ast);

		if (types == null) {
			throw new IllegalArgumentException();
		}
		this.types.addAll(types);
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
		for (ASTNode node : this.types) {
			node.accept(visitor);
		}
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		for (ASTNode node : this.types) {
			node.traverseTopDown(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		for (ASTNode node : this.types) {
			node.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<IntersectionType"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$
		for (ASTNode node : this.types) {
			node.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(tab).append("</IntersectionType>"); //$NON-NLS-1$
	}

	@Override
	public int getType() {
		return ASTNode.INTERSECTION_TYPE;
	}

	/**
	 * @return the list of types
	 */
	public List<Identifier> types() {
		return types;
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
	final List<? extends ASTNode> internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == TYPES_PROPERTY) {
			return types();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	ASTNode clone0(AST target) {
		final List<Identifier> variables = ASTNode.copySubtrees(target, types());
		return new IntersectionType(getStart(), getEnd(), target, variables);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
}
