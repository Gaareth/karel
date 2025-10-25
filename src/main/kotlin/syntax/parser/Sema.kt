package syntax.parser

import freditor.Levenshtein
import syntax.lexer.TokenKind
import syntax.tree.*

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
        // type check the function call
        val command = commands[call.target.lexeme]
        if (command != null) {
            for ((i, arg) in call.args.withIndex()) {
                if (command.args.size > i) {
                    arg.assertType(parser, command.args[i].type)
                }
            }
        } else {
            // should be handled by the sema invoke of Program at the end
        }

        return call
    }

    private fun typeCheckCalls() {
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

                // if type checking here, the environments will not match anymore. Need to type check at the actual call
            }
        }
    }

    private fun returnInIfAndElse(statement: IfThenElse): Boolean {
        val foundInThen = findMissingReturns(statement.th3n);
        val foundInElse = when (statement.e1se) {
            is Block -> findMissingReturns(statement.e1se);
            is IfThenElse -> returnInIfAndElse(statement.e1se)
            null -> false
            else -> error("Impossible")
        }

        return foundInThen && foundInElse
    }

    private fun findMissingReturns(block: Block): Boolean {
        for (statement in block.statements) {
            when (statement) {
                is Block -> {
                    return findMissingReturns(statement)
                }

                is IfThenElse -> {
                    if (returnInIfAndElse(statement)) {
                        return true
                    }
                }

                is Repeat -> {
                    // TODO: if is not zero and constant and includes return then return true
                }

                is Return -> {
                    return true;
                }

                is While -> {
                    // TODO: maybe for trivial cases, return true
                }

                else -> continue
            }
        }

        return false
    }

    operator fun invoke(program: Program): Program {
        typeCheckCalls();

        for (command in program.commands) {
            if (command.type.kind == TokenKind.VOID) {
                continue
            }

            val returnFound = findMissingReturns(command.body)
            if (!returnFound) {
                command.body.closingBrace.error("missing return")
            }
        }

        return program
    }
}
