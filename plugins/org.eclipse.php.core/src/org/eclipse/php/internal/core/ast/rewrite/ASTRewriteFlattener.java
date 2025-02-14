/*******************************************************************************
 * Copyright (c) 2009-2019 IBM Corporation and others.
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
 *     Kaloyan Raev - Bug 486099 - Extra line inserted when inserting ExpressionStatement
 *******************************************************************************/
package org.eclipse.php.internal.core.ast.rewrite;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.php.core.ast.nodes.*;
import org.eclipse.php.core.ast.visitor.AbstractVisitor;
import org.eclipse.php.core.compiler.PHPFlags;

/**
 * This class can be called on a newly created empty node that will serve as a
 * container for an existing node that should be moved in the AST using a
 * NodeRewriteEvent. At first place, this new node was created using
 * <code>ASTRewrite#createMoveTarget(ASTNode node)</code> which in turn called
 * <code>NodeInfoStore#newPlaceholderNode(int nodeType)</code> and
 * <code>AST#createInstance(int nodeType)</code>. The
 * <code>AST#createInstance(int nodeType)</code> creates nodes using the
 * minimalistic constructor <code>ASTNode(AST ast)</code>, so many ASTNode (or
 * subtype) instances will not have their required (non-null) node attributes
 * properly initialized, leading to NPE while using the ASTRewriteFlattener
 * class.<br>
 * <br>
 * There are many possible solutions to initialize required node attributes:<br>
 * - initialize the required node attributes with fake values in
 * NodeInfoStore#newPlaceholderNode(int nodeType)<br>
 * - allow code rewrite for only a set of container nodes in
 * AST#createInstance(int nodeType)<br>
 * - consolidate the checks in class ASTRewriteFlattener to be able to work on
 * nodes (created using the constructor <code>ASTNode(AST ast)</code>) whose
 * required attributes were not initialized<br>
 *
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=552366
 * @see {@link ASTRewriteFormatter#getFormattedResult(ASTNode, int, java.util.Collection)}
 * @see {@link ASTRewriteFlattener#visitList(ASTNode, StructuralPropertyDescriptor, String)}
 *
 */
public class ASTRewriteFlattener extends AbstractVisitor {

	public static String asString(ASTNode node, RewriteEventStore store) {
		ASTRewriteFlattener flattener = new ASTRewriteFlattener(store);
		node.accept(flattener);
		return flattener.getResult();
	}

	protected StringBuilder result;
	private RewriteEventStore store;

	public ASTRewriteFlattener(RewriteEventStore store) {
		this.store = store;
		this.result = new StringBuilder();
	}

	/**
	 * Returns the string accumulated in the visit.
	 * 
	 * @return the serialized
	 */
	public String getResult() {
		// convert to a string, but lose any extra space in the string buffer by
		// copying
		return new String(this.result.toString());
	}

	/**
	 * Resets this printer so that it can be used again.
	 */
	public void reset() {
		this.result.setLength(0);
	}

	/**
	 * Appends the text representation of the given modifier modifiers, followed
	 * by a single space.
	 * 
	 * @param modifiers
	 *            the modifiers
	 * @param buf
	 *            The <code>StringBuffer</code> to write the result to.
	 */
	public static void printModifiers(int modifiers, StringBuilder buf) {
		if (PHPFlags.isPublic(modifiers)) {
			buf.append("public "); //$NON-NLS-1$
		}
		if (PHPFlags.isProtected(modifiers)) {
			buf.append("protected "); //$NON-NLS-1$
		}
		if (PHPFlags.isPrivate(modifiers)) {
			buf.append("private "); //$NON-NLS-1$
		}
		if (PHPFlags.isStatic(modifiers)) {
			buf.append("static "); //$NON-NLS-1$
		}
		if (PHPFlags.isAbstract(modifiers)) {
			buf.append("abstract "); //$NON-NLS-1$
		}
		if (PHPFlags.isFinal(modifiers)) {
			buf.append("final "); //$NON-NLS-1$
		}
	}

	protected List<?> getChildList(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		Object ret = getAttribute(parent, childProperty);
		if (ret instanceof List) {
			return (List<?>) ret;
		}
		return Collections.emptyList();
	}

	protected ASTNode getChildNode(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return (ASTNode) getAttribute(parent, childProperty);
	}

	protected int getIntAttribute(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return ((Integer) getAttribute(parent, childProperty)).intValue();
	}

	protected boolean getBooleanAttribute(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return ((Boolean) getAttribute(parent, childProperty)).booleanValue();
	}

	protected Object getAttribute(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		if (store != null) {
			return this.store.getNewValue(parent, childProperty);
		}

		return null;
	}

	protected void visitList(ASTNode parent, StructuralPropertyDescriptor childProperty, String separator) {
		List<?> list = getChildList(parent, childProperty);
		for (int i = 0; i < list.size(); i++) {
			if (separator != null && i > 0) {
				this.result.append(separator);
			}
			((ASTNode) list.get(i)).accept(this);
		}
	}

