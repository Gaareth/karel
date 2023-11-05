package syntax.parser

import common.Diagnostic
import syntax.lexer.Lexer
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.*

class Parser(private val lexer: Lexer) {
    private var previousEnd: Int = 0

    var previous: Token? = null;

    var token: Token = lexer.nextToken()
        private set

    var current: TokenKind = token.kind
        private set

    var lookahead: Token = lexer.nextToken()
        private set

    fun next(): TokenKind {
        previous = token

        previousEnd = token.end
        token = lookahead
        current = token.kind
        lookahead = lexer.nextToken()
        return current
    }

    /** accepts current token, and advances **/
    fun accept(): Token {
        val result = token
        next()
        return result
    }

    fun expect(vararg expected: TokenKind): Token {
        if (!match(*expected))
            throw Diagnostic(previousEnd, "missing ${expected.joinToString(separator = ", or ")}")
        return accept()
    }

    fun match(vararg tokens: TokenKind): Boolean {
        for (token in tokens) {
            if (token == current) {
                return true
            }
        }
        return false
    }

    fun <T> T.emptyParens(): T {
        expect(OPENING_PAREN)
        expect(CLOSING_PAREN)
        return this
    }

    fun <T> T.semicolon(): T {
        expect(SEMICOLON)
        return this
    }

    fun illegalStartOf(rule: String): Nothing {
        token.error("illegal start of $rule")
    }

    inline fun <T> list1While(proceed: () -> Boolean, parse: () -> T): List<T> {
        val list = mutableListOf(parse())
        while (proceed()) {
            list.add(parse())
        }
        return list
    }

    inline fun <T> list0While(proceed: () -> Boolean, parse: () -> T): List<T> {
        return if (!proceed()) {
            emptyList()
        } else {
            list1While(proceed, parse)
        }
    }

    inline fun <T> list1Until(terminator: TokenKind, parse: () -> T): List<T> {
        return list1While({ current != terminator }, parse)
    }

    inline fun <T> list0Until(terminator: TokenKind, parse: () -> T): List<T> {
        return list0While({ current != terminator }, parse)
    }

    inline fun <T> listArgs(parse: () -> T): List<T> {
        val args = mutableListOf<T>()
        while (current != CLOSING_PAREN) {
            args.add(parse())
            if (current == COMMA) {
                accept()
            }
        }
        return args
    }

    inline fun <T> parenthesized(parse: () -> T): T {
        expect(OPENING_PAREN)
        val result = parse()
        expect(CLOSING_PAREN)
        return result
    }

    inline fun <T> optional(indicator: TokenKind, parse: () -> T): T? {
        return if (current != indicator) {
            null
        } else {
            next()
            parse()
        }
    }


    val sema = Sema(this)
    var environment = Environment(null, this)
    var currentFunctionReturnType: Type? = null
}
