import org.apache.spark.sql.SparkSession

object CleanBiasCrimeJob {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("CleanBiasCrimeJob")
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    val raw = Postgres.queryPartitioned(
      spark,
      "SELECT *, objectid::int AS objectid_num FROM bias_crime",
      column = "objectid_num",
      parts = 4
    )
    val df = BiasCrimeCleaner(raw).cache()

    println(s"Rows: ${df.count()}")
    Postgres.write(df, "bias_crime_clean")
    println("Wrote bias_crime_clean")

    spark.stop()
  }
}
