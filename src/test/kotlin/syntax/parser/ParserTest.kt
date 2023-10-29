package syntax.parser

import org.junit.Assert.assertEquals
import org.junit.Test
import syntax.lexer.Lexer
import syntax.lexer.TokenKind

class ParserTest {
    private var lexer: Lexer = Lexer("")
    private var parser: Parser = Parser(lexer);

    private fun assertToken(expected: TokenKind) {
        assertEquals(expected, parser.accept())
    }

    @Test
    fun binary() {
        lexer = Lexer("2+2")
        parser = Parser(lexer)
        parser.expression()
        //TODO: test internal structure
    }

    @Test
    fun paren() {
        lexer = Lexer("(2)+((2+2+(3+(5))))")
        parser = Parser(lexer)
        parser.expression()
    }

    @Test
    fun ifcond() {
        lexer = Lexer("if (2 == 2 && !onBeeper()) { moveForward(); }")
        parser = Parser(lexer)
        parser.statement()

        lexer = Lexer("if (!onBeeper()) { moveForward(); }")
        parser = Parser(lexer)
        parser.statement()
    }


    @Test
    fun cond() {
        lexer = Lexer("!onBeeper() || 2 == 1 && (true || !false) && 100 <= 3")
        parser = Parser(lexer)
        parser.condition()
    }

    @Test
    fun varDefine() {
        lexer = Lexer("" +
                "let a = true; "
//                "let b = true; " +
//                "let c = 2 + 2; " +
//                "let d = 2 + a;"
                )
        parser = Parser(lexer)
        parser.statement()
//        parser.statement()
//        parser.statement()
//        parser.statement()
    }
}