package syntax.parser

import freditor.Levenshtein
import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.condition(): Expression {
    return disjunction().assertType(environment, Type.Bool)
}

fun Parser.disjunction(): Expression {
    val left = conjunction()
    return if (current != BAR_BAR) {
        left
    } else {
        left.assertOperandsType(environment, token, Type.Bool)
        Disjunction(left, accept(), disjunction())
    }
}

fun Parser.conjunction(): Expression {
    val left = equality()
    return if (current != AMPERSAND_AMPERSAND) {
        left
    } else {
        left.assertOperandsType(environment, token, Type.Bool)
        Conjunction(left, accept(), conjunction())
    }
}


val PREDICATES = listOf("false", "true", "onBeeper", "beeperAhead", "leftIsClear", "frontIsClear", "rightIsClear")

fun Parser.handleWrongConditionCall(): Nothing {
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
