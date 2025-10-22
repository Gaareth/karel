package syntax.tree

import syntax.lexer.Token
import syntax.parser.Type
import kotlin.Number

sealed class Node


data class Program(val commands: List<Command>) : Node()

data class FormalArg(val name: Token, val type: Type) : Node()
data class ActualArg(val expr: Expression) : Node()

data class Command(val type: Token, val identifier: Token, val args: List<FormalArg>, val body: Block) : Node()

data class Block(val openingBrace: Token, val statements: List<Statement>, val closingBrace: Token) : Statement()

sealed class Expression : Node()
data class Binary(val lhs: Expression, val operator: Token, val rhs: Expression) : Expression()
data class Unary(val operator: Token, val operand: Expression) : Expression()

data class Number(val value: Number, val token: Token) : Expression()


//data class Literal(val value: Any, val token: Token) : Expression()
data class Variable(val name: Token) : Expression()


sealed class Statement : Node()

data class ExpressionStmt(val expr: Expression) : Statement()


data class Return(val ret: Token, val expr: Expression) : Statement()

data class Call(val target: Token, val args: List<Expression>) : Expression()
data class Repeat(val repeat: Token, val expr: Expression, val body: Block) : Statement()

// e1se is a Statement? instead of a Block? in order to support else-if (see Parser.statement)
data class IfThenElse(val iF: Token, val condition: Expression, val th3n: Block, val e1se: Statement?) : Statement()

data class While(val whi1e: Token, val condition: Expression, val body: Block) : Statement()

data class Assign(val lhs: Token, val rhs: Expression) : Statement()
data class Declare(val let: Token, val lhs: Token, val rhs: Expression) : Statement()


sealed class Condition : Expression()

//data class BinaryCondition(val lhs: Condition, val operator: Token, val rhs: Condition) : Condition()

data class False(val fa1se: Token) : Condition()

data class True(val tru3: Token) : Condition()

data class OnBeeper(val onBeeper: Token) : Condition()

data class BeeperAhead(val beeperAhead: Token) : Condition()

data class LeftIsClear(val leftIsClear: Token) : Condition()

data class FrontIsClear(val frontIsClear: Token) : Condition()

data class RightIsClear(val rightIsClear: Token) : Condition()

data class Not(val not: Token, val p: Expression) : Condition()

data class Conjunction(val p: Expression, val and: Token, val q: Expression) : Condition()

data class Disjunction(val p: Expression, val or: Token, val q: Expression) : Condition()
