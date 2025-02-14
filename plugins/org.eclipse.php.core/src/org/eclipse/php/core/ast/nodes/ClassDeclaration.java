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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.php.core.PHPVersion;
import org.eclipse.php.core.ast.match.ASTMatcher;
import org.eclipse.php.core.ast.visitor.Visitor;
import org.eclipse.php.core.compiler.PHPFlags;

/**
 * Represents a class declaration
 * 
 * <pre>
 * 
 * e.g.
 * 
 * class MyClass { }, class MyClass extends SuperClass implements Interface1,
 * Interface2 { const MY_CONSTANT = 3; public static final $myVar = 5, $yourVar;
 * var $anotherOne; private function myFunction($a) { } }
 * </pre>
 */
public class ClassDeclaration extends TypeDeclaration {

	public static final int MODIFIER_NONE = 0;
	public static final int MODIFIER_ABSTRACT = 1;
	public static final int MODIFIER_FINAL = 4;
	public static final int MODIFIER_TRAIT = 524288;
	public static final int MODIFIER_ENUM = 1048576;
	public static final int MODIFIER_READONLY = 262144;

	private int modifier;
	private Expression superClass;

	/**
	 * The structural property of this node type.
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = new ChildPropertyDescriptor(ClassDeclaration.class,
			"name", Identifier.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	public static final ChildListPropertyDescriptor INTERFACES_PROPERTY = new ChildListPropertyDescriptor(
			ClassDeclaration.class, "interfaces", Identifier.class, NO_CYCLE_RISK); //$NON-NLS-1$
	public static final ChildPropertyDescriptor BODY_PROPERTY = new ChildPropertyDescriptor(ClassDeclaration.class,
			"body", Block.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$
	public static final ChildPropertyDescriptor SUPER_CLASS_PROPERTY = new ChildPropertyDescriptor(
			ClassDeclaration.class, "superClass", Expression.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
	public static final SimplePropertyDescriptor MODIFIER_PROPERTY = new SimplePropertyDescriptor(
			ClassDeclaration.class, "modifier", Integer.class, OPTIONAL); //$NON-NLS-1$
	public static final ChildListPropertyDescriptor ATTR_GROUPS_PROPERTY = new ChildListPropertyDescriptor(
			ClassDeclaration.class, "attrGroups", AttributeGroup.class, //$NON-NLS-1$
			NO_CYCLE_RISK);

	@Override
	protected ChildPropertyDescriptor getBodyProperty() {
		return BODY_PROPERTY;
	}

	@Override
	protected ChildListPropertyDescriptor getInterfacesProperty() {
		return INTERFACES_PROPERTY;
	}

	@Override
	protected ChildListPropertyDescriptor getAttrGroupsProperty() {
		return ATTR_GROUPS_PROPERTY;
	}

	@Override
	protected ChildPropertyDescriptor getNameProperty() {
		return NAME_PROPERTY;
	}

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<>(5);
		propertyList.add(NAME_PROPERTY);
		propertyList.add(INTERFACES_PROPERTY);
		propertyList.add(BODY_PROPERTY);
		propertyList.add(SUPER_CLASS_PROPERTY);
		propertyList.add(MODIFIER_PROPERTY);
		propertyList.add(ATTR_GROUPS_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(propertyList);
	}

	private ClassDeclaration(int start, int end, AST ast, int modifier, Identifier className, Expression superClass,
			Identifier[] interfaces, Block body, List<AttributeGroup> attrGroups) {
		super(start, end, ast, className, interfaces, body, attrGroups);

		setModifier(modifier);
		if (superClass != null) {
			setSuperClass(superClass);
		}
	}

	public ClassDeclaration(AST ast) {
		super(ast);
	}

	public ClassDeclaration(int start, int end, AST ast, int modifier, Identifier className, Expression superClass,
							List<Identifier> interfaces, Block body) {
		this(start, end, ast, modifier, className, superClass,
			 interfaces, body, null);
	}

	public ClassDeclaration(int start, int end, AST ast, int modifier, Identifier className, Expression superClass,
			List<Identifier> interfaces, Block body, List<AttributeGroup> attrGroups) {
		this(start, end, ast, modifier, className, superClass,
				interfaces == null ? null : interfaces.toArray(new Identifier[interfaces.size()]), body, attrGroups);
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
		if (attrGroups != null) {
			for (AttributeGroup attributeGroup : attrGroups) {
				attributeGroup.accept(visitor);
			}
		}

		getName().accept(visitor);
		if (superClass != null) {
			superClass.accept(visitor);
		}
		for (Object object : interfaces()) {
			final ASTNode node = (ASTNode) object;
			node.accept(visitor);
		}
		getBody().accept(visitor);
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		if (attrGroups != null) {
			for (AttributeGroup attrGroup : attrGroups) {
				attrGroup.traverseTopDown(visitor);
			}
		}
		getName().traverseTopDown(visitor);
		if (superClass != null) {
			superClass.traverseTopDown(visitor);
		}
		for (Object object : interfaces()) {
			final ASTNode node = (ASTNode) object;
			node.traverseTopDown(visitor);
		}
		getBody().traverseTopDown(visitor);
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		if (attrGroups != null) {
			for (AttributeGroup attrGroup : attrGroups) {
				attrGroup.traverseBottomUp(visitor);
			}
		}

		getName().traverseBottomUp(visitor);
		if (superClass != null) {
			superClass.traverseBottomUp(visitor);
		}
		for (Object object : interfaces()) {
			final ASTNode node = (ASTNode) object;
			node.traverseBottomUp(visitor);
		}
		getBody().traverseBottomUp(visitor);
		accept(visitor);
	}

	public static String getModifierString(int modifier) {
		return PHPFlags.toString(modifier);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<ClassDeclaration"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" modifier='").append(getModifierString(modifier)).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(tab).append(TAB).append("<ClassName>\n"); //$NON-NLS-1$
		getName().toString(buffer, TAB + TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(tab).append(TAB).append("</ClassName>\n"); //$NON-NLS-1$

		buffer.append(tab).append(TAB).append("<SuperClassName>\n"); //$NON-NLS-1$
		if (superClass != null) {
			superClass.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(tab).append(TAB).append("</SuperClassName>\n"); //$NON-NLS-1$

		buffer.append(tab).append(TAB).append("<Interfaces>\n"); //$NON-NLS-1$
		for (Object object : interfaces()) {
			final ASTNode node = (ASTNode) object;
			node.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(tab).append(TAB).append("</Interfaces>\n"); //$NON-NLS-1$
		getBody().toString(buffer, TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$

		buffer.append(TAB).append(tab).append("<AttributeGroups>\n"); //$NON-NLS-1$
		if (attrGroups != null) {
			for (AttributeGroup attributeGroup : attrGroups) {
				attributeGroup.toString(buffer, TAB + TAB + tab);
				buffer.append("\n"); //$NON-NLS-1$
			}
		}
		buffer.append(TAB).append(tab).append("</AttributeGroups>\n"); //$NON-NLS-1$

		buffer.append(tab).append("</ClassDeclaration>"); //$NON-NLS-1$
	}

	@Override
	public int getType() {
		return ASTNode.CLASS_DECLARATION;
	}

	public int getModifier() {
		return modifier;
	}

	/**
	 * Sets the modifier of this class declaration
	 * 
	 * @param new
	 *            modifier of this class declaration
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                </ul>
	 */
	public final void setModifier(int value) {
		preValueChange(MODIFIER_PROPERTY);
		this.modifier = value;
		postValueChange(MODIFIER_PROPERTY);
	}

