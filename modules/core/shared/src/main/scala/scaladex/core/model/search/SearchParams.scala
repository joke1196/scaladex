package scaladex.core.model.search

import scaladex.core.model.BinaryVersion
import scaladex.core.model.Project

case class SearchParams(
    queryString: String = "",
    page: PageParams = PageParams(1, 20),
    sorting: Sorting = Sorting.Relevance,
    userRepos: Set[Project.Reference] = Set(),
    binaryVersion: Option[BinaryVersion] = None,
    cli: Boolean = false,
    topics: Seq[String] = Nil,
    languages: Seq[String] = Nil,
    platforms: Seq[String] = Nil,
    contributingSearch: Boolean = false
)
