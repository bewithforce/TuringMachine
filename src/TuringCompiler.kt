import java.io.File
import java.util.LinkedList
import java.util.Scanner

class State(val name: String) {
    val rules = HashMap<Char, Triple<Char, Char?, State>>()

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
    var temp = ""
    var indexOfAlphabet = -1
    val alphabet: MutableSet<Char> = HashSet()
    val file = File("main.tmc")
    if (!file.exists()) {
        System.err.println("no such file")
        return
    }
    val sc = Scanner(file)
    val checkStates: MutableSet<String> = HashSet()
    val rulesTable = HashMap<String, LinkedList<String>>()

    val emptyState = State("")
    var beginState = emptyState
    var endState = emptyState
    val states: MutableSet<State> = HashSet()


    var i = -0
    while (sc.hasNextLine()) {
        i++
        val line = sc.nextLine().trim()
        if (line.isNotBlank()) {
            if (line.startsWith("def")) {
                if (!line.matches("def\\s+$regexState(\\s*start|\\s*end)?".toRegex())) {
                    System.err.println("Bad state definition")
                    System.err.println("line $i")
                    return
                }
                temp = line.split("\\s+".toRegex())[1]
                if (checkStates.contains(temp)) {
                    System.err.println("Redefinition")
                    System.err.println("line $i")
                    return
                }
                if (line.endsWith("start")) {
                    if (beginState == emptyState) {
                        beginState = State(temp)
                    } else {
                        System.err.println("Start redefinition")
                        System.err.println("line $i")
                        return
                    }
                }
                if (line.endsWith("end")) {
                    if (endState == emptyState) {
                        endState = State(temp)
                    } else {
                        System.err.println("End redefinition")
                        System.err.println("line $i")
                        return
                    }
                }
                checkStates.add(temp)
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
                val alphabetRegex = "alphabet\\s*=\\s*\\[($regexSymbol\\s*,\\s*)*$regexSymbol\\s*\\]".toRegex()
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
            if (temp == endState.name) {
                System.err.println("End state rule")
                System.err.println("line $i")
                return
            }
            if (!line.matches("$regexSymbol\\s*=>$regexSymbol\\s*[LR]$regexState".toRegex()) and
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

    for (stateCheck in checkStates) {
        val state = State(stateCheck)
        if (stateCheck == "")
            continue
        val rules = rulesTable[stateCheck]
        if (rules == null) {
            System.err.println("no rules for state $stateCheck")
            return
        }
        for (rule in rules) {
            val firstSplit = rule.split("\\s*=>\\s*".toRegex())
            val fromSymbol = firstSplit[0]
            var toSymbol: Char
            var toState = emptyState
            var char:Char? = null
            if (!alphabet.contains(fromSymbol)) {
                println("Unknown symbol $fromSymbol in rule $rule")
                return
            }
            if (firstSplit[1] != endState.name) {
                char = firstSplit[1].find { it == 'L' || it == 'R' }
                if (char == null){
                    println("bad rule definition $rule")
                    return
                }
                val secondSplit = firstSplit[1].split(char)
                println(secondSplit)
                if (secondSplit.size != 2) {
                    println("bad rule definition $rule")
                    return
                }
                toSymbol = secondSplit[0]
                states.find { it.name == secondSplit[1] }?.let { toState = it }
                if (toState == emptyState) {
                    println("unknown state ${secondSplit[1]} in $rule")
                    return
                }
            } else {
                toState = endState
            }
            state.rules[fromSymbol] = Triple(toSymbol, char, toState)
        }
        states.add(state)
    }

    var tape = readLine() ?: return
    var index: Int
    Scanner(System.`in`).let {
        index = it.nextInt()
        it.close()
    }

    var state = beginState
    while (state != endState){
        val symbol = tape[index]
        beginState.rules[symbol]
    }


    print("stop")
}

