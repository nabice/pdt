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

public class EnumCase extends Statement {

	private Identifier name;
	private Expression value;
	protected ASTNode.NodeList<AttributeGroup> attrGroups = new ASTNode.NodeList<>(ATTR_GROUPS_PROPERTY);

	/**
	 * The structural property of this node type.
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = new ChildPropertyDescriptor(EnumCase.class,
																							"name", Identifier.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	public static final ChildPropertyDescriptor VALUE_PROPERTY = new ChildPropertyDescriptor(EnumCase.class, "value", //$NON-NLS-1$
                                                                                             Expression.class, OPTIONAL, CYCLE_RISK);
	public static final ChildListPropertyDescriptor ATTR_GROUPS_PROPERTY = new ChildListPropertyDescriptor(
			EnumCase.class, "attrGroups", AttributeGroup.class, //$NON-NLS-1$
			NO_CYCLE_RISK);

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;

	static {
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<>(1);
		propertyList.add(NAME_PROPERTY);
		propertyList.add(VALUE_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(propertyList);
	}

	public EnumCase(int start, int end, AST ast, Identifier name, Expression value, List<AttributeGroup> attrGroups) {
		super(start, end, ast);
		setName(name);
		if (value != null) {
			setValue(value);
		}
		if (attrGroups != null) {
			this.attrGroups.addAll(attrGroups);
		}
	}

	public EnumCase(AST ast) {
		super(ast);
	}

	public List<AttributeGroup> getAttrGroups() {
		return attrGroups;
	}

	@Override
	public void accept0(Visitor visitor) {
		final boolean visit = visitor.visit(this);
		if (visit) {
			childrenAccept(visitor);
		}
		visitor.endVisit(this);
	}

	public Identifier getName() {
		return this.name;
	}

	public void setName(Identifier id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		// an Assignment may occur inside a Expression - must check cycles
		ASTNode oldChild = this.name;
		preReplaceChild(oldChild, id, NAME_PROPERTY);
		this.name = id;
		postReplaceChild(oldChild, id, NAME_PROPERTY);
	}

	@Override
	public void childrenAccept(Visitor visitor) {
		name.accept(visitor);
		if (attrGroups != null) {
			for (AttributeGroup attributeGroup : attrGroups) {
				attributeGroup.accept(visitor);
			}
		}
		if (value != null) {
			value.accept(visitor);
		}
	}

	@Override
	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		if (attrGroups != null) {
			for (AttributeGroup attrGroup : attrGroups) {
				attrGroup.traverseTopDown(visitor);
			}
		}
		name.traverseTopDown(visitor);
		if (value != null) {
			value.traverseTopDown(visitor);
		}
	}

	@Override
	public void traverseBottomUp(Visitor visitor) {
		if (attrGroups != null) {
			for (AttributeGroup attrGroup : attrGroups) {
				attrGroup.traverseBottomUp(visitor);
			}
		}
		name.traverseBottomUp(visitor);
		if (value != null) {
			value.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<EnumCase"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(tab).append(TAB).append("<Name>\n"); //$NON-NLS-1$
		getName().toString(buffer, TAB + TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(tab).append(TAB).append("</Name>\n"); //$NON-NLS-1$
		buffer.append(TAB).append(tab).append("<Value>\n"); //$NON-NLS-1$
		if (value != null) {
			value.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</Value>\n"); //$NON-NLS-1$

		buffer.append(TAB).append(tab).append("<AttributeGroups>\n"); //$NON-NLS-1$
		if (attrGroups != null) {
			for (AttributeGroup attributeGroup : attrGroups) {
				attributeGroup.toString(buffer, TAB + TAB + tab);
				buffer.append("\n"); //$NON-NLS-1$
			}
		}
		buffer.append(TAB).append(tab).append("</AttributeGroups>\n"); //$NON-NLS-1$

		buffer.append(tab).append("</EnumCase>"); //$NON-NLS-1$
	}

	@Override
	public int getType() {
		return ASTNode.ENUM_CASE;
	}

	public Expression getValue() {
		return value;
	}

	public void setValue(Expression value) {
		if (value == null) {
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
	final List<? extends ASTNode> internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == ATTR_GROUPS_PROPERTY) {
			return getAttrGroups();
		}

		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Identifier) child);
				return null;
			}
		}

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

	@Override
	ASTNode clone0(AST target) {
		final Expression value = ASTNode.copySubtree(target, getValue());
		final Identifier name = ASTNode.copySubtree(target, getName());
		final List<AttributeGroup> attrGroups = ASTNode.copySubtrees(target, getAttrGroups());
		return new EnumCase(getStart(), getEnd(), target, name, value, attrGroups);
	}

	@Override
	List<StructuralPropertyDescriptor> internalStructuralPropertiesForType(PHPVersion apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
}
