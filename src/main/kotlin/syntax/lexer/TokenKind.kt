package syntax.lexer

import freditor.persistent.StringedValueMap

enum class TokenKind(val lexeme: String) {
    // keywords
    VOID("void"),
    REPEAT("repeat"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    LET("let"),


    OPENING_PAREN("("),
    CLOSING_PAREN(")"),
    OPENING_BRACE("{"),
    CLOSING_BRACE("}"),
    SEMICOLON(";"),
    BANG("!"),
    AMPERSAND_AMPERSAND("&&"),
    BAR_BAR("||"),

    ASSIGN("="),
    EQUAL_EQUAL("=="),
    BANG_EQUAL("!="),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),

    GREATER(">"),
    LESS("<"),

    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),

    NUMBER("NUMBER"),
    IDENTIFIER("IDENTIFIER"),
    END_OF_INPUT("END OF INPUT");

    override fun toString(): String = lexeme

    val isKeyword: Boolean
        get() = lexeme.first().isLowerCase()
}

val keywords: StringedValueMap<TokenKind> = TokenKind.values().filter(TokenKind::isKeyword)
    .fold(StringedValueMap.empty(), StringedValueMap<TokenKind>::put)
