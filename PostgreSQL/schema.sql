-- CREATE DATABASE super_home

CREATE TEMPORARY TABLE device_types (
   aa_id SERIAL PRIMARY KEY,
   type_id VARCHAR(50) UNIQUE,
   keep_alive_interval_sec SMALLINT
);

CREATE TEMPORARY TABLE device_type_names (
   aa_id SERIAL PRIMARY KEY,
   device_type INTEGER NOT NULL REFERENCES device_types,
   device_name VARCHAR(100) NOT NULL UNIQUE,
   ip4_address VARCHAR(15)
);

CREATE TEMPORARY TABLE wi_fi_technical_device_info (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   errors SMALLINT,
   uptime_sec INTEGER,
   firmware_timestamp VARCHAR(50),
   free_heap INTEGER,
   gain SMALLINT,
   reset_reason VARCHAR(500),
   system_restart_reason VARCHAR(500),
   info_dt TIMESTAMP NOT NULL
);

CREATE TEMPORARY TABLE env_sensors_data (
   aa_id SERIAL PRIMARY KEY,
   technical_device_info INTEGER NOT NULL REFERENCES wi_fi_technical_device_info ON DELETE CASCADE,
   temperature REAL, -- float 4 bytes
   humidity REAL,
   light SMALLINT -- -32768 to +32767
);

CREATE TEMPORARY TABLE alarm_sources_setup (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   source VARCHAR(50) NOT NULL,
   ignore_alarms boolean
);

CREATE TEMPORARY TABLE alarm_sensors_data (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   source INTEGER REFERENCES alarm_sources_setup,
   alarm_dt TIMESTAMP NOT NULL
);

CREATE TEMPORARY TABLE shutters_action_time_setup (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   action_time SMALLINT
);

CREATE TEMPORARY TABLE fan_setup (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   turn_on_humidity_threshold REAL,
   manually_turned_on_timeout_minutes SMALLINT,
    -- to not turn off immediately after turning on on threshold
   after_falling_threshold_work_timeout_minutes SMALLINT
);