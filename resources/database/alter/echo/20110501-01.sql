--
-- TH-334 Adding support of private messages
-- SQL authors:
--  Vitaliy Morarian
-- 

BEGIN;

CREATE TABLE `chat` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` BIGINT(20) NOT NULL,
    `discriminator` VARCHAR(24) NOT NULL,
    `date_created` DATETIME NOT NULL,
    `last_updated` DATETIME NOT NULL,
    `topic` VARCHAR(255) NOT NULL,
    `creator_id` BIGINT(20) NOT NULL,
    `community_id` BIGINT(20),
    PRIMARY KEY (`id`)
);

CREATE INDEX `IDX_FK_chat_creator_id` ON `chat` (`creator_id`);
CREATE INDEX `IDX_FK_chat_community_id` ON `chat` (`community_id`);


CREATE TABLE `chat_member` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` BIGINT(20) NOT NULL,
    `date_created` DATETIME NOT NULL,
    `last_updated` DATETIME NOT NULL,
    `chat_id` BIGINT(20) NOT NULL,
    `user_id` BIGINT(20) NOT NULL,
    `last_read` DATETIME NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX `IDX_FK_chat_member_chat_id` ON `chat_member` (`chat_id`);
CREATE INDEX `IDX_FK_chat_member_user_id` ON `chat_member` (`user_id`);


CREATE TABLE `chat_message` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` BIGINT(20) NOT NULL,
    `date_created` DATETIME NOT NULL,
    `last_updated` DATETIME NOT NULL,
    `chat_id` BIGINT(20) NOT NULL,
    `author_id` BIGINT(20) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `type` VARCHAR(24) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX `IDX_FK_chat_message_chat_id` ON `chat_message` (`chat_id`);
CREATE INDEX `IDX_FK_chat_message_author_id` ON `chat_message` (`author_id`);



ALTER TABLE `chat` ADD CONSTRAINT `user_chat` 
    FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `chat` ADD CONSTRAINT `user_group_chat` 
    FOREIGN KEY (`community_id`) REFERENCES `user_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;


ALTER TABLE `chat_member` ADD CONSTRAINT `chat_chat_member` 
    FOREIGN KEY (`chat_id`) REFERENCES `chat` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `chat_member` ADD CONSTRAINT `user_chat_member` 
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;


ALTER TABLE `chat_message` ADD CONSTRAINT `chat_chat_message` 
    FOREIGN KEY (`chat_id`) REFERENCES `chat` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `chat_message` ADD CONSTRAINT `chat_member_chat_message` 
    FOREIGN KEY (`author_id`) REFERENCES `chat_member` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;