package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*
import vm.MAX_VALUE

// precedence (reverse)::: expression -> equality -> comparison --> term -> factor -> unary -> primary

fun Parser.expression(): Expression {
    return equality()
}

fun Parser.equality(): Expression {
    var expr = comparison();
    while ((current == EQUAL_EQUAL) || (current == BANG_EQUAL)) {
        val op = accept();
        val right = comparison();
        expr = Binary(expr, op, right);
    }
    return expr
}

fun Parser.comparison(): Expression {
    var expr = term();
    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
        val op = accept();
        val right = term();
        expr = Binary(expr, op, right);
    }
    return expr;
}

fun Parser.term(): Expression {
    var expr = factor();
    while (match(PLUS, MINUS)) {
        val op = accept();
        val right = factor();
        expr = Binary(expr, op, right);
    }
    return expr;
}


fun Parser.factor(): Expression {
    var expr = unary();
    while (match(STAR, SLASH)) {
        val op = accept();
        val right = unary();
        expr = Binary(expr, op, right);
    }
    return expr;
}

fun Parser.unary(): Expression {
    while (match(BANG, MINUS)) {
        val op = accept();
        val right = unary();
        return Unary(op, right);
    }
    return primary();
}


fun Parser.primary(): Expression = when (current) {
    NUMBER -> Literal(token.toInt(0..MAX_VALUE))
//    IDENTIFIER -> when(token.lexeme) {
//        "false" -> False(accept())
//        "true" -> True(accept())
//        else -> illegalStartOf("expression")
//    }
    OPENING_PAREN -> {
        val expr = expression();
        expect(CLOSING_PAREN)
        expr
    }

    else -> illegalStartOf("expression")
}