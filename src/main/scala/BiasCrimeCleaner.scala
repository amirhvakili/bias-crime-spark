import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions._

object BiasCrimeCleaner {
  // trim text, and turn blank strings into real nulls
  private def text(c: String): Column =
    when(trim(col(c)) === "", lit(null)).otherwise(trim(col(c))).as(c)

  // text -> number (a value that can't convert becomes null)
  private def num(c: String, t: String): Column =
    text(c).cast(t).as(c)

  // "2013/01/13 05:00:00+00" -> timestamp (the +00 means UTC)
  private def ts(c: String): Column =
    to_timestamp(regexp_replace(text(c), "\\+00$", ""), "yyyy/MM/dd HH:mm:ss").as(c)

  // spelling variants of the same group -> one label
  private val groupLabels = Map(
    "Black"          -> "Black/African",
    "Hispanic"       -> "Latino/Hispanic",
    "Middle Eastern" -> "Arab/Middle Eastern",
    "Unknown"        -> "Unspecified"
  )

  def apply(df: DataFrame): DataFrame = {
    val typed = df.select(
      num("x", "double"), num("y", "double"),
      text("ccn"), text("address"),
      ts("date_of_offense"), text("time_of_offense"), ts("date_offense_recorded"),
      num("report_year", "int"), num("month", "int"),
      text("police_district"), text("ward"),
      text("type_of_hate_bias"), text("targeted_group"), text("top_offense_type"),
      num("mar_id", "int"),
      num("mar_xcoord", "double"), num("mar_ycoord", "double"),
      num("mar_latitude", "double"), num("mar_longitude", "double"),
      ts("created_date"), ts("last_edited_date"),
      num("objectid", "int")
    )

    typed
      .withColumn("targeted_group_clean",
        coalesce(element_at(typedLit(groupLabels), col("targeted_group")), col("targeted_group")))
      .withColumn("targeted_group_multiple",
        coalesce(col("targeted_group").rlike(";| or "), lit(false)))
  }
}
