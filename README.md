# Olympic Medal Tracker - API

Système de suivi des médailles olympiques en temps réel développé avec **Spring Boot 4.0.3**.

---

- [Introduction](#introduction)
- [Stack Technique](#stack-technique)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Lancement avec Docker](#lancement-avec-docker)
- [Lancement Manuel](#lancement-manuel)
- [Variables d'Environnement](#variables-denvironnement)
- [API REST](#api-rest)
- [Tests](#tests)
- [Collection Postman](#collection-postman)

---

## Introduction

L'application expose une API REST complète permettant de :

- Gérer les **pays**, **athlètes** et **compétitions**
- Enregistrer et consulter les **médailles** (or, argent, bronze)
- Consulter le **classement** des nations en temps réel avec plusieurs modes de tri
- Paginer et trier les résultats

---

## Stack Technique

| Composant | Technologie | Version |
|---|---|---|
| Langage | Java | 25 |
| Framework | Spring Boot | 4.0.3 |
| ORM | Spring Data JPA / Hibernate | 7.x |
| Base de données | PostgreSQL | 17 |
| Base de données (tests) | H2 Database | 2.4.x |
| Validation | Jakarta Bean Validation | 3.x |
| Build | Maven | 3.9.x |
| Réduction boilerplate | Lombok | 1.18.x |

---

## Architecture

L'application suit une **architecture en couches** stricte :

```
Controller  →  Service  →  Repository  →  Database
    ↕              ↕
  DTOs        Interfaces (ISP)
```

### Principes appliqués

- **SOLID** — Single Responsibility, Open/Closed (Strategy Pattern), Interface Segregation, Dependency Inversion
- **Repository Pattern** — abstraction de l'accès aux données
- **DTO Pattern** — découplage entre l'API et les entités JPA
- **Strategy Pattern** — tri du classement extensible sans modification du service
- **Builder Pattern** — construction des entités dans les tests

### Structure du projet

```
src/main/java/com/fleety/olympics/
├── controller/          ← Couche présentation (HTTP)
├── config/              ← Configuration
├── dto/
│   ├── request/         ← DTOs entrants (validation)
│   └── response/        ← DTOs sortants
├── exception/           ← Gestion des erreurs
├── model/               ← Entités JPA
├── repository/          ← Accès base de données
├── service/
│   ├── interfaces/      ← Contrats (ISP)
│   └── *.java           ← Implémentations métier
└── strategy/            ← Stratégies de tri du classement
```

---

## Prérequis

- **Java 21+**
- **Maven 3.9+**
- **Docker** (optionnel, recommandé)
- **PostgreSQL 15+** (si lancement sans Docker)

---

## Configuration

### `application.properties`

```properties
# Base de données
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/olympics_db}
spring.datasource.username=${DB_USERNAME:olympics_user}
spring.datasource.password=${DB_PASSWORD:olympics_pass}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# API
api.version=${API_VERSION:api/v2}
server.port=${PORT:8080}

# Désactiver data.sql en production
spring.sql.init.mode=never
spring.jpa.defer-datasource-initialization=true
```

---

## Lancement avec Docker

### Option recommandée — Image prête avec données

Une image Docker PostgreSQL est disponible sur Docker Hub avec les données de test déjà chargées :

```bash
docker run -it -p 5432:5432 --name olympics jiem117/olympics:1.0-pg
```

Puis lancer l'application :

```bash
mvn spring-boot:run
```

### Option Docker Compose

Créer un fichier `docker-compose.yml` à la racine :

```yaml
services:
  postgres:
    image: jiem117/olympics:1.0-pg
    container_name: olympics_postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: olympics_db
      POSTGRES_USER: olympics_user
      POSTGRES_PASSWORD: olympics_pass

  app:
    image: jiem117/olympics:2.0
    container_name: olympics_app
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/olympics_db
      DB_USERNAME: olympics_user
      DB_PASSWORD: olympics_pass
    depends_on:
      - postgres
```

```bash
docker compose up -d
```

---

## Lancement Manuel

### 1. Créer la base de données PostgreSQL

```bash
psql -U postgres

CREATE DATABASE olympics_db;
CREATE USER olympics_user WITH PASSWORD 'olympics_pass';
GRANT ALL PRIVILEGES ON DATABASE olympics_db TO olympics_user;

\c olympics_db
GRANT ALL ON SCHEMA public TO olympics_user;
\q
```

### 2. Cloner et lancer

```bash
git clone https://github.com/jiemCode/olympic_medal_tracker.git
cd olympic_medal_tracker

mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`.

---

## Variables d'Environnement

| Variable | Description | Valeur par défaut |
|---|---|---|
| `DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/olympics_db` |
| `DB_USERNAME` | Utilisateur PostgreSQL | `olympics_user` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | `olympics_pass` |
| `API_VERSION` | Version de l'API dans l'URL | `api/v2` |
| `PORT` | Port d'écoute du serveur | `8080` |

---

## API REST

Base URL : `http://localhost:8080/api/v2`

### Pays

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/pays` | Liste des pays |
| `GET` | `/pays/{id}` | Détail d'un pays |
| `POST` | `/pays` | Créer un pays |
| `PUT` | `/pays/{id}` | Modifier un pays |
| `DELETE` | `/pays/{id}` | Supprimer un pays |

### Athlètes

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/athletes` | Liste des athlètes |
| `GET` | `/athletes/{id}` | Détail d'un athlète |
| `GET` | `/athletes/pays/{paysId}` | Athlètes d'un pays |
| `POST` | `/athletes` | Créer un athlète |
| `PUT` | `/athletes/{id}` | Modifier un athlète |
| `DELETE` | `/athletes/{id}` | Supprimer un athlète |

### Compétitions

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/competitions` | Liste des compétitions |
| `GET` | `/competitions/{id}` | Détail d'une compétition |
| `POST` | `/competitions` | Créer une compétition |
| `PUT` | `/competitions/{id}` | Modifier une compétition |
| `DELETE` | `/competitions/{id}` | Supprimer une compétition |

### Médailles

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/medailles` | Liste des médailles |
| `GET` | `/medailles/{id}` | Détail d'une médaille |
| `GET` | `/medailles/athlete/{athleteId}` | Médailles d'un athlète |
| `GET` | `/medailles/competition/{competitionId}` | Médailles d'une compétition |
| `POST` | `/medailles` | Attribuer une médaille |
| `PUT` | `/medailles/{id}` | Modifier une médaille |
| `DELETE` | `/medailles/{id}` | Supprimer une médaille |

### Classement

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/classement` | Classement par total de médailles |
| `GET` | `/classement?tri=or` | Classement par médailles d'or |
| `GET` | `/classement?tri=argent` | Classement par médailles d'argent |
| `GET` | `/classement?tri=bronze` | Classement par médailles de bronze |
| `GET` | `/classement?tri=points` | Classement par points (or=3, argent=2, bronze=1) |
| `GET` | `/classement/pays/{paysId}` | Statistiques d'un pays |

### Pagination

Tous les endpoints `GET` de liste supportent la pagination :

```
GET /api/v1/pays?page=0&size=10&sortBy=nom&direction=asc
```

| Paramètre | Description | Défaut |
|---|---|---|
| `page` | Numéro de page (commence à 0) | `0` |
| `size` | Éléments par page | `10` |
| `sortBy` | Champ de tri | `nom` |
| `direction` | Sens du tri (`asc` ou `desc`) | `asc` |

### Codes de réponse

| Code | Signification |
|---|---|
| `200` | Succès |
| `201` | Ressource créée |
| `204` | Suppression réussie |
| `400` | Données invalides |
| `404` | Ressource introuvable |
| `409` | Conflit — doublon |
| `500` | Erreur serveur |

---

## Tests

```bash
# Lancer tous les tests
mvn test

# Tests unitaires uniquement
mvn test -Dtest="*ServiceTest"

# Tests d'intégration uniquement
mvn test -Dtest="*ControllerTest"

# Rapport de couverture
mvn verify
```

Les tests utilisent **H2 Database** — aucune connexion PostgreSQL requise.

```
src/test/
├── java/.../controller/    ← Tests d'intégration (MockMvc)
│   ├── PaysControllerTest
│   ├── AthleteControllerTest
│   ├── CompetitionControllerTest
│   └── MedailleControllerTest
├── java/.../service/       ← Tests unitaires (Mockito)
│   ├── PaysServiceTest
│   ├── AthleteServiceTest
│   ├── CompetitionServiceTest
│   └── MedailleServiceTest
└── resources/
    └── application-test.properties
```

---

## Historiques

| Version | Description |
|---|---|
| `v1.0.0` | API REST complète et fonctionnelle |
| `v1.1.0` | SOLID + Design Patterns (Strategy, Builder, ISP) |
| `v1.2.0` | Tests unitaires et d'intégration |
| `v2.0.0` | Pagination + PostgreSQL |
| `v2.0.1` | Documentation `javadoc` |
