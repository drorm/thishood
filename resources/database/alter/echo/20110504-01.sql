--
-- TH-353 Recommendation module
-- SQL authors:
--  Dror Matalon
--  Vitaliy Morarian
-- 

BEGIN;

CREATE TABLE `user_reference_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

COMMIT;