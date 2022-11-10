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
 * #[MyAttribute]
 * class ABC {
 *
 * }
 * </pre>
 */
public class Attribute extends ASTNode {

	private String name;
	private NodeList<Expression> args = new NodeList<>(ARGS_PROPERTY);


	public static final SimplePropertyDescriptor NAME_PROPERTY = new SimplePropertyDescriptor(Attribute.class,
																							  "name", String.class, MANDATORY); //$NON-NLS-1$
	public static final ChildListPropertyDescriptor ARGS_PROPERTY = new ChildListPropertyDescriptor(Attribute.class,
																									"args", Expression.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> list = new ArrayList<>(1);
		list.add(NAME_PROPERTY);
		list.add(ARGS_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(list);
	}

	public Attribute(int start, int end, AST ast, String name, List<Expression> args) {
		super(start, end, ast);

		setName(name);
		if(args != null) {
			this.args.addAll(args);
		}
	}

	public Attribute(AST ast) {
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
		for (ASTNode node : this.args) {
			node.accept(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		for (ASTNode node : this.args) {
			node.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		for (ASTNode node : this.args) {
			node.traverseTopDown(visitor);
		}
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<Attribute"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" name='").append(name).append("'>"); //$NON-NLS-1$ //$NON-NLS-2$
		for (ASTNode node : this.args) {
			node.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append("\n").append(tab).append("</Attribute>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int getType() {
		return ASTNode.ATTRIBUTE;
	}

	/**
	 * The args of this attribute
	 *
	 * @return List of args of this attribute
	 */
	public List<Expression> args() {
		return this.args;
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
		final List<Expression> args = ASTNode.copySubtrees(target, args());
		return new Attribute(this.getStart(), this.getEnd(), target, name, args);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode.
	 */
	@Override
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
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
		if (property == ARGS_PROPERTY) {
			return args();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}


	/**
	 * @return arg name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this named arg
	 */
	public final void setName(String name) {
		preValueChange(NAME_PROPERTY);
		this.name = name;
		postValueChange(NAME_PROPERTY);
	}

}
