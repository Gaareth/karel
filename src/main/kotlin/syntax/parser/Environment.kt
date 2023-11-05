package syntax.parser

class Environment<K, V>(private val enclosing: Environment<K, V>? = null) {
    private val values = HashMap<K, V?>()

    fun define(name: K, type: V) {
        values[name] = type
    }

    fun assign(name: K, value: V): Unit? {
        if (values.contains(name)) {
            values[name] = value
            return Unit // OK
        }
        return enclosing?.assign(name, value)
    }

    fun get(name: K): V? {
        if (values.contains(name)) {
            return values[name]
        }
        return enclosing?.get(name)
    }
}