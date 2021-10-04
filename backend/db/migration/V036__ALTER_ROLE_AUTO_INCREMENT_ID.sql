ALTER TABLE user_role
DROP FOREIGN KEY user_role_ibfk_2;

ALTER TABLE role
DROP PRIMARY KEY,
MODIFY id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT;

ALTER TABLE user_role
ADD CONSTRAINT user_role_role_id_fk FOREIGN KEY (role_id) REFERENCES role (id);
