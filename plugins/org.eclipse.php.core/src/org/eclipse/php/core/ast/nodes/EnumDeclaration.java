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

import org.eclipse.php.core.ast.match.ASTMatcher;
import org.eclipse.php.core.ast.visitor.Visitor;

import java.util.List;

public class EnumDeclaration extends ClassDeclaration {
	private Identifier scalaType;

	public static final ChildPropertyDescriptor SCALA_TYPE_PROPERTY = new ChildPropertyDescriptor(
			FormalParameter.class, "scalaType", Identifier.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	public EnumDeclaration(int start, int end, AST ast, int modifier, Identifier className, List<Identifier> interfaces, Block body, Identifier type, List<AttributeGroup> attrGroups) {
		super(start, end, ast, modifier, className, null, interfaces, body, attrGroups);
		if(type != null) {
			setScalaType(type);
		}
	}

	public EnumDeclaration(AST ast) {
		super(ast);
	}

	/*
	 * Method declared on ASTNode.
	 */
	@Override
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		if (!(other instanceof EnumDeclaration)) {
			return false;
		}
		return super.subtreeMatch(matcher, other);
	}

	@Override
	ASTNode clone0(AST target) {
		final Block body = ASTNode.copySubtree(target, getBody());
		final int modifier = getModifier();
		final Identifier name = ASTNode.copySubtree(target, getName());
		final List<AttributeGroup> attrGroups = ASTNode.copySubtrees(target, getAttrGroups());
		final Identifier type = ASTNode.copySubtree(target, this.getScalaType());

		final EnumDeclaration result = new EnumDeclaration(getStart(), getEnd(), target, modifier, name, interfaces(), body, type, attrGroups);
		return result;
	}

	@Override
	public void toString(StringBuilder buffer, String tab) {
		buffer.append(tab).append("<EnumDeclaration"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$
		buffer.append(tab).append(TAB).append("<EnumName>\n"); //$NON-NLS-1$
		getName().toString(buffer, TAB + TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(tab).append(TAB).append("</EnumName>\n"); //$NON-NLS-1$
		buffer.append(TAB).append(tab).append("<ScalaType>\n"); //$NON-NLS-1$
		if (scalaType != null) {
			scalaType.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</ScalaType>\n");
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

		buffer.append(tab).append("</EnumDeclaration>"); //$NON-NLS-1$
	}

	@Override
	public int getType() {
		return ASTNode.ENUM_DECLARATION;
	}


	public Identifier getScalaType() {
		return scalaType;
	}

	public void setScalaType(Identifier scalaType) {
		Identifier oldChild = this.scalaType;
		preReplaceChild(oldChild, scalaType, SCALA_TYPE_PROPERTY);
		this.scalaType = scalaType;
		postReplaceChild(oldChild, scalaType, SCALA_TYPE_PROPERTY);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == SCALA_TYPE_PROPERTY) {
			if (get) {
				return getScalaType();
			} else {
				setScalaType((Identifier) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	public void accept0(Visitor visitor) {
		final boolean visit = visitor.visit(this);
		if (visit) {
			childrenAccept(visitor);
		}
		visitor.endVisit(this);
	}
}
