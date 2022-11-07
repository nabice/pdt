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
 * Represents a named argument
 * 
 * <pre>
 * e.g.
 * 
 * array_fill(value: 50, count: 100, start_index: 0);
 * </pre>
 */
public class NamedArg extends Expression {

	private Expression expression;
	private String name;

	/**
	 * The structural property of this node type.
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY = new ChildPropertyDescriptor(NamedArg.class,
                                                                                                  "expression", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$
	public static final SimplePropertyDescriptor NAME_PROPERTY = new SimplePropertyDescriptor(NamedArg.class,
																							  "name", String.class, MANDATORY); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<>(2);
		propertyList.add(EXPRESSION_PROPERTY);
		propertyList.add(NAME_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(propertyList);
	}

	public NamedArg(int start, int end, AST ast, Expression expr, String name) {
		super(start, end, ast);

		if (expr == null) {
			throw new IllegalArgumentException();
		}
		setExpression(expr);
		setName(name);
	}

	public NamedArg(AST ast) {
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
		expression.accept(visitor);
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		expression.traverseTopDown(visitor);
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		expression.traverseBottomUp(visitor);
		accept(visitor);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<NamedArg"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" name='").append(name).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		expression.toString(buffer, TAB + tab);
		buffer.append("\n").append(tab).append("</NamedArg>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int getType() {
		return ASTNode.NAMED_ARG;
	}

	/**
	 * Returns the expression of this named arg.
	 * 
	 * @return the expression node
	 */
	public Expression getExpression() {
		return expression;
	}

	/**
	 * Sets the expression of this named arg.
	 * 
	 * @param expression
	 *            the new expression node
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                </ul>
	 */
	public void setExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.expression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.expression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
			} else {
				setExpression((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
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
	 * Method declared on ASTNode.
	 */
	@Override
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		final Expression expression = ASTNode.copySubtree(target, this.getExpression());
		final NamedArg result = new NamedArg(this.getStart(), this.getEnd(), target, expression,
                                             this.getName());
		return result;
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
}
