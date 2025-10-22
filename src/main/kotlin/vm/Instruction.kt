package vm

import freditor.persistent.ChampMap

const val categoryMask = 0xf000 // check if bytecode has additional info
const val targetMask = 0x0fff // extracts additional info from bytecode

data class Instruction(val bytecode: Int, val position: Int) {

    val category: Int
        get() = bytecode.and(categoryMask)
    // float support?: bytecode.toBits().and(0xf000.toFloat().toBits())

    val target: Int
        get() = bytecode.and(targetMask)

    private val compiledFromSource: Boolean
        get() = position > 0

    fun withTarget(newTarget: Int): Instruction {
        return copy(bytecode = category.or(newTarget))
    }

    fun mapTarget(f: (Int) -> Int): Instruction {
        return withTarget(f(target))
    }

    fun shouldPause(): Boolean {
        return when (bytecode) {
            RETURN -> compiledFromSource

            MOVE_FORWARD, TURN_LEFT, TURN_AROUND, TURN_RIGHT, PICK_BEEPER, DROP_BEEPER -> true

            ON_BEEPER, BEEPER_AHEAD, LEFT_IS_CLEAR, FRONT_IS_CLEAR, RIGHT_IS_CLEAR, NOT, AND, OR, XOR -> false

            else -> compiledFromSource && (category != JUMP)
        }
    }

    fun mnemonic(): String {
        return when (bytecode) {
            RETURN -> "RET"

            MOVE_FORWARD -> "MOVE"
            TURN_LEFT -> "TRNL"
            TURN_AROUND -> "TRNA"
            TURN_RIGHT -> "TRNR"
            PICK_BEEPER -> "PICK"
            DROP_BEEPER -> "DROP"

            ON_BEEPER -> "BEEP"
            BEEPER_AHEAD -> "HEAD"
            LEFT_IS_CLEAR -> "LCLR"
            FRONT_IS_CLEAR -> "FCLR"
            RIGHT_IS_CLEAR -> "RCLR"

            NOT -> "NOT"
            AND -> "AND"
            OR -> "OR"
            XOR -> "XOR"

            ADD -> "ADD"
            SUB -> "SUB"
            MUL -> "MUL"
            DIV -> "DIV"

            NEG -> "NEG"

            EQ -> "EQ"
            NEQ -> "NEQ"
            GT -> "GT"
            GTE -> "GTE"
            LT -> "LT"
            LTE -> "LTE"

            FALSE -> "FALSE"
            TRUE -> "TRUE"
            ARGS_START -> "ARGS_START"
            ARGS_END -> "ARGS_END"

            else -> when (category) {
                PUSH -> "PUSH %03x".format(target)

                LOOP -> "LOOP %03x".format(target)
                CALL -> "CALL %03x".format(target)

                JUMP -> "JUMP %03x".format(target)
                ELSE -> "ELSE %03x".format(target)
                THEN -> "THEN %03x".format(target)

                LOAD -> "LOAD %03x".format(target)
                STORE -> "STORE %03x".format(target)

                else -> throw IllegalBytecode(bytecode)
            }
        }
    }
}

const val RETURN = 0x0000

const val MOVE_FORWARD = 0x0001
const val TURN_LEFT = 0x0002
const val TURN_AROUND = 0x0003
const val TURN_RIGHT = 0x0004
const val PICK_BEEPER = 0x0005
const val DROP_BEEPER = 0x0006

const val ON_BEEPER = 0x0007
const val BEEPER_AHEAD = 0x0008
const val LEFT_IS_CLEAR = 0x0009
const val FRONT_IS_CLEAR = 0x000a
const val RIGHT_IS_CLEAR = 0x000b

const val NOT = 0x000c
const val AND = 0x000d
const val OR = 0x000e
const val XOR = 0x000f

const val NORM = 0x0000

const val ARGS_START = 0x0010 // marks the next pushes as parameter args
const val ARGS_END = 0x0011 // marks the previous pushes as parameter args

// >= 0x1000? -> category
const val PUSH = 0x8000
const val FALSE = PUSH - 1
const val TRUE = PUSH - 2

const val LOOP = 0x9000
const val CALL = 0xa000

const val JUMP = 0xb000
const val ELSE = 0xc000
const val THEN = 0xd000

const val STORE = 0xe000
const val LOAD = 0xf000

const val ADD = 0x0f00
const val SUB = 0x0f01
const val MUL = 0x0f02
const val DIV = 0x0f03

const val EQ = 0x0f04
const val NEQ = 0x0f05
const val GT = 0x0f06
const val GTE = 0x0f07
const val LT = 0x0f08
const val LTE = 0x0f09

const val NEG = 0x0f0a


val builtinCommands: ChampMap<String, Int> = ChampMap.of(
    "moveForward", MOVE_FORWARD,
    "turnLeft", TURN_LEFT,
    "turnAround", TURN_AROUND,
    "turnRight", TURN_RIGHT,
    "pickBeeper", PICK_BEEPER,
    "dropBeeper", DROP_BEEPER,
)

private val basicGoalInstructions = Array(XOR + 1) { Instruction(it, 0) }

fun createInstructionBuffer(): MutableList<Instruction> {
    return MutableList(ENTRY_POINT) { basicGoalInstructions[RETURN] }
}

fun goalInstruction(bytecode: Int): Instruction {
    return if (bytecode <= XOR) {
        basicGoalInstructions[bytecode]
    } else {
        Instruction(bytecode, 0)
    }
}
