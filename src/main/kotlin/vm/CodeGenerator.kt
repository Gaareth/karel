package vm

import common.Diagnostic
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.parser.Sema
import syntax.tree.*
import syntax.tree.Number

typealias CommandNameId = Int
typealias Address = Int

class CodeGenerator(private val sema: Sema) {

    private val program: MutableList<Instruction> = createInstructionBuffer()
    private var variableIds = HashMap<String, Int>()
    private var currentCommandName: String? = null

    private val pc: Int
        get() = program.size

    private val lastInstruction: Instruction
        get() = program.last()

    private fun removeLastInstruction() {
        program.removeAt(program.lastIndex)
    }

    private fun generateInstruction(bytecode: Int, token: Token) {
        program.add(Instruction(bytecode, token.start))
    }

    private val id = IdentityGenerator()

    // Forward calls cannot know their target address during code generation.
    // For simplicity, ALL call targets are therefore initially encoded as command name ids.
    // In a subsequent phase, the command name ids are then translated into addresses.
    private val addressOfCommandNameId = HashMap<CommandNameId, Address>()

    private fun translateCallTargets() {
        program.forEachIndexed { index, instruction ->
            if (instruction.category == CALL) {
                program[index] = instruction.mapTarget { addressOfCommandNameId[it]!! }
            }
        }
    }

    fun generate(main: Command): List<Instruction> {
        todo.add(main)
        while (todo.isNotEmpty()) {
            val command = todo.removeFirst()
            if (done.add(command)) {
                command.generate()
            }
        }
        translateCallTargets()
        return program
    }

    private val todo = ArrayDeque<Command>()
    private val done = HashSet<Command>()


    private fun genVarName(name: String): String {
        return name + "_" + currentCommandName
    }

    private fun Command.generate() {
        currentCommandName = this.identifier.lexeme

        addressOfCommandNameId[id(identifier.lexeme)] = pc
        for ((i, arg) in args.withIndex()) {
            variableIds[genVarName(arg.name.lexeme)] = variableIds.size + i + 1 // can't get size while mutating Map
            generateInstruction(STORE + variableIds[genVarName(arg.name.lexeme)]!!, arg.name)
        }
        body.generate()
        generateInstruction(RETURN, body.closingBrace)
        println(this)
    }

    private fun prepareForwardJump(token: Token): Int {
        if (lastInstruction.bytecode != NOT) {
            generateInstruction(ELSE, token)
        } else {
            removeLastInstruction()
            generateInstruction(THEN, token)
        }
        return pc - 1
    }

    private fun patchForwardJumpFrom(origin: Int) {
        program[origin] = program[origin].withTarget(pc)
    }


    private fun Statement.generate() {
        when (this) {
            is Block -> {
                statements.forEach { it.generate() }
            }

            is IfThenElse -> {
                if (e1se == null) {
                    condition.generate()
                    val over = prepareForwardJump(iF)
                    th3n.generate()
                    patchForwardJumpFrom(over)
                } else {
                    condition.generate()
                    val overThen = prepareForwardJump(iF)
                    th3n.generate()
                    val overElse = pc
                    generateInstruction(JUMP, th3n.closingBrace)
                    patchForwardJumpFrom(overThen)
                    e1se.generate()
                    patchForwardJumpFrom(overElse)
                }
            }

            is While -> {
                val back = pc
                condition.generate()
                val over = prepareForwardJump(whi1e)
                body.generate()
                generateInstruction(JUMP + back, body.closingBrace)
                patchForwardJumpFrom(over)
            }

            is Repeat -> {
                println(this)
                println(variableIds)
                expr.generate();
                val back = pc
                body.generate()
                generateInstruction(LOOP + back, body.closingBrace)
            }

            is Assign -> {
                rhs.generate()
                generateInstruction(STORE + variableIds[genVarName(lhs.lexeme)]!!, lhs)
            }

            is Declare -> {
                rhs.generate()
                println(this)
                println(variableIds)
                variableIds[genVarName(lhs.lexeme)] = variableIds.size + 1
                generateInstruction(STORE + variableIds[genVarName(lhs.lexeme)]!!, let)
            }

            is Return -> {
                expr.generate()
                generateInstruction(RETURN, ret)
            }

            is ExpressionStmt -> expr.generate()
        }
    }

    private fun Condition.generate() {
        when (this) {
            is False -> generateInstruction(FALSE, fa1se)
            is True -> generateInstruction(TRUE, tru3)

            is OnBeeper -> generateInstruction(ON_BEEPER, onBeeper)
            is BeeperAhead -> generateInstruction(BEEPER_AHEAD, beeperAhead)
            is LeftIsClear -> generateInstruction(LEFT_IS_CLEAR, leftIsClear)
            is FrontIsClear -> generateInstruction(FRONT_IS_CLEAR, frontIsClear)
            is RightIsClear -> generateInstruction(RIGHT_IS_CLEAR, rightIsClear)

            is Not -> {
                p.generate()
                if (lastInstruction.bytecode != NOT) {
                    generateInstruction(NOT, not)
                } else {
                    removeLastInstruction()
                }
            }

            is Conjunction -> {
                p.generate()
                q.generate()
                generateInstruction(AND, and)
            }

            is Disjunction -> {
                p.generate()
                q.generate()
                generateInstruction(OR, or)
            }
        }
    }

    private fun Expression.generate() {
        when (this) {
            is Variable -> {
                println(this)
                generateInstruction(LOAD + variableIds[genVarName(name.lexeme)]!!, name)
            }

            is Number -> {
                program.add(Instruction(PUSH + value.toInt(), token.start))
            }

            is Binary -> {
                // reverse order for subtracting
                rhs.generate()
                lhs.generate()
                when (operator.kind) {
                    TokenKind.PLUS -> generateInstruction(ADD, operator)
                    TokenKind.MINUS -> generateInstruction(SUB, operator)
                    TokenKind.STAR -> generateInstruction(MUL, operator)
                    TokenKind.SLASH -> TODO("Requires floating point support")
                    TokenKind.EQUAL_EQUAL -> generateInstruction(EQ, operator)
                    TokenKind.BANG_EQUAL -> generateInstruction(NEQ, operator)
                    TokenKind.GREATER_EQUAL -> generateInstruction(GTE, operator)
                    TokenKind.LESS_EQUAL -> generateInstruction(LTE, operator)
                    TokenKind.GREATER -> generateInstruction(GT, operator)
                    TokenKind.LESS -> generateInstruction(LT, operator)

                    // should not happen: TODO: implement some sort of interface, enum, ...?
                    else -> throw Diagnostic(operator.start, "Invalid binary operator")
                }
            }

            is Unary -> {
                when (operator.kind) {
                    TokenKind.MINUS -> {
                        operand.generate()
                        generateInstruction(NEG, operator)
                    }

                    else -> throw Diagnostic(operator.start, "Invalid unary operator")
                }
            }

            is Condition -> {
                this.generate()
            }

            is Call -> {
                val builtin = builtinCommands[target.lexeme]
                if (builtin != null) {
                    generateInstruction(builtin, target)
                } else {
                    if (args.isNotEmpty()) {
                        // push args onto stack
                        generateInstruction(ARGS_START, target)

                        for (arg in args) {
                            arg.generate()
                        }
                        generateInstruction(ARGS_END, target)
                    }
                    generateInstruction(CALL + id(target.lexeme), target)
                    val command = sema.command(target.lexeme)!!
                    if (!done.contains(command)) {
                        todo.add(command)
                    }

                }
            }
        }
    }
}
