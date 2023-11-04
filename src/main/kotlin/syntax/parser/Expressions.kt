package syntax.parser

import common.Diagnostic
import syntax.lexer.TokenKind.*
import syntax.tree.*
import syntax.tree.Number
import vm.MAX_VALUE

// precedence (reverse)::: expression -> equality -> comparison --> term -> factor -> unary -> primary

fun Parser.repeatExpression(): Expression {
    val expr = expression().assertType(environment, Type.Number)
    if (expr is Number) {
        val number: Number = expr;
        if (number.value.toInt() < 2) {
            throw Diagnostic(expr.token.start, "Repeating 1 or less times makes no sense.")
        }

        if (number.value.toInt() >= MAX_VALUE) {
            throw Diagnostic(expr.token.start, "$MAX_VALUE out of range")
        }
    }
    return expr
}


fun Parser.expression(): Expression {
    return disjunction()
}


fun Parser.equality(): Expression {
    var expr = comparison()
    while (match(EQUAL_EQUAL, BANG_EQUAL)) {
        val op = accept()
        expr.assertOperandsType(environment, op, Type.Number, Type.Bool)
        val right = comparison().assertOperandsType(environment, op, Type.Number, Type.Bool)
        expr = Binary(expr, op, right).assertResultType(environment, Type.Bool)
    }
    return expr
}

fun Parser.comparison(): Expression {
    var expr = term()
    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
        val op = accept()
        expr.assertOperandsType(environment, op, Type.Number)
        val right = term().assertOperandsType(environment, op, Type.Number)
        expr = Binary(expr, op, right).assertResultType(environment, Type.Bool)
    }
    return expr
}

fun Parser.term(): Expression {
    var expr = factor()
    while (match(PLUS, MINUS)) {
        val op = accept()
        expr.assertOperandsType(environment, op, Type.Number)
        val right = factor().assertOperandsType(environment, op, Type.Number)
        expr = Binary(expr, op, right).assertResultType(environment, Type.Number)
    }
    return expr
}


fun Parser.factor(): Expression {
    var expr = unary()
    while (match(STAR, SLASH)) {
        val op = accept()
        expr.assertOperandsType(environment, op, Type.Number)

        val right = unary().assertOperandsType(environment, op, Type.Number)
        expr = Binary(expr, op, right).assertResultType(environment, Type.Number)
    }
    return expr
}

fun Parser.unary(): Expression {
    while (match(MINUS)) {
        val op = accept()
        val right = unary()
        val operandType = right.type(environment)
        if (operandType != Type.Number) {
            op.error("You can't negate a $operandType. Right side of ${op.lexeme} has to be a Number")
        }
        return Unary(op, right)
    }
    return primary()
}

fun Parser.primary(): Expression = when (current) {
    BANG -> Not(accept(), primary())

    NUMBER -> {
        val token = accept()
        Number(token.toInt(0..MAX_VALUE), token)
    }

    IDENTIFIER ->
        when (token.lexeme) {
            "false" -> False(accept())
            "true" -> True(accept())

            "onBeeper" -> OnBeeper(accept().emptyParens())
            "beeperAhead" -> BeeperAhead(accept().emptyParens())
            "leftIsClear" -> LeftIsClear(accept().emptyParens())
            "frontIsClear" -> FrontIsClear(accept().emptyParens())
            "rightIsClear" -> RightIsClear(accept().emptyParens())

            else -> when {
                lookahead.kind == OPENING_PAREN -> {
                    handleWrongConditionCall()
                }

                else -> {
                    val token = accept();
                    when (val storedExpr = environment.get(token.lexeme)) {
                        // does not work like this. E.g. var gets altered later on and is used in a loop
//                        // inline optimization?
//                        // value is known at compile time?
//                        is Number, is False, is True -> {
//                            storedExpr as Expression
//                        }

                        null -> {
                            throw Diagnostic(
                                token.start,
                                "${token.lexeme} is a undeclared variable. Use 'let ${token.lexeme} = ???'!"
                            )
                        }

                        else -> {
                            Variable(token)
                        }
                    }
                }
            }
        }


    OPENING_PAREN -> {
        accept()
        val expr = expression()
        expect(CLOSING_PAREN)
        expr
    }

    else -> {
        illegalStartOf("expression: ${token.lexeme}")
    }
}