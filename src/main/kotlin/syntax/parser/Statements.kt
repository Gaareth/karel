package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.program(): Program {
    if (current != VOID) token.error("expected void")

    return sema(Program(list1Until(END_OF_INPUT, ::command)))
}

fun Parser.command(): Command = when (current) {
    VOID -> sema(Command(accept(), expect(IDENTIFIER).emptyParens(), block()))

    CLOSING_BRACE -> token.error("too many closing braces")

    REPEAT, WHILE, IF -> token.error("$current belongs inside command.\nDid you close too many braces?")

    IDENTIFIER -> {
        val identifier = accept().emptyParens()
        when (current) {
            SEMICOLON -> identifier.error("Command calls belong inside command.\nDid you close too many braces?")

            else -> identifier.error("expected void")
        }
    }

    else -> token.error("expected void")
}

fun Parser.block(): Block {
    val prevEnvironment = environment;
    environment = Environment(prevEnvironment)
    val block = Block(expect(OPENING_BRACE), list0Until(CLOSING_BRACE, ::statement), accept())
    environment = prevEnvironment
    return block
}

//fun Parser.expression(): Expression {
//    val expr = equality();
//
//}

//fun Parser.equality(): Expression {
//    var expr = comparison();
//    while (current == EQUAL_EQUAL || current == BANG_EQUAL) {
//        val op = accept();
//        val right = comparison();
//        expr = Binary(expr, op, right);
//    }
//    return expr
//}
//
//fun Parser.comparison(): Expression {
//
//}

//fun Parser.primary(): Expression = when (current) {
//    NUMBER -> Literal(token.toInt(2..4095))
//    IDENTIFIER -> when(token.lexeme) {
//        "false" -> False(accept())
//        "true" -> True(accept())
//        else -> illegalStartOf("expression")
//    }
//
//    else -> illegalStartOf("expression")
//}


fun Parser.statement(): Statement = when (current) {
    IDENTIFIER -> {
        val id = accept();
        val curr = token;
        if (curr.kind == ASSIGN) {
            next();
            val value = expect(NUMBER);
            println("ASSSign");

            if (environment.assign(id.lexeme, value) == null) {
                curr.error("Can't assign to undeclared variable '%s'".format(id.lexeme))
            }
            Assign(id, value.toInt(-4095..4095)).semicolon()
        } else {
            sema(Call(id.emptyParens()).semicolon())
        }
    }
    LET -> {
        val let = accept();
        val id = expect(IDENTIFIER);
        expect(ASSIGN);
        val rhs = expect(NUMBER);
        println(id.lexeme)
        println(rhs.lexeme)

        val value = rhs.toInt(-4095..4095);
        val declaration = Declare(let, id, value).semicolon();
        environment.define(id.lexeme, value)
        declaration
    }

//    IDENTIFIER -> sema(Call(accept().emptyParens()).semicolon())

    REPEAT -> Repeat(accept(), parenthesized { expect(NUMBER).toInt(2..4095) }, block())

    WHILE -> While(accept(), parenthesized(::disjunction), block())

    IF -> IfThenElse(accept(), parenthesized(::disjunction), block(), optional(ELSE) {
        when (current) {
            OPENING_BRACE -> block()

            IF -> statement()

            else -> token.error("else must be followed by { or if")
        }
    })

//    ASSIGN -> {
//        println(token);
//        token.error("AAA");
//    }

    VOID -> {
        val void = accept()
        expect(IDENTIFIER).emptyParens()
        when (current) {
            OPENING_BRACE -> void.error("Command definitions cannot be nested.\nDid you forget a } somewhere?")

            else -> void.error("Command calls have no void before the command name")
        }
    }

    END_OF_INPUT -> token.error("End of file encountered in an unclosed block.\nDid you forget a } somewhere?")

    else -> illegalStartOf("statement")
}
