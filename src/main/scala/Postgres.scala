import org.apache.spark.sql.{DataFrame, SparkSession}

object Postgres {
  private def env(name: String): String =
    sys.env.getOrElse(name, sys.error(s"Missing environment variable: $name"))

  private def conn = Map(
    "url"      -> env("PG_URL"),
    "user"     -> env("PG_USER"),
    "password" -> env("PG_PASSWORD"),
    "driver"   -> "org.postgresql.Driver"
  )

  private def reader(spark: SparkSession) =
    spark.read.format("jdbc").options(conn)

  def query(spark: SparkSession, sql: String): DataFrame =
    reader(spark).option("dbtable", s"($sql) AS q").load()

  def queryPartitioned(spark: SparkSession, sql: String, column: String, parts: Int): DataFrame = {
    val b = query(spark, s"SELECT MIN($column) AS lo, MAX($column) AS hi FROM ($sql) AS s").first()
    reader(spark)
      .option("dbtable", s"($sql) AS q")
      .option("partitionColumn", column)
      .option("lowerBound", b.get(0).toString)
      .option("upperBound", b.get(1).toString)
      .option("numPartitions", parts.toString)
      .load()
  }

  // replaces the target table if it already exists
  def write(df: DataFrame, table: String): Unit =
    df.write.format("jdbc").options(conn)
      .option("dbtable", table)
      .mode("overwrite")
      .save()
}
