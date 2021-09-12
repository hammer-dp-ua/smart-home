INSERT INTO device_types (type_id, keep_alive_interval_sec) VALUES ('ENV_SENSOR', 60);
INSERT INTO device_types (type_id, keep_alive_interval_sec) VALUES ('SHUTTER', 60);
INSERT INTO device_types (type_id, keep_alive_interval_sec) VALUES ('PROJECTOR', 60);
INSERT INTO device_types (type_id, keep_alive_interval_sec) VALUES ('MOTION_DETECTOR', 30);

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

INSERT INTO fan_setup (aa_id, device_type_name, turn_on_humidity_threshold, manually_turned_on_timeout_minutes, after_falling_threshold_work_timeout_minutes)
VALUES (nextval('fan_setup_aa_id_seq'), (SELECT aa_id FROM device_type_names WHERE device_name = 'Bathroom fan'), 85, 10, 30);