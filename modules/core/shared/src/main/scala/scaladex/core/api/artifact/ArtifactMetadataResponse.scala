package scaladex.core.api.artifact

final case class ArtifactMetadataResponse(
    version: String,
    projectReference: String,
    releaseDate: String,
    language: String,
    platform: String
)
