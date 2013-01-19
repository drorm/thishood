--
-- TH-399 Add statistics for groups (most popular, posted, etc)
-- persisting gathered statistics
--
-- SQL authors:
--  Vitaliy Morarian
-- 

BEGIN;


CREATE TABLE `application_metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `community_metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `community_id` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);


CREATE INDEX `IDX_FK_community_metric_community` ON `community_metric` (`community_id`);

ALTER TABLE `community_metric` ADD CONSTRAINT `community_metric_community` 
    FOREIGN KEY (`community_id`) REFERENCES `user_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;


ALTER TABLE user_group ADD COLUMN stat_popularity_index int(11) DEFAULT 0;

COMMIT;