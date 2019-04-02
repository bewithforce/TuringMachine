import java.io.File
import java.util.LinkedList
import java.util.Scanner

data class State(val name: String){
    val rules = ArrayList<String>()
}

fun main() {
    val emptyState = State("")
    var temp = emptyState
    var beginState = emptyState
    var endState = emptyState
    var indexOfAlphabet = -1
    val alphabet: MutableSet<String> = HashSet()
    val file = File("main.tmc")
    if (!file.exists()) {
        System.err.println("no such file")
        return
    }
    val sc = Scanner(file)
    val states: MutableSet<State> = HashSet()
    val rulesTable = HashMap<State, LinkedList<String>>()


    var i = -1
    while (sc.hasNextLine()) {
        i++
        val line = sc.nextLine().trim { it <= ' ' }
        if (line.isNotBlank()) {
            if (line.startsWith("def")) {
                if (!line.matches("def [a-zA-Z]\\w+( start| end)?".toRegex())) {
                    System.err.println("Bad definition")
                    System.err.println("line $i")
                    return
                }
                temp = State(line.split(" ")[1])
                if (states.contains(temp)) {
                    System.err.println("Redefinition")
                    System.err.println("line $i")
                    return
                }
                if (line.endsWith("start")) {
                    if (beginState == emptyState) {
                        beginState = temp
                    } else {
                        System.err.println("Start redefinition")
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
                val alphabetRegex = "alphabet=\\[[\\S+, ]*[\\S+]\\]".toRegex()
                if (!line.matches(alphabetRegex)){
                    System.err.println("Bad alphabet definition")
                    System.err.println("line $i")
                    return
                }
                val protoAlphabet = line.substringAfter("alphabet=[").substringBefore("]").split(", ")
                for (protoChar in protoAlphabet){
                    if (alphabet.contains(protoChar)){
                        System.err.println("Bad alphabet definition")
                        System.err.println("line $i")
                        return
                    }
                    alphabet.add(protoChar)
                }
                continue
            }
            if (temp == endState){
                System.err.println("End state rule")
                System.err.println("line $i")
                return
            }
            if (rulesTable[temp] == null){
                rulesTable[temp] = LinkedList()
            }
            rulesTable[temp]!!.add(line)
        }
    }

    val code = rulesTable.flatMap { it.value }

    if(beginState == emptyState){
        System.err.println("No begin state")
        return
    }
    if(endState == emptyState){
        System.err.println("No end state")
        return
    }


    print("stop")
}