	public Expression getSuperClass() {
		return superClass;
	}

	/**
	 * Sets the super class name of this class declaration
	 * 
	 * @param the
	 *            super class name of this class declaration
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                </ul>
	 */
	public void setSuperClass(Expression id) {
		if (id != null && !(id instanceof Identifier) && !(id instanceof NamespaceName)) {
			throw new IllegalArgumentException();
		}
		// an Assignment may occur inside a Expression - must check cycles
		ASTNode oldChild = this.superClass;
		preReplaceChild(oldChild, id, SUPER_CLASS_PROPERTY);
		this.superClass = id;
		postReplaceChild(oldChild, id, SUPER_CLASS_PROPERTY);
	}

	@Override
	ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == SUPER_CLASS_PROPERTY) {
			if (get) {
				return getSuperClass();
			} else {
				setSuperClass((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int child) {
		if (property == MODIFIER_PROPERTY) {
			if (get) {
				return getModifier();
			} else {
				setModifier(child);
				return 0;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetIntProperty(property, get, child);
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
		final Block body = ASTNode.copySubtree(target, getBody());
		final Expression superName = ASTNode.copySubtree(target, getSuperClass());
		final int modifier = getModifier();
		final List<Identifier> interfaces = ASTNode.copySubtrees(target, interfaces());
		final Identifier name = ASTNode.copySubtree(target, getName());
		final List<AttributeGroup> attrGroups = ASTNode.copySubtrees(target, getAttrGroups());
		return new ClassDeclaration(getStart(), getEnd(), target, modifier, name, superName, interfaces, body, attrGroups);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
}
