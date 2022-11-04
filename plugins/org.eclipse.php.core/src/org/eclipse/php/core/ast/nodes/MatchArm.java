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
 * Represents a match arm statement. A match arm statement is part of match statement
 * 
 * <pre>
 * e.g.
 *
 * 'apple' => 'This food is an apple',
 *
 *  default => 'This food is fruit',
 * </pre>
 */
public class MatchArm extends Statement {

	private Expression value;
	private NodeList<Expression> conditionals = new NodeList<>(CONDITIONALS_PROPERTY);
	private boolean isDefault;

	/**
	 * The structural property of this node type.
	 */
	public static final ChildPropertyDescriptor VALUE_PROPERTY = new ChildPropertyDescriptor(MatchArm.class, "value", //$NON-NLS-1$
                                                                                             Expression.class, OPTIONAL, CYCLE_RISK);
	public static final ChildListPropertyDescriptor CONDITIONALS_PROPERTY = new ChildListPropertyDescriptor(MatchArm.class,
																											"actions", Expression.class, CYCLE_RISK); //$NON-NLS-1$
	public static final SimplePropertyDescriptor IS_DEFAULT_PROPERTY = new SimplePropertyDescriptor(MatchArm.class,
                                                                                                    "isDefault", Boolean.class, OPTIONAL); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<>(1);
		propertyList.add(CONDITIONALS_PROPERTY);
		propertyList.add(VALUE_PROPERTY);
		propertyList.add(IS_DEFAULT_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(propertyList);
	}

	public MatchArm(int start, int end, AST ast, List<Expression> conditionals, Expression value, boolean isDefault) {
		super(start, end, ast);

		if (conditionals == null) {
			throw new IllegalArgumentException();
		}
		this.conditionals.addAll(conditionals);
		if (value != null) {
			setValue(value);
		}
		setIsDefault(isDefault);
	}

	public MatchArm(AST ast) {
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
		for (ASTNode node : this.conditionals) {
			node.accept(visitor);
		}
		if (value != null) {
			value.accept(visitor);
		}
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		for (ASTNode node : this.conditionals) {
			node.traverseTopDown(visitor);
		}
		if (value != null) {
			value.traverseTopDown(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		for (ASTNode node : this.conditionals) {
			node.traverseBottomUp(visitor);
		}
		if (value != null) {
			value.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<MatchArm"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" isDefault='").append(isDefault).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		for (ASTNode node : this.conditionals) {
			node.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("<Value>\n"); //$NON-NLS-1$
		if (value != null) {
			value.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</Value>\n"); //$NON-NLS-1$
		buffer.append(tab).append("</MatchArm>"); //$NON-NLS-1$
	}

	@Override
	public int getType() {
		return ASTNode.MATCH_ARM;
	}

	/**
	 * The conditionals of this match arm statement
	 * 
	 * @return List of conditionals of this match arm statement
	 */
	public List<Expression> conditionals() {
		return this.conditionals;
	}

	/**
	 * True if this is a default match arm statement
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * Set to true if this match arm statement represents a 'default' arm
	 * 
	 * @param isDefault
	 * @exception IllegalArgumentException
	 *                if the argument is incorrect
	 */
	public void setIsDefault(boolean isDefault) {
		preValueChange(IS_DEFAULT_PROPERTY);
		this.isDefault = isDefault;
		postValueChange(IS_DEFAULT_PROPERTY);
	}

	/**
	 * The value (expression) of this match arm statement
	 * 
	 * @return value (expression) of this match arm statement
	 */
	public Expression getValue() {
		return value;
	}

	/**
	 * Sets the value of this match arm statement
	 * 
	 * @param value
	 *            the value of this match arm statement.
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                <li>the MatchArm is the default arm</li>
	 *                </ul>
	 */
	public void setValue(Expression value) {
		if (isDefault || value == null) {
			throw new IllegalArgumentException();
		}
		// an Assignment may occur inside a Expression - must check cycles
		ASTNode oldChild = this.value;
		preReplaceChild(oldChild, value, VALUE_PROPERTY);
		this.value = value;
		postReplaceChild(oldChild, value, VALUE_PROPERTY);
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
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == IS_DEFAULT_PROPERTY) {
			if (get) {
				return isDefault();
			} else {
				setIsDefault(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == VALUE_PROPERTY) {
			if (get) {
				return getValue();
			} else {
				setValue((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
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
		if (property == CONDITIONALS_PROPERTY) {
			return conditionals();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	ASTNode clone0(AST target) {
		final boolean isDefault = isDefault();
		final Expression value = ASTNode.copySubtree(target, getValue());
		final List<Expression> conditionals = ASTNode.copySubtrees(target, conditionals());
		return new MatchArm(getStart(), getEnd(), target, conditionals, value, isDefault);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
}
