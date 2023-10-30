package syntax.parser

import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.tree.*
import syntax.tree.Number


enum class Type {
    Number,
    Bool
}

fun <T : Expression> T.assertType(vararg expected: Type, msg: String? = null): T {
    var msg = msg;
    if (msg == null) {
        if (this is Binary) {
            msg = "Expected Result of %s to be a %s. Is: %s"
        } else {
            msg = "Expected %s to be a %s. Is: %s"
        }

    }

    val operandType = typeOfExpression(this)
    if (!expected.contains(operandType)) {
        val exprToken = tokenOfExpression(this)
        exprToken.error(msg.format(exprToken.lexeme, expected.joinToString(separator = ", or "), operandType))
    }
    return this
}

fun <T : Expression> T.assertResultType(vararg expected: Type): T {
    return this.assertType(*expected, msg = "Result of %s has to be a %s")
}

fun <T : Expression> T.assertOperandsType(op: Token, vararg expected: Type): T {
    return this.assertType(*expected, msg = "Operands of $op (%s) has to be a %s")
}


fun tokenOfExpression(expr: Expression): Token {
    return when (expr) {
        is Binary -> expr.operator
        is BeeperAhead -> expr.beeperAhead
        is Conjunction -> expr.and
        is Disjunction -> expr.or
        is False -> expr.fa1se
        is FrontIsClear -> expr.frontIsClear
        is LeftIsClear -> expr.leftIsClear
        is Not -> expr.not
        is OnBeeper -> expr.onBeeper
        is RightIsClear -> expr.rightIsClear
        is True -> expr.tru3
        is Number -> expr.token
        is Unary -> expr.operator
        is Variable -> expr.name
    }
}

fun typeOfExpression(expr: Expression): Type {
    return when (expr) {
        is Binary -> {
            val leftType = typeOfExpression(expr.lhs);
            val rightType = typeOfExpression(expr.rhs);

            if (leftType != rightType) {
                expr.operator.error("${expr.operator.lexeme} requires both operands to be of the same type")
            }

            return when (expr.operator.kind) {
                // comparison operators
                TokenKind.AMPERSAND_AMPERSAND, TokenKind.BAR_BAR, TokenKind.EQUAL_EQUAL,
                TokenKind.BANG_EQUAL, TokenKind.GREATER_EQUAL, TokenKind.LESS_EQUAL, TokenKind.GREATER, TokenKind.LESS -> Type.Bool

                TokenKind.PLUS, TokenKind.MINUS, TokenKind.STAR, TokenKind.SLASH -> leftType
                else -> throw Exception("Illegal Binary Operator")
            }
        }

        is True, is False, is Not,
        is FrontIsClear, is LeftIsClear, is RightIsClear, is OnBeeper, is BeeperAhead,
        is Disjunction, is Conjunction,
        -> Type.Bool

        is Number, is Unary -> Type.Number
        is Variable -> TODO()
    }
}
