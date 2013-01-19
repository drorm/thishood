--
-- TH-390 Two phase signup
-- persisting of state/city/email for users who are not yet covered by TH
--
-- SQL authors:
--  Vitaliy Morarian
-- 

BEGIN;

CREATE TABLE `prospect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `city` varchar(255) NOT NULL,
  `date_created` datetime NOT NULL,
  `email` varchar(255) NOT NULL,
  `state` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
);

CREATE UNIQUE INDEX `IDX_prospect_email` ON `prospect` (`email`);


COMMIT;