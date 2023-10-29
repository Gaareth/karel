package syntax.parser

import org.junit.Assert.assertEquals
import org.junit.Test
import syntax.lexer.Lexer
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.*
import syntax.tree.*
import syntax.tree.Number

class ParserTest {
    private var lexer: Lexer = Lexer("")
    private var parser: Parser = Parser(lexer);

    private fun assertAST(expected: Node, actual: Node) {
        assertEquals(expected, actual)
//        when(expected) {
//            is Command -> TODO()
//            is Binary -> {
//                assertTrue(actual is Binary)
//                val actual = actual as Binary;
//                assertTrue(expected.operator.equalsKind(actual.operator))
//                assertAST(expected.lhs, actual.lhs)
//                assertAST(expected.rhs, actual.rhs)
//            }
//            is BeeperAhead -> TODO()
//            is BinaryCondition -> TODO()
//            is Conjunction -> TODO()
//            is Disjunction -> TODO()
//            is False -> TODO()
//            is FrontIsClear -> TODO()
//            is LeftIsClear -> TODO()
//            is Not -> TODO()
//            is NumberCondition -> TODO()
//            is OnBeeper -> TODO()
//            is RightIsClear -> TODO()
//            is True -> TODO()
//            is Number -> {
//                assertTrue(actual is Number)
//                val actual = actual as Number;
//                assertEquals(expected.value, actual.value)
//            }
//            is Unary -> TODO()
//            is Variable -> TODO()
//            is Program -> TODO()
//            is Assign -> TODO()
//            is Block -> TODO()
//            is Call -> TODO()
//            is Declare -> TODO()
//            is IfThenElse -> TODO()
//            is Repeat -> TODO()
//            is While -> TODO()
//            else -> TODO()
//        }
    }

    private fun dummyToken(kind: TokenKind): Token {
        return Token(kind, 0, "dummy")
    }


    @Test
    fun binary() {
        lexer = Lexer("2+2")
        parser = Parser(lexer)
        assertAST(
            Binary(Number(2, dummyToken(NUMBER)), dummyToken(PLUS), Number(2, dummyToken(NUMBER))),
            parser.expression()
        )

        lexer = Lexer("2+2-2")
        parser = Parser(lexer)
        assertAST(
            Binary(
                Binary(Number(2, dummyToken(NUMBER)), dummyToken(PLUS), Number(2, dummyToken(NUMBER))),
                dummyToken(MINUS),
                Number(2, dummyToken(NUMBER))
            ),
            parser.expression()
        )

    }

     @Test
    fun unary() {
        lexer = Lexer("!5")
        parser = Parser(lexer)
        assertAST(
            Not(dummyToken(BANG), Number(5, dummyToken(NUMBER))),
            parser.expression()
        )

        lexer = Lexer("-1+5")
        parser = Parser(lexer)
        assertAST(
            Binary(
                Unary(dummyToken(MINUS), Number(1, dummyToken(NUMBER))),
                dummyToken(PLUS),
                Number(5, dummyToken(NUMBER))
            ),
            parser.expression()
        )

    }

    @Test
    fun precedence() {
        lexer = Lexer("1-8*7")
        parser = Parser(lexer)
        assertAST(
            Binary(
                Number(1, dummyToken(NUMBER)),
                dummyToken(MINUS),
                Binary(Number(8, dummyToken(NUMBER)), dummyToken(STAR), Number(7, dummyToken(NUMBER))),
            ),
            parser.expression()
        )
    }

    @Test
    fun paren() {
        lexer = Lexer("(2)+((2+2+(3+(5))))")
        parser = Parser(lexer)
        parser.expression()
    }

    @Test
    fun ifCond() {
        lexer = Lexer("if (2 == 2 && !onBeeper()) { moveForward(); }")
        parser = Parser(lexer)
        parser.statement()

        lexer = Lexer("if (!onBeeper()) { moveForward(); }")
        parser = Parser(lexer)
        parser.statement()

        lexer = Lexer("if (2 + 2 == 4) { moveForward(); }")
        parser = Parser(lexer)
        parser.statement()
    }

    @Test
    fun whileCond() {
        lexer = Lexer("while (onBeeper() || 2 + 2 == 1) { moveForward(); }")
        parser = Parser(lexer)
        parser.statement()

        lexer = Lexer("while (!onBeeper()) { moveForward(); }")
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
        lexer = Lexer(
            "" +
                    "let a = true; " +
                    "let b = true; " +
                    "let c = 2 + 2; " +
                    "let d = 2 + a; " +
                    "let e = 7;" +
                    "let f = 0 + 100 - 2 * 30 + 2;"
        )
        parser = Parser(lexer)
        parser.statement()
        parser.statement()
        parser.statement()
        parser.statement()
        parser.statement()
        parser.statement()
    }

    @Test
    fun varAssign() {
        lexer = Lexer(
            "" +
                    "let a = true; " +
                    "a = false;"
        )
        parser = Parser(lexer)
        parser.statement()
        parser.statement()
    }

    @Test
    fun varNestedUse() {
        lexer = Lexer(
            "let a = 1; " +
                    "while (true) {" +
                        " a = a + 1;" +
                        " if (a == 1) {" +
                            "   moveForward(); " +
                        "}" +
                    "}"
        )
        parser = Parser(lexer)
        parser.statement()
        parser.statement()
    }
}