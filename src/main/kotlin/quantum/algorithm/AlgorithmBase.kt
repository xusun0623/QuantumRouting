package quantum.algorithm

import quantum.topo.Edge
import quantum.topo.Node
import quantum.topo.Path
import quantum.topo.Topo
import utils.require
import java.io.BufferedWriter
import java.io.Writer

abstract class Algorithm(val topo: Topo) {
    abstract val name: String //算法名称
    lateinit var logWriter: BufferedWriter //输出记录
    var settings: String = "Simple" //设置

    val srcDstPairs: MutableList<Pair<Node, Node>> = mutableListOf() //源端点&目的端点对 List<Pair<Node, Node>>

    fun work(pairs: List<Pair<Node, Node>>): Pair<Int, Int> {
        require({ topo.isClean() }) //清除拓扑
        srcDstPairs.clear() //清除源端点&目的端点
        srcDstPairs.addAll(pairs) //初始化

        val pairs = srcDstPairs.map { "${it.first.id}⟷${it.second.id}" } //生成量子对的txt
//    println("""[$settings] Establishing: $pairs""".trimIndent())
        logWriter.appendln(pairs.joinToString())

        P2() // 阶段2

        tryEntanglement() // 尝试针对每个源端点-目的端点生成纠缠对

        P4() // 阶段1

        val established =
            srcDstPairs.map { (n1, n2) -> n1 to n2 to topo.getEstablishedEntanglements(n1, n2) } // 源端点&目的端点是否纠缠

//    established.forEach {
//      it.second.forEach {
//        println("Established path: ${it}")
//      }
//    }
        println("""[$settings] Established: ${established.map { "${it.first.first.id}⟷${it.first.second.id} × ${it.second.size}" }} - $name""".trimIndent())

        topo.clearEntanglements() // 清除纠缠对
        return established.count { it.second.isNotEmpty() } to established.sumBy { it.second.size } // 返回生成纠缠的数量
    }

    fun tryEntanglement() {
        topo.links.forEach { it.tryEntanglement() }  // 尝试建立纠缠
    }

    abstract fun prepare()

    abstract fun P2()

    abstract fun P4()
}

typealias PickedPath = Triple<Double, Int, Path>