	protected void visitList(ASTNode parent, StructuralPropertyDescriptor childProperty, String separator, String lead,
			String post) {
		List<?> list = getChildList(parent, childProperty);
		if (!list.isEmpty()) {
			this.result.append(lead);
			for (int i = 0; i < list.size(); i++) {
				if (separator != null && i > 0) {
					this.result.append(separator);
				}
				((ASTNode) list.get(i)).accept(this);
			}
			this.result.append(post);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.php.core.ast.visitor.AbstractVisitor#visit(org. eclipse
	 * .php.internal.core.ast.nodes.ArrayAccess)
	 */
	@Override
	public boolean visit(ArrayAccess arrayAccess) {
		if (arrayAccess.getName() != null) {
			arrayAccess.getName().accept(this);
		}
		boolean isVariableHashtable = arrayAccess.getArrayType() == ArrayAccess.VARIABLE_HASHTABLE;
		if (isVariableHashtable) {
			result.append('{');
		} else {
			result.append('[');
		}
		if (arrayAccess.getIndex() != null) {
			arrayAccess.getIndex().accept(this);
		}
		if (isVariableHashtable) {
			result.append('}');
		} else {
			result.append(']');
		}
		return false;
	}

	@Override
	public boolean visit(ArrayCreation arrayCreation) {
		result.append("array("); //$NON-NLS-1$
		Iterator<ArrayElement> elements = arrayCreation.elements().iterator();
		if (elements.hasNext()) {
			elements.next().accept(this);
			while (elements.hasNext()) {
				result.append(","); //$NON-NLS-1$
				elements.next().accept(this);
			}
		}
		result.append(")"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(ArrayElement arrayElement) {
		if (arrayElement.getKey() != null) {
			arrayElement.getKey().accept(this);
			result.append("=>"); //$NON-NLS-1$
		}
		if (arrayElement.getValue() != null) {
			arrayElement.getValue().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ArraySpreadElement arraySpreadElement) {
		result.append("..."); //$NON-NLS-1$
		if (arraySpreadElement.getValue() != null) {
			arraySpreadElement.getValue().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(Assignment assignment) {
		if (assignment.getLeftHandSide() != null) {
			assignment.getLeftHandSide().accept(this);
		}
		result.append(Assignment.getOperator(assignment.getOperator()));
		if (assignment.getRightHandSide() != null) {
			assignment.getRightHandSide().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ASTError astError) {
		// cant flatten, needs source
		return false;
	}

	@Override
	public boolean visit(BackTickExpression backTickExpression) {
		result.append("`"); //$NON-NLS-1$
		for (Expression expr : backTickExpression.expressions()) {
			expr.accept(this);
		}
		result.append("`"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(Block block) {
		// Handle non-bracketed NamespaceDeclaration body (see also
		// CodeFormatterVisitor#visit(Block block)):
		if (!block.isCurly() && block.getParent() != null && block.getParent().getType() == ASTNode.NAMESPACE) {
			visitList(block, Block.STATEMENTS_PROPERTY, null);
			return false;
		}

		if (block.isCurly()) {
			result.append("{\n"); //$NON-NLS-1$
		} else {
			result.append(":\n"); //$NON-NLS-1$
		}

		visitList(block, Block.STATEMENTS_PROPERTY, null);

		if (block.isCurly()) {
			result.append("}\n"); //$NON-NLS-1$
		} else {
			StructuralPropertyDescriptor locationInParent = block.getLocationInParent();
			if (locationInParent == IfStatement.TRUE_STATEMENT_PROPERTY) {
				if (((IfStatement) block.getParent()).getFalseStatement() == null) {
					// End the if statement
					result.append("endif;\n"); //$NON-NLS-1$
				} else {
					// Just add a new line char
					result.append("\n"); //$NON-NLS-1$
				}
			} else if (locationInParent == IfStatement.FALSE_STATEMENT_PROPERTY) {
				result.append("endif;\n"); //$NON-NLS-1$
			} else if (locationInParent == WhileStatement.BODY_PROPERTY) {
				result.append("endwhile;\n"); //$NON-NLS-1$
			} else if (locationInParent == ForStatement.BODY_PROPERTY) {
				result.append("endfor;\n"); //$NON-NLS-1$
			} else if (locationInParent == ForEachStatement.STATEMENT_PROPERTY) {
				result.append("endforeach;\n"); //$NON-NLS-1$
			} else if (locationInParent == SwitchStatement.BODY_PROPERTY) {
				result.append("endswitch;\n"); //$NON-NLS-1$
			}
		}
		return false;
	}

	@Override
	public boolean visit(BreakStatement breakStatement) {
		result.append("break"); //$NON-NLS-1$
		if (breakStatement.getExpression() != null) {
			result.append(' ');
			breakStatement.getExpression().accept(this);
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(CastExpression castExpression) {
		result.append("("); //$NON-NLS-1$
		result.append(CastExpression.getCastType(castExpression.getCastingType()));
		result.append(")"); //$NON-NLS-1$
		if (castExpression.getExpression() != null) {
			castExpression.getExpression().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(CatchClause catchClause) {
		result.append("catch ("); //$NON-NLS-1$
		int size = catchClause.getClassNames().size();
		if (size > 0) {
			catchClause.getClassNames().get(0).accept(this);
			for (int i = 1; i < size; i++) {
				result.append(" | "); //$NON-NLS-1$
				Expression className = catchClause.getClassNames().get(i);
				className.accept(this);
			}
		}
		result.append(" "); //$NON-NLS-1$
		if (catchClause.getVariable() != null) {
			catchClause.getVariable().accept(this);
		}
		result.append(") "); //$NON-NLS-1$
		if (catchClause.getBody() != null) {
			catchClause.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(FinallyClause finallyClause) {
		result.append("finally "); //$NON-NLS-1$
		if (finallyClause.getBody() != null) {
			finallyClause.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ConstantDeclaration classConstantDeclaration) {
		result.append("const "); //$NON-NLS-1$
		boolean isFirst = true;
		List<Identifier> variableNames = classConstantDeclaration.names();
		List<Expression> constantValues = classConstantDeclaration.initializers();
		for (int i = 0; i < variableNames.size(); i++) {
			if (!isFirst) {
				result.append(", "); //$NON-NLS-1$
			}
			variableNames.get(i).accept(this);
			result.append(" = "); //$NON-NLS-1$
			constantValues.get(i).accept(this);
			isFirst = false;
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(TraitDeclaration traitDeclaration) {
		result.append("trait "); //$NON-NLS-1$
		if (traitDeclaration.getName() != null) {
			traitDeclaration.getName().accept(this);
		}
		if (traitDeclaration.getSuperClass() != null) {
			result.append(" extends "); //$NON-NLS-1$
			traitDeclaration.getSuperClass().accept(this);
		}
		if (traitDeclaration.getBody() != null) {
			traitDeclaration.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ClassDeclaration classDeclaration) {
		int modifier = classDeclaration.getModifier();
		if (modifier != ClassDeclaration.MODIFIER_NONE) {
			result.append(ClassDeclaration.getModifierString(modifier));
			result.append(' ');
		}
		result.append("class "); //$NON-NLS-1$
		if (classDeclaration.getName() != null) {
			classDeclaration.getName().accept(this);
		}
		if (classDeclaration.getSuperClass() != null) {
			result.append(" extends "); //$NON-NLS-1$
			classDeclaration.getSuperClass().accept(this);
		}
		Iterator<Identifier> iterator = classDeclaration.interfaces().iterator();
		if (!iterator.hasNext()) {
			result.append(" implements "); //$NON-NLS-1$
			iterator.next().accept(this);
			while (iterator.hasNext()) {
				result.append(" , "); //$NON-NLS-1$
				iterator.next().accept(this);
			}
		}
		if (classDeclaration.getBody() != null) {
			classDeclaration.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation) {
		result.append("new "); //$NON-NLS-1$
		if (classInstanceCreation.getClassName() != null) {
			classInstanceCreation.getClassName().accept(this);
			if (classInstanceCreation.getEnd() != classInstanceCreation.getClassName().getEnd()
					|| classInstanceCreation.getClassName().getStart() == -1) {
				result.append("("); //$NON-NLS-1$
			}
		}
		Iterator<Expression> ctorParams = classInstanceCreation.ctorParams().iterator();
		if (ctorParams.hasNext()) {
			ctorParams.next().accept(this);
			while (ctorParams.hasNext()) {
				result.append(","); //$NON-NLS-1$
				ctorParams.next().accept(this);
			}
		}
		if (classInstanceCreation.getClassName() != null) {
			if (classInstanceCreation.getEnd() != classInstanceCreation.getClassName().getEnd()
					|| classInstanceCreation.getClassName().getStart() == -1) {
				result.append(")"); //$NON-NLS-1$
			}
		}
		return false;
	}

	@Override
	public boolean visit(ClassName className) {
		if (className.getName() != null) {
			className.getName().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(CloneExpression cloneExpression) {
		result.append("clone "); //$NON-NLS-1$
		if (cloneExpression.getExpression() != null) {
			cloneExpression.getExpression().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(Comment comment) {
		result.append(getComment(comment));
		result.append("\n"); //$NON-NLS-1$
		return false;
	}

	public String getComment(Comment comment) {
		if (comment.getCommentType() == Comment.TYPE_SINGLE_LINE) {
			return "//"; //$NON-NLS-1$
		}
		if (comment.getCommentType() == Comment.TYPE_MULTILINE) {
			return "/* */"; //$NON-NLS-1$
		}
		if (comment.getCommentType() == Comment.TYPE_PHPDOC) {
			return "/** */"; //$NON-NLS-1$ "
		}
		return null;
	}

	@Override
	public boolean visit(ConditionalExpression conditionalExpression) {
		if (conditionalExpression.getCondition() != null) {
			conditionalExpression.getCondition().accept(this);
		}
		if (conditionalExpression.getOperatorType() == ConditionalExpression.OP_TERNARY) {
			result.append(" ? "); //$NON-NLS-1$
			if (conditionalExpression.getIfTrue() != null) {
				conditionalExpression.getIfTrue().accept(this);
			}
			result.append(" : "); //$NON-NLS-1$
			if (conditionalExpression.getIfFalse() != null) {
				conditionalExpression.getIfFalse().accept(this);
			}
		} else if (conditionalExpression.getOperatorType() == ConditionalExpression.OP_COALESCE) {
			result.append(" ?? "); //$NON-NLS-1$
			if (conditionalExpression.getIfTrue() != null) {
				conditionalExpression.getIfTrue().accept(this);
			}
		}
		return false;
	}

	@Override
	public boolean visit(ContinueStatement continueStatement) {
		result.append("continue "); //$NON-NLS-1$
		if (continueStatement.getExpression() != null) {
			continueStatement.getExpression().accept(this);
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(DeclareStatement declareStatement) {
		result.append("declare ("); //$NON-NLS-1$
		boolean isFirst = true;
		List<Identifier> directiveNames = declareStatement.directiveNames();
		List<Expression> directiveValues = declareStatement.directiveValues();
		for (int i = 0; i < directiveNames.size(); i++) {
			if (!isFirst) {
				result.append(", "); //$NON-NLS-1$
			}
			directiveNames.get(i).accept(this);
			result.append(" = "); //$NON-NLS-1$
			directiveValues.get(i).accept(this);
			isFirst = false;
		}
		result.append(")"); //$NON-NLS-1$
		if (declareStatement.getBody() != null) {
			declareStatement.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(DoStatement doStatement) {
		result.append("do "); //$NON-NLS-1$
		Statement body = doStatement.getBody();

		if (body != null) {
			body.accept(this);
		}
		result.append("while ("); //$NON-NLS-1$
		Expression cond = doStatement.getCondition();
		if (cond != null) {
			cond.accept(this);
		}
		result.append(");\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(EchoStatement echoStatement) {
		result.append("echo "); //$NON-NLS-1$
		List<Expression> expressions = echoStatement.expressions();
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).accept(this);
			if (i + 1 < expressions.size()) {
				result.append(", "); //$NON-NLS-1$
			}
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(EmptyStatement emptyStatement) {
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(EmptyExpression emptyExpression) {
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement expressionStatement) {
		if (expressionStatement.getExpression() != null) {
			expressionStatement.getExpression().accept(this);
			result.append(";"); //$NON-NLS-1$
		} else {
			result.append("Missing();"); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public boolean visit(FieldAccess fieldAccess) {
		if (fieldAccess.getDispatcher() != null) {
			fieldAccess.getDispatcher().accept(this);
		}
		result.append("->"); //$NON-NLS-1$
		if (fieldAccess.getField() != null) {
			fieldAccess.getField().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(FieldsDeclaration fieldsDeclaration) {
		Variable[] variableNames = fieldsDeclaration.getVariableNames();
		Expression[] initialValues = fieldsDeclaration.getInitialValues();
		for (int i = 0; i < variableNames.length; i++) {
			result.append(fieldsDeclaration.getModifierString() + " "); //$NON-NLS-1$
			if (fieldsDeclaration.getFieldsType() != null) {
				fieldsDeclaration.getFieldsType().accept(this);
				result.append(" "); //$NON-NLS-1$
			}
			variableNames[i].accept(this);
			if (initialValues[i] != null) {
				result.append(" = "); //$NON-NLS-1$
				initialValues[i].accept(this);
			}
			result.append(";\n"); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public boolean visit(ForEachStatement forEachStatement) {
		result.append("foreach ("); //$NON-NLS-1$
		Expression express = forEachStatement.getExpression();
		if (express != null) {
			express.accept(this);
		}
		result.append(" as "); //$NON-NLS-1$
		if (forEachStatement.getKey() != null) {
			forEachStatement.getKey().accept(this);
			result.append(" => "); //$NON-NLS-1$
		}
		Expression value = forEachStatement.getValue();
		if (value != null) {
			value.accept(this);
		}
		result.append(")"); //$NON-NLS-1$
		if (forEachStatement.getStatement() != null) {
			forEachStatement.getStatement().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(NamespaceDeclaration namespaceDeclaration) {
		result.append("namespace "); //$NON-NLS-1$
		if (namespaceDeclaration.getName() != null) {
			namespaceDeclaration.getName().accept(this);
		}
		if (!namespaceDeclaration.isBracketed()) {
			result.append(";\n"); //$NON-NLS-1$
		}
		if (namespaceDeclaration.getBody() != null) {
			namespaceDeclaration.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(NamespaceName namespaceName) {
		if (namespaceName.isGlobal()) {
			result.append("\\"); //$NON-NLS-1$
		}
		if (namespaceName.isCurrent()) {
			result.append("namespace\\"); //$NON-NLS-1$
		}
		List<Identifier> segments = namespaceName.segments();
		Iterator<Identifier> it = segments.iterator();
		while (it.hasNext()) {
			it.next().accept(this);
			if (it.hasNext()) {
				result.append("\\"); //$NON-NLS-1$
			}
		}
		return false;
	}

	@Override
	public boolean visit(UseStatement useStatement) {
		result.append("use "); //$NON-NLS-1$
		if (useStatement.getStatementType() == UseStatement.T_FUNCTION) {
			result.append("function "); //$NON-NLS-1$
		} else if (useStatement.getStatementType() == UseStatement.T_CONST) {
			result.append("const "); //$NON-NLS-1$
		}
		if (useStatement.getNamespace() != null) {
			useStatement.getNamespace().accept(this);
			result.append("\\{"); //$NON-NLS-1$
		}
		Iterator<UseStatementPart> it = useStatement.parts().iterator();
		while (it.hasNext()) {
			it.next().accept(this);
			if (it.hasNext()) {
				result.append(", "); //$NON-NLS-1$
			}
		}
		if (useStatement.getNamespace() != null) {
			result.append("}"); //$NON-NLS-1$
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(UseStatementPart useStatementPart) {
		if (useStatementPart.getParent() instanceof UseStatement
				&& ((UseStatement) useStatementPart.getParent()).getStatementType() == UseStatement.T_NONE) {
			if (useStatementPart.getStatementType() == UseStatement.T_FUNCTION) {
				result.append("function "); //$NON-NLS-1$
			} else if (useStatementPart.getStatementType() == UseStatement.T_CONST) {
				result.append("const "); //$NON-NLS-1$
			}
		}
		if (useStatementPart.getName() != null) {
			useStatementPart.getName().accept(this);
		}
		Identifier alias = useStatementPart.getAlias();
		if (alias != null) {
			result.append(" as "); //$NON-NLS-1$
			alias.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(FormalParameter formalParameter) {
		Expression paramType = formalParameter.getParameterType();
		if (paramType != null /* && paramType.getLength() > 0 */) {
			paramType.accept(this);
			result.append(' ');
		}

		if (formalParameter.isVariadic()) {
			result.append("..."); //$NON-NLS-1$
		}

		if (formalParameter.getParameterName() != null) {
			formalParameter.getParameterName().accept(this);
		}
		Expression defaultValue = formalParameter.getDefaultValue();
		if (defaultValue != null /* && defaultValue.getLength() > 0 */) {
			result.append(" = "); //$NON-NLS-1$
			defaultValue.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ForStatement forStatement) {
		result.append("for ("); //$NON-NLS-1$

		Iterator<Expression> expressions = forStatement.initializers().iterator();
		if (expressions.hasNext()) {
			expressions.next().accept(this);
			while (expressions.hasNext()) {
				result.append(", "); //$NON-NLS-1$
				expressions.next().accept(this);
			}
		}

		result.append(" ; "); //$NON-NLS-1$
		expressions = forStatement.conditions().iterator();
		if (expressions.hasNext()) {
			expressions.next().accept(this);
			while (expressions.hasNext()) {
				result.append(", "); //$NON-NLS-1$
				expressions.next().accept(this);
			}
		}

		result.append(" ; "); //$NON-NLS-1$
		expressions = forStatement.updaters().iterator();
		if (expressions.hasNext()) {
			expressions.next().accept(this);
			while (expressions.hasNext()) {
				result.append(", "); //$NON-NLS-1$
				expressions.next().accept(this);
			}
		}
		result.append(" ) "); //$NON-NLS-1$
		Statement body = forStatement.getBody();
		if (body != null) {
			body.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(FunctionDeclaration functionDeclaration) {
		result.append(" function "); //$NON-NLS-1$
		if (functionDeclaration.isReference()) {
			result.append('&');
		}
		if (functionDeclaration.getFunctionName() != null) {
			functionDeclaration.getFunctionName().accept(this);
		}
		result.append('(');
		List<FormalParameter> formalParametersList = functionDeclaration.formalParameters();
		FormalParameter[] formalParameters = formalParametersList
				.toArray(new FormalParameter[formalParametersList.size()]);
		if (formalParameters.length != 0) {
			formalParameters[0].accept(this);
			for (int i = 1; i < formalParameters.length; i++) {
				result.append(", "); //$NON-NLS-1$
				formalParameters[i].accept(this);
			}

		}
		result.append(')');
		if (functionDeclaration.getReturnType() != null) {
			result.append(':');
			functionDeclaration.getReturnType().accept(this);
		}
		Block body = functionDeclaration.getBody();
		if (body != null) {
			body.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(FunctionInvocation functionInvocation) {
		if (functionInvocation.getFunctionName() != null) {
			functionInvocation.getFunctionName().accept(this);
		}
		result.append('(');
		Iterator<Expression> parameters = functionInvocation.parameters().iterator();
		if (parameters.hasNext()) {
			parameters.next().accept(this);
			while (parameters.hasNext()) {
				result.append(',');
				parameters.next().accept(this);
			}
		}
		result.append(')');
		return false;
	}

	@Override
	public boolean visit(FunctionName functionName) {
		if (functionName.getName() != null) {
			functionName.getName().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(GlobalStatement globalStatement) {
		result.append("global "); //$NON-NLS-1$
		Iterator<Variable> variables = globalStatement.variables().iterator();
		if (variables.hasNext()) {
			variables.next().accept(this);
			while (variables.hasNext()) {
				result.append(", "); //$NON-NLS-1$
				variables.next().accept(this);
			}
		}
		result.append(";\n "); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(GotoLabel gotoLabel) {
		if (gotoLabel.getName() != null) {
			gotoLabel.getName().accept(this);
		}
		result.append(":\n "); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(GotoStatement gotoStatement) {
		result.append("goto "); //$NON-NLS-1$
		if (gotoStatement.getLabel() != null) {
			gotoStatement.getLabel().accept(this);
		}
		result.append(";\n "); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(Identifier identifier) {
		result.append(identifier.getName());
		return false;
	}

	@Override
	public boolean visit(IfStatement ifStatement) {
		result.append("if("); //$NON-NLS-1$
		Expression cond = ifStatement.getCondition();
		if (cond != null) {
			cond.accept(this);
		}
		result.append(")"); //$NON-NLS-1$
		Statement trueStatement = ifStatement.getTrueStatement();
		if (trueStatement != null) {
			trueStatement.accept(this);
		}
		if (ifStatement.getFalseStatement() != null) {
			result.append("else"); //$NON-NLS-1$
			ifStatement.getFalseStatement().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(IgnoreError ignoreError) {
		result.append("@"); //$NON-NLS-1$
		if (ignoreError.getExpression() != null) {
			ignoreError.getExpression().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(Include include) {
		result.append(Include.getType(include.getIncludeType()));
		result.append(" ("); //$NON-NLS-1$
		if (include.getExpression() != null) {
			include.getExpression().accept(this);
		}
		result.append(")"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(InfixExpression infixExpression) {
		if (infixExpression.getLeft() != null) {
			infixExpression.getLeft().accept(this);
		}
		result.append(' ');
		result.append(InfixExpression.getOperator(infixExpression.getOperator()));
		result.append(' ');
		if (infixExpression.getRight() != null) {
			infixExpression.getRight().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(InLineHtml inLineHtml) {
		// cant flatten, needs source
		return false;
	}

	@Override
	public boolean visit(InstanceOfExpression instanceOfExpression) {
		if (instanceOfExpression.getExpression() != null) {
			instanceOfExpression.getExpression().accept(this);
		}
		result.append(" instanceof "); //$NON-NLS-1$
		if (instanceOfExpression.getClassName() != null) {
			instanceOfExpression.getClassName().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(InterfaceDeclaration interfaceDeclaration) {
		result.append("interface "); //$NON-NLS-1$
		if (interfaceDeclaration.getName() != null) {
			interfaceDeclaration.getName().accept(this);
		}
		List<Identifier> interfaces;
		if (interfaceDeclaration.interfaces().size() > 0) {
			result.append(" extends "); //$NON-NLS-1$
			boolean isFirst = true;
			interfaces = interfaceDeclaration.interfaces();
			for (Identifier interfaceItem : interfaces) {
				if (!isFirst) {
					result.append(", "); //$NON-NLS-1$
				}
				interfaceItem.accept(this);
				isFirst = false;
			}
		}
		if (interfaceDeclaration.getBody() != null) {
			interfaceDeclaration.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ListVariable listVariable) {
		result.append("list("); //$NON-NLS-1$
		Iterator<Expression> variables = listVariable.variables().iterator();
		if (variables.hasNext()) {
			variables.next().accept(this);
			while (variables.hasNext()) {
				result.append(", "); //$NON-NLS-1$
				variables.next().accept(this);
			}
		}
		result.append(")"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(LambdaFunctionDeclaration functionDeclaration) {
		if (functionDeclaration.isStatic()) {
			result.append(" static"); //$NON-NLS-1$
		}
		result.append(" function "); //$NON-NLS-1$
		if (functionDeclaration.isReference()) {
			result.append('&');
		}
		result.append('(');
		List<FormalParameter> formalParametersList = functionDeclaration.formalParameters();
		Iterator<FormalParameter> paramIt = formalParametersList.iterator();
		while (paramIt.hasNext()) {
			paramIt.next().accept(this);
			if (paramIt.hasNext()) {
				result.append(", "); //$NON-NLS-1$
			}
		}
		result.append(')');

		List<Expression> lexicalVariables = functionDeclaration.lexicalVariables();
		if (lexicalVariables.size() > 0) {
			result.append(" use ("); //$NON-NLS-1$
			Iterator<Expression> it = lexicalVariables.iterator();
			while (it.hasNext()) {
				it.next().accept(this);
				if (it.hasNext()) {
					result.append(", "); //$NON-NLS-1$
				}
			}
			result.append(')');
		}

		if (functionDeclaration.getReturnType() != null) {
			result.append(':');
			functionDeclaration.getReturnType().accept(this);
		}

		if (functionDeclaration.getBody() != null) {
			functionDeclaration.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ArrowFunctionDeclaration functionDeclaration) {
		if (functionDeclaration.isStatic()) {
			result.append(" static"); //$NON-NLS-1$
		}
		result.append(" fn "); //$NON-NLS-1$
		if (functionDeclaration.isReference()) {
			result.append('&');
		}
		result.append('(');
		List<FormalParameter> formalParametersList = functionDeclaration.formalParameters();
		Iterator<FormalParameter> paramIt = formalParametersList.iterator();
		while (paramIt.hasNext()) {
			paramIt.next().accept(this);
			if (paramIt.hasNext()) {
				result.append(", "); //$NON-NLS-1$
			}
		}
		result.append(')');

		if (functionDeclaration.getReturnType() != null) {
			result.append(':');
			functionDeclaration.getReturnType().accept(this);
			result.append(' ');
		}

		if (functionDeclaration.getBody() != null) {
			functionDeclaration.getBody().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		Comment comment = methodDeclaration.getComment();
		if (comment != null) {
			comment.accept(this);
		}
		result.append(methodDeclaration.getModifierString());
		if (methodDeclaration.getFunction() != null) {
			methodDeclaration.getFunction().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (methodInvocation.getDispatcher() != null) {
			methodInvocation.getDispatcher().accept(this);
		}
		result.append("->"); //$NON-NLS-1$
		if (methodInvocation.getMethod() != null) {
			methodInvocation.getMethod().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ParenthesisExpression parenthesisExpression) {
		result.append("("); //$NON-NLS-1$
		if (parenthesisExpression.getExpression() != null) {
			parenthesisExpression.getExpression().accept(this);
		}
		result.append(")"); //$NON-NLS-1$

		return false;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpressions) {
		if (postfixExpressions.getVariable() != null) {
			postfixExpressions.getVariable().accept(this);
		}
		result.append(PostfixExpression.getOperator(postfixExpressions.getOperator()));
		return false;
	}

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		if (prefixExpression.getVariable() != null) {
			prefixExpression.getVariable().accept(this);
		}
		result.append(PrefixExpression.getOperator(prefixExpression.getOperator()));
		return false;
	}

	@Override
	public boolean visit(Program program) {
		boolean isPhpState = false;
		for (Statement statement : program.statements()) {
			boolean isHtml = statement instanceof InLineHtml;

			if (!isHtml && !isPhpState) {
				// html -> php
				result.append("<?php\n"); //$NON-NLS-1$
				statement.accept(this);
				isPhpState = true;
			} else if (!isHtml && isPhpState) {
				// php -> php
				statement.accept(this);
				result.append("\n"); //$NON-NLS-1$
			} else if (isHtml && isPhpState) {
				// php -> html
				result.append("?>\n"); //$NON-NLS-1$
				statement.accept(this);
				result.append("\n"); //$NON-NLS-1$
				isPhpState = false;
			} else {
				// html first
				statement.accept(this);
				result.append("\n"); //$NON-NLS-1$
			}
		}

		if (isPhpState) {
			result.append("?>\n"); //$NON-NLS-1$
		}

		for (Comment comment : program.comments()) {
			comment.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(Quote quote) {
		switch (quote.getQuoteType()) {
		case Quote.QT_QUOTE:
			result.append("\""); //$NON-NLS-1$
			acceptQuoteExpression(quote.expressions());
			result.append("\""); //$NON-NLS-1$
			break;
		case Quote.QT_SINGLE:
			result.append("\'"); //$NON-NLS-1$
			acceptQuoteExpression(quote.expressions());
			result.append("\'"); //$NON-NLS-1$
			break;
		case Quote.QT_HEREDOC:
			result.append("<<<Heredoc\n"); //$NON-NLS-1$
			acceptQuoteExpression(quote.expressions());
			result.append("\nHeredoc"); //$NON-NLS-1$
			break;
		case Quote.QT_NOWDOC:
			result.append("<<<'Nowdoc'\n"); //$NON-NLS-1$
			acceptQuoteExpression(quote.expressions());
			result.append("\nNowdoc"); //$NON-NLS-1$
			break;
		}
		return false;
	}

	@Override
	public boolean visit(Reference reference) {
		result.append("&"); //$NON-NLS-1$
		if (reference.getExpression() != null) {
			reference.getExpression().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ReflectionVariable reflectionVariable) {
		result.append("$"); //$NON-NLS-1$
		if (reflectionVariable.getName() != null) {
			reflectionVariable.getName().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ReturnStatement returnStatement) {
		result.append("return "); //$NON-NLS-1$
		if (returnStatement.getExpression() != null) {
			returnStatement.getExpression().accept(this);
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(YieldExpression returnStatement) {
		result.append("yield "); //$NON-NLS-1$
		if (returnStatement.getExpression() != null) {
			returnStatement.getExpression().accept(this);
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(Scalar scalar) {
		if (scalar.getScalarType() == Scalar.TYPE_UNKNOWN) {
			// cant flatten, needs source
		} else {
			result.append(scalar.getStringValue());
		}
		return false;
	}

	@Override
	public boolean visit(StaticConstantAccess staticFieldAccess) {
		if (staticFieldAccess.getClassName() != null) {
			staticFieldAccess.getClassName().accept(this);
		}
		result.append("::"); //$NON-NLS-1$
		if (staticFieldAccess.getConstant() != null) {
			staticFieldAccess.getConstant().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(StaticFieldAccess staticFieldAccess) {
		if (staticFieldAccess.getClassName() != null) {
			staticFieldAccess.getClassName().accept(this);
		}
		result.append("::"); //$NON-NLS-1$
		if (staticFieldAccess.getField() != null) {
			staticFieldAccess.getField().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(StaticMethodInvocation staticMethodInvocation) {
		if (staticMethodInvocation.getClassName() != null) {
			staticMethodInvocation.getClassName().accept(this);
		}
		result.append("::"); //$NON-NLS-1$
		if (staticMethodInvocation.getMethod() != null) {
			staticMethodInvocation.getMethod().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(StaticStatement staticStatement) {
		result.append("static "); //$NON-NLS-1$
		Iterator<Expression> expressions = staticStatement.expressions().iterator();
		if (expressions.hasNext()) {
			expressions.next().accept(this);
			while (expressions.hasNext()) {
				result.append(", "); //$NON-NLS-1$
				expressions.next().accept(this);
			}
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(SwitchCase switchCase) {
		if (switchCase.isDefault()) {
			result.append("default:\n"); //$NON-NLS-1$
		} else {
			result.append("case "); //$NON-NLS-1$
			if (switchCase.getValue() != null) {
				switchCase.getValue().accept(this);
				result.append(":\n"); //$NON-NLS-1$
			}
		}
		for (Statement act : switchCase.actions()) {
			act.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(SwitchStatement switchStatement) {
		result.append("switch ("); //$NON-NLS-1$

		Expression express = switchStatement.getExpression();
		if (express != null) {
			express.accept(this);
		}
		result.append(")"); //$NON-NLS-1$
		Block statment = switchStatement.getBody();
		if (statment != null) {
			statment.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ThrowStatement throwStatement) {
		result.append("throw "); //$NON-NLS-1$
		if (throwStatement.getExpression() != null) {
			throwStatement.getExpression().accept(this);
		}
		result.append(";\n"); //$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(TryStatement tryStatement) {
		result.append("try "); //$NON-NLS-1$

		Block body = tryStatement.getBody();
		if (body != null) {
			body.accept(this);
		}
		List<CatchClause> catchClauses = tryStatement.catchClauses();
		for (int i = 0; i < catchClauses.size(); i++) {
			catchClauses.get(i).accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(UnaryOperation unaryOperation) {
		result.append(UnaryOperation.getOperator(unaryOperation.getOperator()));
		if (unaryOperation.getExpression() != null) {
			unaryOperation.getExpression().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(Variable variable) {
		if (variable.isDollared()) {
			result.append("$"); //$NON-NLS-1$
		}
		if (variable.getName() != null) {
			variable.getName().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(WhileStatement whileStatement) {
		result.append("while ("); //$NON-NLS-1$
		Expression condition = whileStatement.getCondition();

		if (condition != null) {
			whileStatement.getCondition().accept(this);
		}
		result.append(")\n"); //$NON-NLS-1$
		Statement body = whileStatement.getBody();
		if (body != null) {
			body.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(SingleFieldDeclaration singleFieldDeclaration) {
		if (singleFieldDeclaration.getName() != null) {
			singleFieldDeclaration.getName().accept(this);
		}
		Expression value = singleFieldDeclaration.getValue();
		if (value != null) {
			result.append(" = ");//$NON-NLS-1$
			value.accept(this);
		}
		return false;
	}

	private void acceptQuoteExpression(List<Expression> expressions) {
		for (Expression expr : expressions) {
			expr.accept(this);
		}
	}

}
