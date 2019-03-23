import java.io.File
import java.util.LinkedList
import java.util.Scanner

class State(val name: String)

fun main() {
    val file = File("main.tmc")
    if (!file.exists()) {
        System.err.println("no such file")
        return
    }
    val sc = Scanner(file)
    val code = LinkedList<String>()
    val states: List<State>
    var indexOfBeginState = -1
    var indexOfEndState = -1
    var indexOfAlphabet = -1


    var i = 0
    while (sc.hasNextLine()) {
        val line = sc.nextLine().trim { it <= ' ' }
        if (line != "") {
            if (line.startsWith("def")) {
                if (line.endsWith("start")) {
                    if (indexOfBeginState == -1) {
                        indexOfBeginState = i
                    } else {
                        System.err.println("Another start state definition")
                        System.err.println("line $i")
                        return
                    }
                }
                if (line.endsWith("end")) {
                    if (indexOfEndState == -1) {
                        indexOfEndState = i
                    } else {
                        System.err.println("Another end state definition")
                        System.err.println("line $i")
                        return
                    }
                }
            }
            if(line.startsWith("alphabet")){
                if (indexOfAlphabet == -1) {
                    indexOfAlphabet = i
                } else {
                    System.err.println("Another alphabet definition")
                    System.err.println("line $i")
                    return
                }
            }
            code.add(line)
        }
        i++
    }

}
