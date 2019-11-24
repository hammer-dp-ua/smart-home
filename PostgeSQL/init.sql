CREATE TABLE device_types (
   aa_id SERIAL PRIMARY KEY,
   type_id VARCHAR(50) UNIQUE
);

CREATE TABLE device_type_names (
   aa_id SERIAL PRIMARY KEY,
   device_type INTEGER NOT NULL REFERENCES device_types,
   device_name VARCHAR(100) NOT NULL
);

CREATE TABLE wi_fi_technical_device_info (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
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

INSERT INTO device_types (aa_id, type_id) VALUES (nextval('device_types_aa_id_seq'), 'ENV_SENSOR');
INSERT INTO device_types (aa_id, type_id) VALUES (nextval('device_types_aa_id_seq'), 'SHUTTER');
INSERT INTO device_types (aa_id, type_id) VALUES (nextval('device_types_aa_id_seq'), 'PROJECTOR');
INSERT INTO device_types (aa_id, type_id) VALUES (nextval('device_types_aa_id_seq'), 'MOTION_DETECTOR');

INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Basement temp and humidity monitor');
INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Bathroom fan');
INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Street temp and humidity monitor');
