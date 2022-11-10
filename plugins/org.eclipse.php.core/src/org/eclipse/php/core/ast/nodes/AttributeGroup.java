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
 * Represents a PHP Attribute
 * 
 * <pre>
 * e.g.
 * 
 * #[MyAttribute, MyAttribute2]
 * class ABC {
 *
 * }
 * </pre>
 */
public class AttributeGroup extends ASTNode {

	private NodeList<Attribute> attrs = new NodeList<>(ATTRS_PROPERTY);


	/**
	 * The "attrs" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor ATTRS_PROPERTY = new ChildListPropertyDescriptor(AttributeGroup.class,
																									 "attrs", Attribute.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> list = new ArrayList<>(1);
		list.add(ATTRS_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(list);
	}

	public AttributeGroup(int start, int end, AST ast, List<Attribute> attrs) {
		super(start, end, ast);

		if(attrs != null) {
			this.attrs.addAll(attrs);
		}
	}

	public AttributeGroup(AST ast) {
		super(ast);
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
		for (ASTNode node : this.attrs) {
			node.accept(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		for (ASTNode node : this.attrs) {
			node.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		for (ASTNode node : this.attrs) {
			node.traverseTopDown(visitor);
		}
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<AttributeGroup"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append("'>"); //$NON-NLS-1$ //$NON-NLS-2$
		for (ASTNode node : this.attrs) {
			node.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append("\n").append(tab).append("</AttributeGroup>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int getType() {
		return ASTNode.ATTRIBUTEGROUP;
	}

	/**
	 * The attrs of this attribute group
	 *
	 * @return List of attrs of this attribute group
	 */
	public List<Attribute> attrs() {
		return this.attrs;
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
		final List<Attribute> attrs = ASTNode.copySubtrees(target, attrs());
		return new AttributeGroup(this.getStart(), this.getEnd(), target, attrs);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.php.internal.core.ast.nodes.ASTNode#
	 * internalGetChildListProperty(org.eclipse.php.internal.core.ast.nodes.
	 * ChildListPropertyDescriptor)
	 */
	@Override
	final List<? extends ASTNode> internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == ATTRS_PROPERTY) {
			return attrs();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

}
