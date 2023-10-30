package syntax.parser

import common.Diagnostic
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringContains
import org.junit.Assert
import org.junit.Test
import syntax.lexer.Lexer

class TypeCheckTest {

    private fun assertDiagnostic(messageSubstring: String, sourceCode: String) {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val diagnostic = Assert.assertThrows(Diagnostic::class.java) {
            parser.program()
        }
        MatcherAssert.assertThat(diagnostic.message, StringContains.containsString(messageSubstring))
    }

    private fun assertOk(sourceCode: String) {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        parser.program()
    }

    @Test
    fun binaryOperations() {
        assertOk(
            """
        void main() {
            let a = 1 + 3 - 4 * 5;
            let b = true || false && 1 == 2 && 1 < 2 && 2 > 1 && 2 <= 2 && 2 >= 2 && 1 != 2;
            repeat (a) {
                moveForward();
            }

            if (b) {
                moveForward();
            }
          }
        """
        )
    }

    @Test
    fun binaryOperationsSimple() {
        assertDiagnostic(
            "Operands of +",
            """
        void main() {
            let a = 1 + true;       
          }
        """
        )
    }

    @Test
    fun binaryOperationsBoth() {
        assertDiagnostic(
            "both operands",
            """
        void main() {
            let a = 1 == true;       
          }
        """
        )
    }

    @Test
    fun expectedBool() {
        assertDiagnostic(
            "to be a Bool. Is: Number",
            """
        void main() {
             if (2 * 2) {
                moveForward();
            }
          }
        """
        )
    }

    @Test
    fun wrongOperandType() {
        assertDiagnostic(
            "Operands",
            """
        void main() {
             let a = 2 * 7 || ((!false) && true || false);
          }
        """
        )
    }
}