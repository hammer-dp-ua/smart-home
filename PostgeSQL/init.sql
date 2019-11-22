CREATE TABLE device_types (
   aa_id INTEGER PRIMARY KEY,
   type_id VARCHAR(50) UNIQUE
);

CREATE TABLE wi_fi_technical_device_info (
   aa_id INTEGER PRIMARY KEY,
   device_type INTEGER REFERENCES device_types,
   device_name VARCHAR(100) NOT NULL,
   errors SMALLINT,
   uptime_sec INTEGER,
   firmware_timestamp VARCHAR(50),
   free_heap INTEGER,
   reset_reason VARCHAR(100),
   info_dt TIMESTAMP
);

CREATE TABLE env_sensors_data (
   aa_id INTEGER PRIMARY KEY,
   technical_device_info INTEGER REFERENCES wi_fi_technical_device_info ON DELETE CASCADE,
   temperature REAL, -- float 4 bytes
   humidity REAL,
   light SMALLINT -- -32768 to +32767
);

INSERT INTO device_types (type_id) VALUES ('ENV_SENSOR');
INSERT INTO device_types (type_id) VALUES ('SHUTTER');
INSERT INTO device_types (type_id) VALUES ('PROJECTOR');
INSERT INTO device_types (type_id) VALUES ('MOTION_DETECTOR');

