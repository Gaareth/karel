package syntax.parser

import common.Diagnostic
import freditor.Levenshtein
import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.condition(): Condition {
    val disj = disjunction()
    if (disj !is Condition) {
        throw Diagnostic(token.start, "Expression is not a condition")
    }
    return disj as Condition
}

fun Parser.disjunction(): Expression {
    val left = conjunction()
    return if (current != BAR_BAR) {
        left
    } else {
        Disjunction(left, accept(), disjunction())
    }
}

fun Parser.conjunction(): Expression {
    val left = equality()
    return if (current != AMPERSAND_AMPERSAND) {
        left
    } else {
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
