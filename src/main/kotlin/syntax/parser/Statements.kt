package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.program(): Program {
    if (!match(VOID, BOOL, NUM)) token.error("expected void, bool, num")

    return sema(Program(list1Until(END_OF_INPUT, ::command)))
}

fun Parser.command(): Command = when (current) {
    VOID, NUM, BOOL -> {
        val type = accept()
        val id = expect(IDENTIFIER)
        val args = parenthesized { listArgs(::formalArg) }
        currentFunctionReturnType = type.toType()
        sema(
            Command(
                type,
                id,
                args,
                block(args.associateBy({ it.name.lexeme }, { it.type }))
            )
        )
    }

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

//fun Parser.call(): Call {
//
//}

fun Parser.block(args: Map<String, Type>? = null): Block {
    val prevEnvironment = environment;
    environment = Environment(prevEnvironment, this)
    // insert local variables of block (currently only functions)
    if (args != null) {
        for ((name, type) in args.iterator()) {
            environment.define(name, type)
        }
    }
    val block = Block(expect(OPENING_BRACE), list0Until(CLOSING_BRACE, ::statement), accept())
    environment = prevEnvironment
    return block
}

fun Parser.formalArg(): FormalArg {
    val arg = expect(IDENTIFIER)
    expect(COLON)
    val type = expect(BOOL, NUM, VOID)
    return FormalArg(arg, type.toType())
}

fun Parser.actualArg(): ActualArg {
    val expr = expression().assertType(this, Type.Bool, Type.Number)
    return syntax.tree.ActualArg(expr)
}

fun Parser.statement(): Statement = when (current) {
    IDENTIFIER -> {
        val id = accept();
        val curr = token;
        if (curr.kind == ASSIGN) {
            next();
            val value = expression();
            val newType = value.type(this)
            val storedType = environment.get(id.lexeme)
            val ok = environment.assign(id.lexeme, value.type(this))

            if (ok == null) {
                curr.error("Can't assign to undeclared variable '${id.lexeme}'")
            } else if (storedType != newType) {
                curr.error("${id.lexeme} is of type $storedType. Don't assign ${value.token().lexeme} (a $newType) to it.")
            }

            Assign(id, value).semicolon()
        } else {
            ExpressionStmt(sema(Call(id, parenthesized { listArgs(::expression) }).semicolon()))
        }
    }

    LET -> {
        val let = accept();
        val id = expect(IDENTIFIER);
        expect(ASSIGN);
        val rhs = expression()

        val declaration = Declare(let, id, rhs).semicolon();
        environment.define(id.lexeme, rhs)
        declaration
    }

    RETURN -> {
//        if (currentFunctionReturnType == null) {
//            token.error("Only use return in")
//        }
        Return(accept(),
            expression()
                .assertType(this, currentFunctionReturnType!!,
                    msg = "Wrong return type. Expected %s to be a %s. Is: %s").semicolon())
    }

    REPEAT -> Repeat(accept(), parenthesized(::repeatExpression), block())

    WHILE -> While(accept(), parenthesized(::condition), block())

    IF -> IfThenElse(accept(), parenthesized(::condition), block(), optional(ELSE) {
        when (current) {
            OPENING_BRACE -> block()

            IF -> statement()

            else -> token.error("else must be followed by { or if")
        }
    })

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
