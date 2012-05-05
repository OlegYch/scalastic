package com.traackr.scalastic.elasticsearch

import org.elasticsearch.client._, transport._
import org.elasticsearch.common.settings.ImmutableSettings._
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.node._, NodeBuilder._
import scala.collection.JavaConversions._
import org.slf4j.Log._

object Indexer {
  import org.elasticsearch.common.transport._

  def transport(settings: Map[String, String], host: String = "localhost", ports: Seq[Int] = Seq(9300)) = {
    require(settings.contains("cluster.name"))

    val builder = settingsBuilder
    for ((key, value) <- settings) builder.put(key, value)
    builder.put("client.transport.sniff", true)
    val client = new TransportClient(builder.build)
    for (each <- ports) client.addTransportAddress(new InetSocketTransportAddress(host, each))
    new ClientIndexer(client)
  }

  def using(settings: String): Indexer = using(settingsBuilder.loadFromSource(settings))

  def using(settings: Map[String, String]): Indexer = using(settingsBuilder.put(settings))

  private def using(builder: Builder) = at(nodeBuilder.settings(builder).build)

  def local = at(nodeBuilder.local(true).data(true).node)

  def at(node: Node) = new NodeIndexer(node)
}

trait Indexer extends ClusterAdmin with IndexCrud with Analysis with Indexing with Searching {
  val client: Client
  def start: Indexer
  def stop

  def catchUpOn(`type`: String, bar: Int, seed: Int = 1, maxFactor: Int = 64) = {
    var factor = seed
    while (factor <= maxFactor && count(types = Seq(`type`)) < bar) {
      info(this, "catching up on {} to bar {} in {} sec ...", `type`, bar, factor)
      Thread sleep factor * 1000
      factor *= 2
    }
    if (factor > maxFactor) throw new RuntimeException(
      """failed to catch up while indexing %s after %s seconds""".format(`type`, maxFactor))
  }

  def reindexWith[A](alias: String, originalIndex: String, parameters: Map[String, String])(f: (Map[String, String], Indexer) => A) = {
    import org.joda.time._
    import net.liftweb.json._, Extraction._
    implicit val formats = DefaultFormats
    import scala.collection._

    // before whole data indexing do:
    //	- memento the original settings ...
    val original = metadataFor(originalIndex).settings
    //	- ... then create a new index based on timestamp, without replication
    val reindexName = "%s__%s".format(originalIndex, new DateTime toString ("yyyy_MM_dd"))
    val withoutReplicas = new mutable.HashMap ++ original.getAsMap + ("index.number_of_replicas" -> "0")
    createIndex(index = reindexName, settings = compact(render(decompose(withoutReplicas.toMap))))
    waitTillActive()

    try {

      f(parameters, this)

    } finally {
      // after whole data indexing do:
      //	- optimize the newly indexed ...
      optimize(reindexName)
      // 	- restore to original settings ...
      updateSettings("""{"number_of_replicas": %s}""".format(original.get("index.number_of_replicas")), reindexName)
      //	- unalias original ... 
      unalias(alias, originalIndex)
      //	- ... then switch index alias to new index
      this.alias(alias, reindexName)
    }
  }
}

private[elasticsearch] class NodeIndexer(node: Node) extends Indexer {
  val client = node.client
  def start(): Indexer = { node.start; waitForYellowStatus(); this }
  def stop() = node.close
}

private[elasticsearch] class ClientIndexer(val client: Client) extends Indexer {
  def start(): Indexer = { waitForYellowStatus(); this }
  def stop() = client.close
}
