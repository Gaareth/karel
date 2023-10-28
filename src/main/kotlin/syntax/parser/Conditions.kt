package syntax.parser

import common.Diagnostic
import freditor.Levenshtein
import syntax.lexer.TokenKind.*
import syntax.tree.*
import vm.MAX_VALUE

fun Parser.condition(): Condition {
    return disjunction()
}

fun Parser.disjunction(): Condition {
    val left = conjunction()
    return if (current != BAR_BAR) {
        left
    } else {
        Disjunction(left, accept(), disjunction())
    }
}

fun Parser.conjunction(): Condition {
    val left = equalityCondition()
    return if (current != AMPERSAND_AMPERSAND) {
        left
    } else {
        Conjunction(left, accept(), conjunction())
    }
}

fun Parser.equalityCondition(): Condition {
    var expr = comparisonCondition()
    while ((current == EQUAL_EQUAL) || (current == BANG_EQUAL)) {
        val op = accept()
        val right = comparisonCondition()
        expr = BinaryCondition(expr, op, right)
    }

    if (expr is NumberCondition) {
        throw Diagnostic(token.start, "number is not condition")
    }
    return expr
}

fun Parser.comparisonCondition(): Condition {
    var expr = primaryCondition()

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
        val op = accept()
        val right = primaryCondition()
        expr = BinaryCondition(expr, op, right)
    }
    return expr
}


val PREDICATES = listOf("false", "true", "onBeeper", "beeperAhead", "leftIsClear", "frontIsClear", "rightIsClear")

fun Parser.primaryCondition(): Condition = when (current) {
    IDENTIFIER -> when (token.lexeme) {
        "false" -> False(accept())
        "true" -> True(accept())

        "onBeeper" -> OnBeeper(accept().emptyParens())
        "beeperAhead" -> BeeperAhead(accept().emptyParens())
        "leftIsClear" -> LeftIsClear(accept().emptyParens())
        "frontIsClear" -> FrontIsClear(accept().emptyParens())
        "rightIsClear" -> RightIsClear(accept().emptyParens())

        else -> {
            val bestMatches = Levenshtein.bestMatches(token.lexeme, PREDICATES)
            if (bestMatches.size == 1) {
                val bestMatch = bestMatches.first()
                val prefix = bestMatch.commonPrefixWith(token.lexeme)
                token.error("Did you mean $bestMatch?", prefix.length)
            } else {
                val commaSeparated = bestMatches.joinToString(", ")
                token.error("Did you mean $commaSeparated?")
            }
        }
    }

    BANG -> Not(accept(), primaryCondition())

    OPENING_PAREN -> parenthesized(::disjunction)
    NUMBER -> {
//        if ((lookahead.kind == CLOSING_PAREN) && (previous?.kind == OPENING_PAREN)) {
//            throw Diagnostic(token.start, "number is not condition")
//        }

        val token = accept()
        NumberCondition(token.toInt(0..MAX_VALUE), token)
    }

    else -> illegalStartOf("condition")
}
