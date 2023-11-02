package syntax.lexer

import org.junit.Assert.assertEquals
import org.junit.Test
import syntax.lexer.TokenKind.*

class LexerTest {
    private var lexer = Lexer("")

    private fun assertToken(expected: TokenKind) {
        val actualToken = lexer.nextToken()
        assertEquals(expected, actualToken.kind)
    }

    private fun assertIdentifier(expected: String) {
        val actualToken = lexer.nextToken()
        assertEquals(IDENTIFIER, actualToken.kind)
        assertEquals(expected, actualToken.lexeme)
    }

    private fun assertNumber(expected: String) {
        val actualToken = lexer.nextToken()
        assertEquals(NUMBER, actualToken.kind)
        assertEquals(expected, actualToken.lexeme)
    }

    @Test
    fun emptyString() {
        lexer = Lexer("")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun singleLineComments() {
        lexer = Lexer(
            """
        // comment #1
        a
        // comment #2
        // comment #3
        b
        c // comment #4
        d// comment #5
        e//
        """
        )

        assertIdentifier("a")
        assertIdentifier("b")
        assertIdentifier("c")
        assertIdentifier("d")
        assertIdentifier("e")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun openSingleLineComment() {
        lexer = Lexer("//")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun multiLineComments() {
        lexer = Lexer(
            """
        /*
        comment #1
        */
        a   /* comment #2 */
        b  /*/ comment #3*/
        c /**/
        d/***/
        e /* / ** / *** /*/
        f
        """
        )

        assertIdentifier("a")
        assertIdentifier("b")
        assertIdentifier("c")
        assertIdentifier("d")
        assertIdentifier("e")
        assertIdentifier("f")
        assertToken(END_OF_INPUT)
    }

    @Test
    fun digits() {
        lexer = Lexer("0 1 2 3 4 5 6 7 8 9")
        assertNumber("0")
        assertNumber("1")
        assertNumber("2")
        assertNumber("3")
        assertNumber("4")
        assertNumber("5")
        assertNumber("6")
        assertNumber("7")
        assertNumber("8")
        assertNumber("9")
    }

    @Test
    fun numbers() {
        lexer = Lexer("10 42 97 1234567890")
        assertNumber("10")
        assertNumber("42")
        assertNumber("97")
        assertNumber("1234567890")
    }

//    @Test
//    fun floats() {
//        lexer = Lexer("10.0 1.234 0.41")
//        assertNumber("10.0")
//        assertNumber("1.234")
//        assertNumber("0.41")
//    }

    @Test
    fun separators() {
        lexer = Lexer("();{}")
        assertToken(OPENING_PAREN)
        assertToken(CLOSING_PAREN)
        assertToken(SEMICOLON)
        assertToken(OPENING_BRACE)
        assertToken(CLOSING_BRACE)
    }

    @Test
    fun operators() {
        lexer = Lexer("!&&||+-*")
        assertToken(BANG)
        assertToken(AMPERSAND_AMPERSAND)
        assertToken(BAR_BAR)
        assertToken(PLUS)
        assertToken(MINUS)
        assertToken(STAR)
    }

    @Test
    fun cmpOperators() {
        lexer = Lexer("!= == <= >= < >")

        assertToken(BANG_EQUAL)
        assertToken(EQUAL_EQUAL)
        assertToken(LESS_EQUAL)
        assertToken(GREATER_EQUAL)
        assertToken(LESS)
        assertToken(GREATER)
    }

    // TODO
//    @Test
//    fun divOrComment() {
//
//    }

    @Test
    fun identifiers() {
        lexer =
            Lexer("a z a0 z9 a_z foo _bar the_quick_brown_fox_jumps_over_the_lazy_dog THE_QUICK_BROWN_FOX_JUMPS_OVER_THE_LAZY_DOG")

        assertIdentifier("a")
        assertIdentifier("z")
        assertIdentifier("a0")
        assertIdentifier("z9")
        assertIdentifier("a_z")
        assertIdentifier("foo")
        assertIdentifier("_bar")
        assertIdentifier("the_quick_brown_fox_jumps_over_the_lazy_dog")
        assertIdentifier("THE_QUICK_BROWN_FOX_JUMPS_OVER_THE_LAZY_DOG")
    }

    @Test
    fun falseTrue() {
        lexer =
            Lexer("false true !false !true")

        assertIdentifier("false")
        assertIdentifier("true")
        assertToken(BANG)
        assertIdentifier("false")
        assertToken(BANG)
        assertIdentifier("true")
    }


    @Test
    fun keywords() {
        lexer = Lexer("if else repeat void while let")

        assertToken(IF)
        assertToken(ELSE)
        assertToken(REPEAT)
        assertToken(VOID)
        assertToken(WHILE)
        assertToken(LET)
    }

    @Test
    fun variables() {
        lexer = Lexer("let a = 5;")

        assertToken(LET)
        assertToken(IDENTIFIER)
        assertToken(ASSIGN)
        assertToken(NUMBER)
        assertToken(SEMICOLON)
    }

    @Test
    fun types() {
        lexer = Lexer("bool num void")

        assertToken(BOOL)
        assertToken(NUM)
        assertToken(VOID)
    }
}
