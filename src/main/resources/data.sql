-- ------------------------------------------------
-- ------------ OLYMPIC MEDAL TRACKER -------------
-- ------------------------------------------------

-- ---- PAYS --------------------------------------
INSERT INTO pays (nom, code, drapeau) VALUES ('États-Unis', 'USA', '🇺🇸');
INSERT INTO pays (nom, code, drapeau) VALUES ('Chine', 'CHN', '🇨🇳');
INSERT INTO pays (nom, code, drapeau) VALUES ('Grande-Bretagne', 'GBR', '🇬🇧');
INSERT INTO pays (nom, code, drapeau) VALUES ('France', 'FRA', '🇫🇷');
INSERT INTO pays (nom, code, drapeau) VALUES ('Sénégal', 'SEN', '🇸🇳');
INSERT INTO pays (nom, code, drapeau) VALUES ('Jamaïque', 'JAM', '🇯🇲');
INSERT INTO pays (nom, code, drapeau) VALUES ('Kenya', 'KEN', '🇰🇪');
INSERT INTO pays (nom, code, drapeau) VALUES ('Japon', 'JPN', '🇯🇵');

-- ---- ATHLETES --------------------------------------
-- USA
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Johnson', 'Noah', '1999-03-12', 'Natation', 1);
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Williams', 'Serena', '1995-07-22', 'Athlétisme', 1);
-- Chine
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Zhang', 'Wei', '2001-01-05', 'Gymnastique', 2);
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Li', 'Na', '1998-11-30', 'Natation', 2);
-- France
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Dupont', 'Léon', '2000-04-18', 'Natation', 4);
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Martin', 'Clarisse', '1997-09-09', 'Judo', 4);
-- Sénégal
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Faye', 'Mbaye', '1998-06-14', 'Lutte', 5);
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Seck', 'Adja', '2002-02-28', 'Athlétisme', 5);
-- Jamaïque
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Thompson', 'Elaine', '1996-08-03', 'Athlétisme', 6);
-- Kenya
INSERT INTO athletes (nom, prenom, date_naissance, discipline, pays_id) VALUES ('Kipchoge', 'Eliud', '1994-11-17', 'Athlétisme', 7);

-- ---- COMPETITIONS -------------------------------------
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('100m Hommes', 'Athlétisme', '2026-07-26', '2026-07-26', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('100m Femmes', 'Athlétisme', '2026-07-26', '2026-07-26', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('Marathon Hommes', 'Athlétisme', '2026-08-10', '2026-08-10', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('50m Nage Libre Hommes', 'Natation', '2026-07-28', '2026-07-28', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('100m Nage Libre Femmes', 'Natation', '2026-07-29', '2026-07-29', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('Judo -70kg Femmes', 'Judo', '2026-07-30', '2026-07-30', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('Lutte Libre 65kg', 'Lutte', '2026-08-05', '2026-08-05', 'TERMINEE');
INSERT INTO competitions (nom, discipline, date_debut, date_fin, statut) VALUES ('Sol Femmes', 'Gymnastique', '2026-08-01', '2026-08-01', 'EN_COURS');

-- ---- MEDAILLES --------------------------------------
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-07-26', 2, 1, 1);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-07-26', 9, 6, 1);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-07-26', 8, 5, 1);

INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-07-26', 9, 6, 2);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-07-26', 8, 5, 2);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-07-26', 2, 1, 2);

INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-08-10', 10, 7, 3);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-08-10', 1,  1, 3);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-08-10', 5,  4, 3);

INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-07-28', 5, 4, 4);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-07-28', 1, 1, 4);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-07-28', 4, 2, 4);

INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-07-29', 4, 2, 5);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-07-29', 1, 1, 5);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-07-29', 5, 4, 5);

INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-07-30', 6, 4, 6);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-07-30', 3, 2, 6);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-07-30', 7, 5, 6);

INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('OR',     '2026-08-05', 7, 5, 7);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('ARGENT', '2026-08-05', 1, 1, 7);
INSERT INTO medailles (type, date_obtention, athlete_id, pays_id, competition_id) VALUES ('BRONZE', '2026-08-05', 3, 2, 7);