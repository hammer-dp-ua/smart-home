CREATE TABLE device_types (
   aa_id SERIAL PRIMARY KEY,
   type_id VARCHAR(50) UNIQUE,
   keep_alive_interval_sec SMALLINT
);

CREATE TABLE device_type_names (
   aa_id SERIAL PRIMARY KEY,
   device_type INTEGER NOT NULL REFERENCES device_types,
   device_name VARCHAR(100) NOT NULL UNIQUE,
   ip4_address VARCHAR(15)
);

CREATE TABLE wi_fi_technical_device_info (
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

CREATE TABLE env_sensors_data (
   aa_id SERIAL PRIMARY KEY,
   technical_device_info INTEGER NOT NULL REFERENCES wi_fi_technical_device_info ON DELETE CASCADE,
   temperature REAL, -- float 4 bytes
   humidity REAL,
   light SMALLINT -- -32768 to +32767
);

CREATE TABLE alarm_sensors_data (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   source VARCHAR(50) NOT NULL,
   alarm_dt TIMESTAMP NOT NULL
);

CREATE TABLE shutters_action_time_setup (
   aa_id SERIAL PRIMARY KEY,
   device_type_name INTEGER NOT NULL REFERENCES device_type_names,
   action_time SMALLINT
);

INSERT INTO device_types (type_id) VALUES ('ENV_SENSOR');
INSERT INTO device_types (type_id) VALUES ('SHUTTER');
INSERT INTO device_types (type_id) VALUES ('PROJECTOR');
INSERT INTO device_types (type_id) VALUES ('MOTION_DETECTOR');

INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Basement temp and humidity monitor');
INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Bathroom fan');
INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Street temp and humidity monitor');
INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'ENV_SENSOR'), 'Room temp and humidity monitor');

INSERT INTO device_type_names (aa_id, device_type, device_name, ip4_address)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'PROJECTOR'), 'Entrance projectors', '192.168.0.23');
INSERT INTO device_type_names (aa_id, device_type, device_name, ip4_address)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'PROJECTOR'), 'Storehouse projector', '192.168.0.22');

INSERT INTO device_type_names (aa_id, device_type, device_name, ip4_address)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'SHUTTER'), 'Room shutter', '192.168.0.27');
INSERT INTO device_type_names (aa_id, device_type, device_name, ip4_address)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'SHUTTER'), 'Kitchen shutter', '192.168.0.29');

INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'MOTION_DETECTOR'), 'Entrance Motion Detector');
INSERT INTO device_type_names (aa_id, device_type, device_name)
VALUES (nextval('device_type_names_aa_id_seq'), (SELECT aa_id FROM device_types WHERE type_id = 'MOTION_DETECTOR'), 'Motion Sensor with Immobilizer');

INSERT INTO shutters_action_time_setup (aa_id, device_type_name, action_time)
VALUES (nextval('shutters_action_time_setup_aa_id_seq'), (SELECT aa_id FROM device_type_names WHERE device_name = 'Room shutter'), 19);
INSERT INTO shutters_action_time_setup (aa_id, device_type_name, action_time)
VALUES (nextval('shutters_action_time_setup_aa_id_seq'), (SELECT aa_id FROM device_type_names WHERE device_name = 'Kitchen shutter'), 25);