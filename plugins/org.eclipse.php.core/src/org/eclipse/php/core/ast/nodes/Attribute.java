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

	private Expression className;
	private NodeList<Expression> args = new NodeList<>(ARGS_PROPERTY);


	public static final ChildPropertyDescriptor CLASSNAME_PROPERTY = new ChildPropertyDescriptor(
			Attribute.class, "className", Expression.class, //$NON-NLS-1$
			MANDATORY, CYCLE_RISK);
	public static final ChildListPropertyDescriptor ARGS_PROPERTY = new ChildListPropertyDescriptor(Attribute.class,
																									"args", Expression.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> list = new ArrayList<>(1);
		list.add(CLASSNAME_PROPERTY);
		list.add(ARGS_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(list);
	}

	public Attribute(int start, int end, AST ast, Expression className, List<Expression> args) {
		super(start, end, ast);
		if (className == null) {
			throw new IllegalArgumentException();
		}

		setClassName(className);
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
		if (className != null) {
			className.accept(visitor);
		}
		for (ASTNode node : this.args) {
			node.accept(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		if (className != null) {
			className.traverseBottomUp(visitor);
		}
		for (ASTNode node : this.args) {
			node.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		if (className != null) {
			className.traverseTopDown(visitor);
		}
		for (ASTNode node : this.args) {
			node.traverseTopDown(visitor);
		}
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<Attribute"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append("'>"); //$NON-NLS-1$ //$NON-NLS-2$
		if (className != null) {
			className.toString(buffer, TAB + tab);
		}
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
		final Expression cn = ASTNode.copySubtree(target, getClassName());
		final List<Expression> args = ASTNode.copySubtrees(target, args());
		return new Attribute(this.getStart(), this.getEnd(), target, cn, args);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == CLASSNAME_PROPERTY) {
			if (get) {
				return getClassName();
			} else {
				setClassName((Expression) child);
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
		if (property == ARGS_PROPERTY) {
			return args();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}



	/**
	 * Class name of this instance creation node
	 *
	 * @return class name
	 */
	public Expression getClassName() {
		return className;
	}

	/**
	 * Sets the class name of this instansiation.
	 *
	 * @param classname
	 *            the new class name
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                </ul>
	 */
	public void setClassName(Expression classname) {
		if (classname == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.className;
		preReplaceChild(oldChild, classname, CLASSNAME_PROPERTY);
		this.className = classname;
		postReplaceChild(oldChild, classname, CLASSNAME_PROPERTY);
	}

}
