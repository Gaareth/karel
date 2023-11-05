package syntax.parser

import syntax.tree.Expression

class Environment(private val enclosing: Environment? = null, private val parser: Parser) {
    private val values = HashMap<String, Type?>()
//    private val enclosing: Environment? = null

    fun define(name: String, value: Expression) {
        values[name] = value.type(parser)
    }

    fun define(name: String, type: Type) {
        values[name] = type
    }

    fun assign(name: String, value: Type?): Unit? {
        if (values.contains(name)) {
            values[name] = value
            return Unit // OK
        }
        return enclosing?.assign(name, value)
    }

    fun get(name: String): Type? {
        if (values.contains(name)) {
            return values[name]!!
        }
        return enclosing?.get(name)
    }
}