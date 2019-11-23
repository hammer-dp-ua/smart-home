CREATE TABLE device_types (
   aa_id SERIAL PRIMARY KEY,
   type_id VARCHAR(50) UNIQUE
);

CREATE TABLE wi_fi_technical_device_info (
   aa_id SERIAL PRIMARY KEY,
   device_type INTEGER NOT NULL REFERENCES device_types,
   device_name VARCHAR(100) NOT NULL,
   errors SMALLINT,
   uptime_sec INTEGER,
   firmware_timestamp VARCHAR(50),
   free_heap INTEGER,
   gain INTEGER,
   reset_reason VARCHAR(500),
   system_restart_reason VARCHAR(500),
   info_dt TIMESTAMP NOT NULL
);

CREATE TABLE env_sensors_data (
   aa_id SERIAL PRIMARY KEY,
   technical_device_info INTEGER NOT NULL REFERENCES wi_fi_technical_device_info ON DELETE CASCADE,
   temperature REAL, -- float 4 bytes
   humidity REAL,
   light SMALLINT -- -32768 to +32767
);

INSERT INTO device_types (aa_id, type_id) VALUES (1, 'ENV_SENSOR');
INSERT INTO device_types (aa_id, type_id) VALUES (2, 'SHUTTER');
INSERT INTO device_types (aa_id, type_id) VALUES (3, 'PROJECTOR');
INSERT INTO device_types (aa_id, type_id) VALUES (4, 'MOTION_DETECTOR');

