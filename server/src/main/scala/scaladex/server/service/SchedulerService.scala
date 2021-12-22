package scaladex.server.service

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration._

import com.typesafe.scalalogging.LazyLogging
import scaladex.core.service.SchedulerDatabase
import scaladex.core.service.SearchEngine
import scaladex.core.util.ScalaExtensions._
import scaladex.infra.github.GithubClient
import scaladex.server.service.SchedulerService._
import scaladex.template.SchedulerStatus

class SchedulerService(db: SchedulerDatabase, searchEngine: SearchEngine, githubClientOpt: Option[GithubClient])
    extends LazyLogging {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  private val mostDependentProjectScheduler = Scheduler("most-dependent", mostDependentProjectJob, 1.hour)
  private val updateProject = Scheduler("update-projects", updateProjectJob, 30.minutes)
  private val searchSynchronizer = new SearchSynchronizer(db, searchEngine)
  private val githubSynchronizerOpt = githubClientOpt.map(client => new GithubSynchronizer(db, client))
  private val moveReleasesSynchronizer = new MoveReleasesSynchronizer(db)

  private val schedulers = Map[String, Scheduler](
    mostDependentProjectScheduler.name -> mostDependentProjectScheduler,
    updateProject.name -> updateProject,
    searchSynchronizer.name -> searchSynchronizer,
    moveReleasesSynchronizer.name -> moveReleasesSynchronizer
  ) ++
    githubSynchronizerOpt.map(g => Map(g.name -> g)).getOrElse(Map.empty)

  def startAll(): Unit =
    schedulers.values.foreach(_.start())

  def start(name: String): Unit =
    schedulers.get(name).foreach(_.start())

  def stop(name: String): Unit =
    schedulers.get(name).foreach(_.stop())

  def getSchedulers(): Seq[SchedulerStatus] =
    schedulers.values.toSeq.map(_.status)

  private def mostDependentProjectJob(): Future[Unit] =
    for {
      _ <- updateProjectDependenciesTable(db)
    } yield ()

  private def updateProjectJob(): Future[Unit] =
    for {
      _ <- updateCreatedTimeIn(db)
    } yield ()
}

object SchedulerService {

  def updateProjectDependenciesTable(db: SchedulerDatabase)(implicit ec: ExecutionContext): Future[Unit] =
    for {
      projectWithDependencies <- db
        .computeProjectDependencies()
        .mapFailure(e =>
          new Exception(
            s"not able to getAllProjectDependencies because of ${e.getMessage}"
          )
        )
      _ <- db
        .insertProjectDependencies(projectWithDependencies)
        .mapFailure(e =>
          new Exception(
            s"not able to insertProjectDependencies because of ${e.getMessage}"
          )
        )

    } yield ()

  private def updateCreatedTimeIn(db: SchedulerDatabase)(implicit ec: ExecutionContext): Future[Unit] = {
    // one request at time
    val future = for {
      oldestReleases <- db.computeAllProjectsCreationDate()
      _ <- oldestReleases.mapSync { case (creationDate, ref) => db.updateProjectCreationDate(ref, creationDate) }
    } yield ()
    future.mapFailure(e => new Exception(s"not able to updateCreatedTimeIn all projects because of ${e.getMessage}"))
  }
}