package ch.epfl.scala.index
package data

import model._
import org.elasticsearch.common.settings.Settings
import com.sksamuel.elastic4s._
import source.Indexable
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}

trait ProjectProtocol {

  implicit val formats = Serialization.formats(ShortTypeHints(List(
    classOf[Milestone],
    classOf[ReleaseCandidate],
    classOf[OtherPreRelease]
  ))) ++ List(ScopeSerializer)

  implicit val serialization = native.Serialization

  implicit object ProjectAs extends HitAs[Project] {

    override def as(hit: RichSearchHit): Project = read[Project](hit.sourceAsString)
  }

  implicit object ProjectIndexable extends Indexable[Project] {

    override def json(project: Project): String = write(project)
  }

  /**
    * Scope serializer, since Scope is not a case class json4s can't handle this by default
    *
    */
  object ScopeSerializer extends CustomSerializer[Scope]( format => (
    {
      case JString(scope) => Scope(scope)
    },
    {
      case scope: Scope => JString(scope.name)
    }
  ))
}

package object elastic extends ProjectProtocol {

  /** @see https://github.com/sksamuel/elastic4s#client for configurations */

  val maxResultWindow = 10000 // <=> max amount of projects (June 1st 2016 ~ 2500 projects)
  private val home = System.getProperty("user.home")
  val esSettings = Settings.settingsBuilder()
    .put("path.home", home + "/.esdata")
    .put("max_result_window", maxResultWindow)

  lazy val esClient = ElasticClient.local(esSettings.build)

  val indexName = "scaladex"
  val collectionName = "artifacts"
}