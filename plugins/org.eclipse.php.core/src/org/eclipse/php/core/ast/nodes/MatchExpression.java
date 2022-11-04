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
 * Represents a match expression.
 *
 * <pre>
 * e.g.
 *
 * $food = 'cake';
 *
 * $return_value = match ($food) {
 *     'apple' => 'This food is an apple',
 *     'bar' => 'This food is a bar',
 *     'cake' => 'This food is a cake',
 *     default => 'This food is fruit',
 * };
 *
 * </pre>
 */
public class MatchExpression extends Expression {
	private Expression subject;
	private NodeList<MatchArm> arms = new NodeList<>(ARMS_PROPERTY);

	/**
	 * The structural property of this node type.
	 */
	public static final ChildPropertyDescriptor SUBJECT_PROPERTY = new ChildPropertyDescriptor(
            MatchExpression.class, "subject", Expression.class, MANDATORY, //$NON-NLS-1$
            CYCLE_RISK);
	public static final ChildListPropertyDescriptor ARMS_PROPERTY = new ChildListPropertyDescriptor(MatchExpression.class,
																											"arms", Expression.class, CYCLE_RISK); //$NON-NLS-1$
	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> properyList = new ArrayList<>(2);
		properyList.add(SUBJECT_PROPERTY);
		properyList.add(ARMS_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(properyList);
	}

	public MatchExpression(int start, int end, AST ast, Expression subject, List<MatchArm> arms) {
		super(start, end, ast);

		setSubject(subject);
		if(arms != null) {
			this.arms.addAll(arms);
		}
	}

	public MatchExpression(AST ast) {
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
		subject.accept(visitor);
		for (ASTNode node : this.arms) {
			node.accept(visitor);
		}
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		subject.traverseTopDown(visitor);
		for (ASTNode node : this.arms) {
			node.traverseTopDown(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		subject.traverseBottomUp(visitor);
		for (ASTNode node : this.arms) {
			node.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<MatchExpression"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append("'>\n"); //$NON-NLS-1$
		subject.toString(buffer, TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		for (ASTNode node : this.arms) {
			node.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append("\n").append(tab).append("</MatchExpression>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int getType() {
		return ASTNode.MATCH_EXPRESSION;
	}

	/**
	 * The arms of this match expression
	 *
	 * @return List of arms of this match expression
	 */
	public List<MatchArm> arms() {
		return this.arms;
	}

	/**
	 * Returns the subject of this match expression.
	 *
	 * @return the subject node
	 */
	public Expression getSubject() {
		return this.subject;
	}

	/**
	 * Sets the subject of this match expression.
	 * 
	 * @param expression
	 *            the subject node
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                </ul>
	 */
	public void setSubject(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.subject;
		preReplaceChild(oldChild, expression, SUBJECT_PROPERTY);
		this.subject = expression;
		postReplaceChild(oldChild, expression, SUBJECT_PROPERTY);
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
		final Expression subject = ASTNode.copySubtree(target, getSubject());
		final List<MatchArm> arms = ASTNode.copySubtrees(target, arms());

		return new MatchExpression(this.getStart(), this.getEnd(), target, subject,
								   arms);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == SUBJECT_PROPERTY) {
			if (get) {
				return getSubject();
			} else {
				setSubject((Expression) child);
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
		if (property == ARMS_PROPERTY) {
			return arms();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
}
