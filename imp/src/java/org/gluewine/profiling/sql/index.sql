CREATE INDEX gluewine_profiling_index
  ON gluewine_profiling
  USING btree (class_name, duration, exception, method, execution_date);