package vm

interface StackValue {
    val color: Int
}

class ReturnAddress(val value: Int) : StackValue {
    override fun toString(): String {
        return "%4x".format(value)
    }

    override val color: Int
        get() = 0x808080
}

data class Num(val value: Int) : StackValue {
    override fun toString(): String {
        return "%4d".format(value)
    }

    operator fun plus(num: Num): Num {
        return Num(this.value + num.value)
    }

    operator fun minus(num: Num): Num {
        return Num(this.value - num.value)
    }

    operator fun times(num: Num): Num {
        return Num(this.value * num.value)
    }

    operator fun times(num: Int): Num {
        return Num(this.value * num)
    }

    operator fun compareTo(num: Num): Int {
        return this.value.compareTo(num.value)
    }

//    operator fun div(num: Num): Num {
//        return Num(this.value / num.value)
//    }

    override val color: Int
        get() = 0x6400c8
}

enum class Bool : StackValue {
    FALSE {
        override fun toString(): String {
            return "false"
        }

        override val color: Int
            get() = 0xff0000
    },
    TRUE {
        override fun toString(): String {
            return "true"
        }

        override val color: Int
            get() = 0x008000
    }
}
