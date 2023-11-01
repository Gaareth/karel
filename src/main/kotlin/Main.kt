import freditor.persistent.ChampMap
import logic.Problem
import vm.*

fun main() {
    // SwingConfig.nimbusWithDefaultFont(SwingConfig.SANS_SERIF_PLAIN_16)
    // EventQueue.invokeLater(::MainHandler)
    println("HEHE");
//    for (pb: Problem in Problem.problems) {
//        println("\n==== ${pb.name} ====")
//        pb.goal.forEach {
//            val instruction = vm.goalInstruction(it.code)
//            println(instruction.mnemonic())
//        };
//        println("==== ${pb.name} ====\n")
//    }

    println(Problem.obtainArtifact.decompile());
    Problem.obtainArtifact.goal.forEach {
        val instruction = vm.goalInstruction(it.code)
        println(instruction.mnemonic())
    };
}

val builtinCommands: ChampMap<Int, String> = ChampMap.of(
    MOVE_FORWARD, "moveForward",
    TURN_LEFT, "turnLeft",
    TURN_AROUND, "turnAround",
    TURN_RIGHT, "turnRight",
    PICK_BEEPER, "pickBeeper",
    DROP_BEEPER, "dropBeeper",
)


fun Problem.decompile(): String {
    val code = StringBuilder().append("void ${this.name}() {\n")
    val functionNames = HashMap<Int, String>()
//    val stack = ArrayDeque<Int>()
//    val blockInstructions = ArrayList<Instruction>()
//    var inBlock = false
    val skip

    for ((lineCounter, ins) in this.goal.map { vm.goalInstruction(it.code) }.withIndex()) {
        val commandName = builtinCommands[ins.bytecode]

        if (commandName != null) {
            code.append("$commandName();\n")
        } else {
            when (ins.bytecode) {
                RETURN -> {
                    code.append("}\n")
                    code.append("void ${functionNames[lineCounter+1]}() {\n")
                }

                else -> when (ins.category) {
                    CALL -> {
                        code.append("fn${ins.target}();\n")
                        functionNames[ins.target - 256] = "fn${ins.target}"
                    }
                }
//                PUSH -> {
//                    stack.add(ins.target)
//                    inBlock = true
//                }
//
//                LOOP -> {
//                    code.append("repeat (${stack.removeFirst()}) {\n")
//                    blockInstructions.forEach({ it.})
//                }
            }
        }
    }

    return code.toString()
}