package scaladex.infra.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl
import com.sksamuel.elastic4s.analysis._
import com.sksamuel.elastic4s.requests.mappings.FieldDefinition

object DataMapping extends ElasticDsl {
  val urlStrip: CharFilter = PatternReplaceCharFilter(
    "url_strip",
    "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)",
    ""
  )
  val codeStrip: CharFilter = PatternReplaceCharFilter(
    "code_strip",
    "<code>[\\w\\W]*?<\\/code>",
    ""
  )
  val englishStop: TokenFilter = StopTokenFilter(
    "english_stop",
    language = Some(NamedStopTokenFilter.English)
  )
  val englishStemmer: TokenFilter =
    StemmerTokenFilter("english_stemmer", "english")
  val englishPossessiveStemmer: TokenFilter = StemmerTokenFilter(
    "english_possessive_stemmer",
    "possessive_english"
  )

  val englishReadme: CustomAnalyzer =
    CustomAnalyzer(
      "english_readme",
      "standard",
      List("code_strip", "html_strip", "url_strip"),
      List(
        "lowercase",
        "english_possessive_stemmer",
        "english_stop",
        "english_stemmer"
      )
    )

  val lowercase: Normalizer =
    CustomNormalizer("lowercase", List(), List("lowercase"))

  val projectFields: Seq[FieldDefinition] = List(
    textField("organization")
      .analyzer("standard")
      .fields(
        keywordField("keyword").normalizer("lowercase")
      ),
    textField("repository")
      .analyzer("standard")
      .fields(
        keywordField("keyword").normalizer("lowercase")
      ),
    keywordField("artifactNames").normalizer("lowercase"),
    dateField("creationDate"),
    dateField("updateDate"),
    keywordField("platformTypes"),
    keywordField("scalaVersions"),
    keywordField("scalaJsVersions"),
    keywordField("scalaNativeVersions"),
    keywordField("sbtVersions"),
    textField("primaryTopic")
      .analyzer("english")
      .fields(
        keywordField("keyword").normalizer("lowercase")
      ),
    textField("githubInfo.description").analyzer("english"),
    textField("githubInfo.readme").analyzer("english_readme"),
    intField("githubInfo.forks"),
    intField("githubInfo.stars"),
    intField("githubInfo.contributorCount"),
    textField("githubInfo.topics")
      .analyzer("standard")
      .fields(
        keywordField("keyword").normalizer("lowercase")
      ),
    nestedField("githubInfo.beginnerIssues"),
    objectField("formerReferences").fields(
      textField("organization").analyzer("standard"),
      textField("repository").analyzer("standard")
    )
  )
}
