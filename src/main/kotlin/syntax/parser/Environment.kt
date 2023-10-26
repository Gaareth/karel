package syntax.parser

class Environment(private val enclosing: Environment? = null) {
    private val values = HashMap<String, Any>()
//    private val enclosing: Environment? = null

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun assign(name: String, value: Any): Any? {
        if (values.contains(name)) {
            values[name] = value
            return value
        }
        return enclosing?.assign(name, value)
    }

    fun get(name: String): Any? {
        if (values.contains(name)) {
            return values[name]!!
        }
        return enclosing?.get(name)
    }
}