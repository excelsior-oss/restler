CREATE TABLE persons(id INT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE pets(id INT PRIMARY KEY, name VARCHAR(255), owner1_id INT);

INSERT INTO persons (id, name) VALUES ('0', 'test name');
INSERT INTO persons (id, name) VALUES ('1', 'test name');

INSERT INTO pets (id, name, owner1_id) VALUES ('0', 'bobik', '0');
INSERT INTO pets (id, name, owner1_id) VALUES ('1', 'sharik', '0');
