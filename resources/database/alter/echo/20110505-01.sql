--
-- TH-364 Issues with user approval
-- added status to persist decision and display to user on >1 try to reject/approve join to group
--
-- SQL authors:
--  Vitaliy Morarian
-- 

BEGIN;

ALTER TABLE membership_verification ADD COLUMN status VARCHAR(24);
UPDATE membership_verification set status='PENDING';
ALTER TABLE membership_verification MODIFY status VARCHAR(24) NOT NULL;

COMMIT;