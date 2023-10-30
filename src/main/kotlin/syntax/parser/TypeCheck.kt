package syntax.parser

import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.tree.*
import syntax.tree.Number


enum class Type {
    Number,
    Bool
}


fun Expression.assertType(env: Environment, vararg expected: Type, msg: String? = null): Expression {
    var msg = msg;
    if (msg == null) {
        if (this is Binary) {
            msg = "Expected Result of %s to be a %s. Is: %s"
        } else {
            msg = "Expected %s to be a %s. Is: %s"
        }

    }

    val operandType = this.type(env)
    if (!expected.contains(operandType)) {
        val exprToken = this.token()
        exprToken.error(msg.format(exprToken.lexeme, expected.joinToString(separator = ", or "), operandType))
    }
    return this
}

fun Expression.assertResultType(env: Environment, vararg expected: Type): Expression {
    return this.assertType(env, *expected, msg = "Result of %s has to be a %s")
}

fun Expression.assertOperandsType(env: Environment, op: Token, vararg expected: Type): Expression {
    return this.assertType(env, *expected, msg = "Operands of $op (%s) have to be a %s")
}


fun Expression.token(): Token {
    return when (this) {
        is Binary -> this.operator
        is BeeperAhead -> this.beeperAhead
        is Conjunction -> this.and
        is Disjunction -> this.or
        is False -> this.fa1se
        is FrontIsClear -> this.frontIsClear
        is LeftIsClear -> this.leftIsClear
        is Not -> this.not
        is OnBeeper -> this.onBeeper
        is RightIsClear -> this.rightIsClear
        is True -> this.tru3
        is Number -> this.token
        is Unary -> this.operator
        is Variable -> this.name
    }
}

fun Expression.type(env: Environment): Type {
    return when (val expr = this) {
        is Binary -> {
            val leftType = expr.lhs.type(env)
            val rightType = expr.rhs.type(env)

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
        is Variable -> env.get(expr.name.lexeme)!!
    }
}
