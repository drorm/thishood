--
-- TH-353 Recommendation module
--
-- SQL authors:
--  Vitaliy Morarian
--
BEGIN;

CREATE TABLE `user_reference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` BIGINT(20) NOT NULL,
    `date_created` DATETIME NOT NULL,
    `for_user_id` BIGINT(20) NOT NULL,
    `from_user_id` BIGINT(20) NOT NULL,
    `message` LONGTEXT NOT NULL,
    `score` INTEGER(11) NOT NULL,
    `subject` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX `IDX_FK_user_reference_user_for` ON `user_reference` (`for_user_id`);
CREATE INDEX `IDX_FK_user_reference_user_from` ON `user_reference` (`from_user_id`);

ALTER TABLE `user_reference` ADD CONSTRAINT `user_user_reference_user_from` 
    FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `user_reference` ADD CONSTRAINT `user_user_reference_user_for` 
    FOREIGN KEY (`for_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;