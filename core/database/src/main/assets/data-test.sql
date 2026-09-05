-- Skills
INSERT INTO skills (id, name) VALUES ('1', 'Speed');
INSERT INTO skills (id, name) VALUES ('2', 'Memory');
INSERT INTO skills (id, name) VALUES ('3', 'Attention');
INSERT INTO skills (id, name) VALUES ('4', 'Flexibility');
INSERT INTO skills (id, name) VALUES ('5', 'Problem solving');
INSERT INTO skills (id, name) VALUES ('6', 'Math');

-- Games
INSERT INTO games (id, name, description, skill_id) VALUES ('speed_match', 'Speed Match', 'Decide whether the current symbol matches the previous one. Train processing speed under time pressure.', '1');
INSERT INTO games (id, name, description, skill_id) VALUES ('memory_matrix', 'Memory Matrix', 'Memorize the highlighted tiles, then tap them back in the same pattern.', '2');
INSERT INTO games (id, name, description, skill_id) VALUES ('eagle_eye', 'Eagle Eye', 'Find the one shape that does not belong before time runs out.', '3');
INSERT INTO games (id, name, description, skill_id) VALUES ('lost_in_migration', 'Lost in Migration', 'Spot the bird flying in a different direction from the flock.', '3');
INSERT INTO games (id, name, description, skill_id) VALUES ('homeward', 'Homeward', 'Trace a path home through the grid while planning each move carefully.', '4');
INSERT INTO games (id, name, description, skill_id) VALUES ('matrix_deduction', 'Matrix Deduction', 'Study the pattern and choose the missing tile that completes the matrix.', '5');
INSERT INTO games (id, name, description, skill_id) VALUES ('compute_challenge', 'Compute Challenge', 'Tap falling equations that are true and let the false ones pass.', '6');
