-- Raw landing table: every column is TEXT on purpose, so the load never fails.
-- Types are fixed later by the Spark job (see BiasCrimeCleaner.scala).
CREATE TABLE IF NOT EXISTS bias_crime (
  x TEXT, y TEXT, ccn TEXT, address TEXT,
  date_of_offense TEXT, time_of_offense TEXT, date_offense_recorded TEXT,
  report_year TEXT, month TEXT, police_district TEXT, ward TEXT,
  type_of_hate_bias TEXT, targeted_group TEXT, top_offense_type TEXT,
  mar_id TEXT, mar_xcoord TEXT, mar_ycoord TEXT,
  mar_latitude TEXT, mar_longitude TEXT,
  created_date TEXT, last_edited_date TEXT, objectid TEXT,
  shape TEXT, se_anno_cad_data TEXT
);
