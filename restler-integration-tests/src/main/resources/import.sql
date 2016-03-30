CREATE TABLE persons(id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE pets(id INT PRIMARY KEY, name VARCHAR(255), owner_id INT, FOREIGN KEY(owner_id) REFERENCES persons(id));
CREATE TABLE addresses(id INT PRIMARY KEY, name VARCHAR(255), owner_id INT, FOREIGN KEY(owner_id) REFERENCES persons(id));
CREATE TABLE posts(id INT PRIMARY KEY, message VARCHAR(255), author_id INT, FOREIGN KEY(author_id) REFERENCES persons(id));

INSERT INTO persons (id, name) VALUES ('0', 'person0');
INSERT INTO persons (id, name) VALUES ('1', 'person1');
INSERT INTO persons (id, name) VALUES ('2', 'person2');

INSERT INTO pets (id, name, owner_id) VALUES ('0', 'bobik', '0');
INSERT INTO pets (id, name, owner_id) VALUES ('1', 'sharik', '0');
INSERT INTO pets (id, name, owner_id) VALUES ('2', 'pet2', '1');
INSERT INTO pets (id, name, owner_id) VALUES ('3', 'pet3', '1');

INSERT INTO addresses (id, name, owner_id) VALUES ('0', 'Earth', '0');
INSERT INTO addresses (id, name, owner_id) VALUES ('1', 'Mars', '0');

INSERT INTO posts (id, message, author_id) VALUES ('0', 'Hello', '0');
INSERT INTO posts (id, message, author_id) VALUES ('1', 'World', '1');
INSERT INTO posts (id, message, author_id) VALUES ('2', 'Hello, again!', '2');