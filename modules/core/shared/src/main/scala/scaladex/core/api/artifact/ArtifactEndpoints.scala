package scaladex.core.api.artifact

import scaladex.core.model.search.Page

trait ArtifactEndpoints
    extends ArtifactEndpointSchema
    with endpoints4s.algebra.Endpoints
    with endpoints4s.algebra.JsonEntitiesFromSchemas {

  val artifactEndpointParams: QueryString[ArtifactParams] = (qs[Option[String]](
    name = "language",
    docs = Some(
      "Filter the results matching the given language version only (e.g., '3', '2.13', '2.12', '2.11', 'java')"
    )
  ) & qs[Option[String]](
    name = "platform",
    docs = Some("Filter the results matching the given platform only (e.g., 'jvm', 'sjs1', 'native0.4', 'sbt1.0')")
  )).xmap((ArtifactParams.apply _).tupled)(Function.unlift(ArtifactParams.unapply))

  val artifactMetadataParams: Path[ArtifactMetadataParams] = (segment[String](
    name = "groupId",
    docs = Some("Result matching the given group id only (e.g., 'org.apache.spark', 'org.scala-lang'")
  ) / segment[String](
    name = "artifactId",
    docs = Some("Result matching the given artifact id only (e.g., 'scala-library', 'spark-parent_2.11'")
  )).xmap((ArtifactMetadataParams.apply _).tupled)(Function.unlift(ArtifactMetadataParams.unapply))

  // Artifact endpoint definition
  val artifact: Endpoint[ArtifactParams, Page[ArtifactResponse]] =
    endpoint(
      get(path / "api" / "artifacts" /? artifactEndpointParams),
      ok(jsonResponse[Page[ArtifactResponse]])
    )

  // Artifact metadata endpoint definition
  val artifactMetadata: Endpoint[ArtifactMetadataParams, Page[ArtifactMetadataResponse]] =
    endpoint(
      get(path / "api" / "artifacts" / artifactMetadataParams),
      ok(jsonResponse[Page[ArtifactMetadataResponse]])
    )
}
