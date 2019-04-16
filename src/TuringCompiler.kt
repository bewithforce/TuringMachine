import java.io.File
import java.util.LinkedList
import java.util.Scanner

class State(val name: String) {
    val rules = HashMap<String, Triple<String, Char, State>>()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

const val regexSymbol = "([\\W]|!|#|@|/\$|/*|-|/+|^|%|&|/?|,|.)+"
const val regexState = "[a-zA-Z]\\w+"

fun main() {
    var temp = State("")
    var indexOfAlphabet = -1
    val alphabet = LinkedList<String>()
    val file = File("main.tmc")
    if (!file.exists()) {
        System.err.println("no such file")
        return
    }
    val sc = Scanner(file)
    val rulesTable = HashMap<State, LinkedList<String>>()

    val emptyState = State("")
    var beginState = emptyState
    var endState = emptyState
    val states: MutableSet<State> = HashSet()

    var tape: MutableList<String> = LinkedList()
    var index = 0


    var i = -0
    while (sc.hasNextLine()) {
        i++
        val line = sc.nextLine().trim()
        if (line.isNotBlank() && !line.startsWith("//")) {
            if (line.startsWith("state")) {
                if (!line.matches("state\\s+$regexState(\\s*begin|\\s*end)?".toRegex())) {
                    System.err.println("Bad state definition")
                    System.err.println("line $i")
                    return
                }
                temp = State(line.split("\\s+".toRegex())[1])
                if (states.contains(temp)) {
                    System.err.println("Redefinition")
                    System.err.println("line $i")
                    return
                }
                if (line.endsWith("begin")) {
                    if (beginState == emptyState) {
                        beginState = temp
                    } else {
                        System.err.println("Begin redefinition")
                        System.err.println("line $i")
                        return
                    }
                }
                if (line.endsWith("end")) {
                    if (endState == emptyState) {
                        endState = temp
                    } else {
                        System.err.println("End redefinition")
                        System.err.println("line $i")
                        return
                    }
                }
                states.add(temp)
                continue
            }
            if (line.startsWith("alphabet")) {
                if (indexOfAlphabet == -1) {
                    indexOfAlphabet = i
                } else {
                    System.err.println("Another alphabet definition")
                    System.err.println("line $i")
                    return
                }
                val alphabetRegex = "alphabet\\s*=\\s*\\[($regexSymbol\\s*,\\s*)*$regexSymbol\\s*]".toRegex()
                if (!line.matches(alphabetRegex)) {
                    System.err.println("Bad alphabet definition")
                    System.err.println("line $i")
                    return
                }
                val protoAlphabet = line.substringAfter("alphabet=[").substringBefore("]").split(", ")
                for (protoChar in protoAlphabet) {
                    if (alphabet.contains(protoChar)) {
                        System.err.println("Bad alphabet definition")
                        System.err.println("line $i")
                        return
                    }
                    alphabet.add(protoChar)
                }
                continue
            }
            if (line.startsWith("index")) {
                if (!line.matches("index\\s*=\\s*\\d+".toRegex())) {
                    System.err.println("Bad index definition")
                    System.err.println("line $i")
                    return
                }
                try {
                    index = line.split("index\\s*=\\s*".toRegex())[1].toInt()
                } catch (e: Exception) {
                    System.err.println(e.localizedMessage)
                    return
                }
                continue
            }
            if (line.startsWith("tape")) {
                if (!line.matches("tape\\s*=\\s*($regexSymbol\\s*,\\s*)*$regexSymbol".toRegex())) {
                    System.err.println("Bad tape definition")
                    System.err.println("line $i")
                    return
                }
                tape = line.split("tape\\s*=\\s*".toRegex())[1].split("\\s*,\\s*".toRegex()).toMutableList()

                continue
            }
            if (temp == endState) {
                System.err.println("End state rule")
                System.err.println("line $i")
                return
            }
            if (!line.matches("$regexSymbol\\s*=>$regexSymbol\\s*[LR]$regexState".toRegex()) &&
                    !line.matches("$regexSymbol\\s*=>$regexState".toRegex())) {
                System.err.println("bad rule definition")
                System.err.println("line $i")
                return
            }
            if (rulesTable[temp] == null) {
                rulesTable[temp] = LinkedList()
            }
            rulesTable[temp]!!.add(line)
        }
    }

    if (beginState == emptyState) {
        System.err.println("No begin state")
        return
    }
    if (endState == emptyState) {
        System.err.println("No end state")
        return
    }
    if (tape.isEmpty()) {
        return
    }

    for (state in states) {
        if (state == endState)
            continue
        val rules = rulesTable[state]
        if (rules == null) {
            System.err.println("no rules for state ${state.name}")
            return
        }
        for (rule in rules) {
            val firstSplit = rule.split("\\s*=>\\s*".toRegex())
            val fromSymbol = firstSplit[0]
            var toSymbol = ""
            var toState = emptyState
            var char = '0'
            if (!alphabet.contains(fromSymbol)) {
                System.err.println("Unknown symbol $fromSymbol in rule $rule")
                return
            }
            if (firstSplit[1] != endState.name) {
                char = firstSplit[1].find { it == 'L' || it == 'R' } ?: '0'
                if (char == '0') {
                    System.err.println("bad rule definition $rule")
                    return
                }
                val secondSplit = firstSplit[1].split(char)
                if (secondSplit.size != 2) {
                    System.err.println("bad rule definition $rule")
                    return
                }
                toSymbol = secondSplit[0]
                states.find { it.name == secondSplit[1] }?.let { toState = it }
                if (toState == emptyState) {
                    System.err.println("unknown state ${secondSplit[1]} in $rule")
                    return
                }
            } else {
                toState = endState
            }
            state.rules[fromSymbol] = Triple(toSymbol, char, toState)
        }
    }

    var state = beginState
    var iterations = 0
    while (state != endState) {
        iterations++
        val symbol = if (index >= 0 && index < tape.size) tape[index] else alphabet[0]
        val triple = state.rules[symbol]
        if (triple == null) {
            System.err.println("error at index $index in state ${state.name}")
            print("tape: ${tape.joinToString()}")
            return
        }
        if (triple.third == endState) {
            tape.removeAll { it == alphabet[0] }
            println(tape.joinToString())
            return
        }
        when {
            index < 0 -> {
                index = 0
                tape.add(0, triple.first)
            }
            index == tape.size -> tape.add(index, triple.first)
            else -> tape[index] = triple.first
        }

        if (triple.second == 'L') index-- else index++
        state = triple.third
        if (iterations > 1000) {
            System.err.println("too long")
            tape.removeAll { it == alphabet[0] }
            println(tape.joinToString())
            return
        }
    }
}

