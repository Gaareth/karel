package syntax.parser

import freditor.Levenshtein
import syntax.tree.Call
import syntax.tree.Command
import syntax.tree.Program

val BUILTIN_COMMANDS = setOf("moveForward", "turnLeft", "turnAround", "turnRight", "pickBeeper", "dropBeeper")

class Sema(val parser: Parser) {
    private val commands = HashMap<String, Command>()
    private val calls = ArrayList<Call>()
    fun command(name: String): Command? = commands[name]

    operator fun invoke(command: Command): Command {
        if (commands.containsKey(command.identifier.lexeme)) {
            command.identifier.error("duplicate command ${command.identifier.lexeme}")
        }
        if (BUILTIN_COMMANDS.contains(command.identifier.lexeme)) {
            command.identifier.error("cannot redefine builtin command ${command.identifier.lexeme}")
        }
        commands[command.identifier.lexeme] = command
        return command
    }

    operator fun invoke(call: Call): Call {
        if (!BUILTIN_COMMANDS.contains(call.target.lexeme)) {
            calls.add(call)
        }
        return call
    }

    operator fun invoke(program: Program): Program {
        for (call in calls) {
            val command = commands[call.target.lexeme]

            if (command == null) {
                val bestMatches = Levenshtein.bestMatches(call.target.lexeme, commands.keys + BUILTIN_COMMANDS)
                if (bestMatches.size == 1) {
                    val bestMatch = bestMatches.first()
                    val prefix = bestMatch.commonPrefixWith(call.target.lexeme)
                    call.target.error("Did you mean $bestMatch?", prefix.length)
                } else {
                    val commaSeparated = bestMatches.joinToString(", ")
                    call.target.error("Did you mean $commaSeparated?")
                }
            } else {
                if (call.args.size > command.args.size) {
                    call.target.error("${command.identifier.lexeme} only accepts ${command.args.size} arguments. You passed ${call.args.size}")
                }

                if (call.args.size < command.args.size) {
                    call.target.error("${command.identifier.lexeme} needs ${command.args.size} arguments. You passed ${call.args.size}")
                }

                for ((i, arg) in call.args.withIndex()) {
                    arg.assertType(parser, command.args[i].type)
                }
            }
        }
        return program
    }
}
