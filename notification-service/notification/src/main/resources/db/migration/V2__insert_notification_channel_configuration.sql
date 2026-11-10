INSERT INTO notification_channel_configuration (notification_type)
VALUES ('WELCOME'), ('FAILED_LOGIN');

INSERT INTO notification_channel_configuration_channels
    (notification_channel_configuration_id, channel)
SELECT c.id, ch.channel
FROM notification_channel_configuration c
CROSS JOIN (VALUES ('EMAIL'), ('INAPP')) AS ch(channel)
WHERE c.notification_type IN ('WELCOME');
INSERT INTO notification_channel_configuration_channels
    (notification_channel_configuration_id, channel)
SELECT c.id, ch.channel
FROM notification_channel_configuration c
CROSS JOIN (VALUES  ('INAPP')) AS ch(channel)
WHERE c.notification_type IN ( 'FAILED_LOGIN');