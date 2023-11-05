package syntax.parser

import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.tree.*
import syntax.tree.Number


enum class Type {
    Number,
    Bool,
    Void
}

fun Token.toType(): Type = when (this.kind) {
    TokenKind.NUM -> Type.Number
    TokenKind.BOOL -> Type.Bool
    TokenKind.VOID -> Type.Void
    else -> throw Exception("Illegal Type")
}

fun Expression.assertType(parser: Parser, vararg expected: Type, msg: String? = null): Expression {
    var msg = msg;
    if (msg == null) {
        if (this is Binary) {
            msg = "Expected Result of %s to be a %s. Is: %s"
        } else {
            msg = "Expected %s to be a %s. Is: %s"
        }

    }

    val operandType = this.type(parser)
    if (!expected.contains(operandType)) {
        val exprToken = this.token()
        exprToken.error(msg.format(exprToken.lexeme, expected.joinToString(separator = ", or "), operandType))
    }
    return this
}

fun Expression.assertResultType(parser: Parser, vararg expected: Type): Expression {
    return this.assertType(parser, *expected, msg = "Result of %s has to be a %s")
}

fun Expression.assertOperandsType(parser: Parser, op: Token, vararg expected: Type): Expression {
    return this.assertType(parser, *expected, msg = "Operands of $op (%s) have to be a %s")
}

//fun Expression.assertType(env: Environment, sema: Sema, vararg expected: Type, msg: String? = null): Expression {
//    var msg = msg;
//    if (msg == null) {
//        if (this is Binary) {
//            msg = "Expected Result of %s to be a %s. Is: %s"
//        } else {
//            msg = "Expected %s to be a %s. Is: %s"
//        }
//
//    }
//
//    val operandType = this.type(env, sema)
//    if (!expected.contains(operandType)) {
//        val exprToken = this.token()
//        exprToken.error(msg.format(exprToken.lexeme, expected.joinToString(separator = ", or "), operandType))
//    }
//    return this
//}
//
//fun Expression.assertResultType(env: Environment, sema: Sema, vararg expected: Type): Expression {
//    return this.assertType(env, sema, *expected, msg = "Result of %s has to be a %s")
//}
//
//fun Expression.assertOperandsType(env: Environment, sema: Sema, op: Token, vararg expected: Type): Expression {
//    return this.assertType(env, sema, *expected, msg = "Operands of $op (%s) have to be a %s")
//}


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
        is Call -> this.target
    }
}

fun Expression.type(parser: Parser): Type? {
    return when (val expr = this) {
        is Binary -> {
            val leftType = expr.lhs.type(parser)
            val rightType = expr.rhs.type(parser)

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
        is Variable -> parser.environment.get(expr.name.lexeme)!!
        is Call -> {
            if (PREDICATES.contains(expr.target.lexeme)) {
                Type.Bool
            } else if (BUILTIN_COMMANDS.contains(expr.target.lexeme)) {
                Type.Void
            } else {
                val command = parser.sema.command(expr.target.lexeme)
                    ?: expr.target.error("Calling commands (as expression) before declaring them, currently not supported")
                command.type.toType()
            }
        }
    }
}
