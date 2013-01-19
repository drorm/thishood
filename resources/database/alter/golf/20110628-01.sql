--
-- Invitations for Facebook/etc
--
-- SQL authors:
--  Vitaliy Morarian
-- 

BEGIN;

CREATE TABLE `social_invite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `friend_id` varchar(255) NOT NULL,
  `friend_name` varchar(255) NOT NULL,
  `provider` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `date_created` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE INDEX `IDX_FK_social_invite_user` ON `social_invite` (`user_id`);
CREATE INDEX `IDX_social_invite_friend_id` ON `social_invite` (`friend_id`);

ALTER TABLE `social_invite` ADD CONSTRAINT `social_invite_user` 
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;


COMMIT;