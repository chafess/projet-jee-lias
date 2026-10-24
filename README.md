# 🔬 Plateforme de Gestion du Laboratoire LIAS

> **Projet JEE — Master Data Science & Big Data (2025–2026)**
> **Faculté des Sciences Ben M’Sik — Université Hassan II de Casablanca**

Application web d'entreprise dédiée à la centralisation, la traçabilité et la gouvernance scientifique et administrative du **Laboratoire d'Intelligence Artificielle et Systèmes (LIAS)**.

---

## 🎯 Objectifs du Système

* **Centraliser** la gestion du laboratoire LIAS.
* **Assurer la traçabilité** intégrale des activités scientifiques et administratives.
* **Capitaliser la mémoire institutionnelle** (documents, événements, décisions) sans jamais supprimer de données historiques.
* **Structurer les responsabilités** en séparant rigoureusement le statut académique du rôle de gouvernance.
* **Automatiser la production du rapport annuel obligatoire** pour le pilotage et le budget.

---

## 🚀 Fonctionnalités Principales

* **Gouvernance & Mandats :** gestion temporelle des mandats (Directeur, Chefs d'équipe) avec clôture automatique et historisation des périodes.
* **Cycle de vie des membres & départs :**
  * Candidatures et adhésions en ligne validées par le Directeur en mandat.
  * **Règle d'or :** comptes retraités gelés définitivement (connexion bloquée, patrimoine scientifique préservé) ; anciens membres désactivés et réactivables.
* **Production scientifique :** partage, consultation et filtrage par auteur / année / équipe des publications, avec protection contre l'usurpation (contrôle d'autorisation auteur).
* **Archivage documentaire :** dépôt et consultation sécurisés de documents officiels classés par types (Conventions, PV, Financements, Attestations, Programmes).
* **Rapport annuel automatisé :** bilan consolidé en temps réel (effectifs, publications, événements, matériels) avec impression et export PDF officiel.
* **Gestion équitable du matériel :** suivi des stocks en direct et gestion de la priorité (prioritaire pour ceux qui n'ont rien reçu).
* **Collaboration & calendrier :** messagerie collaborative interne (fils de discussion par équipe et par événement) et calendrier dynamique avec navigation temporelle.
* **Paramétrage dynamique des modules :** activation et désactivation à chaud des 5 modules métier (Publications, Matériel, Événements, Feed, Annuaire) via des intercepteurs Spring.

---

## 👥 Typologie des Rôles

* **Administrateur Système :** paramétrage dynamique des modules, gestion des comptes, sauvegardes, journal d'activité (logs).
* **Directeur du Laboratoire :** validation des adhésions, approbation du matériel, signature des conventions, pilotage du rapport annuel.
* **Vice-Directeur :** supervision globale et consultation étendue en mode lecture seule.
* **Chefs d'Équipe :** gestion des membres de l'équipe, suivi des publications et communication d'équipe.
* **Membres Permanents :** publication scientifique, déclaration des besoins matériels.
* **Doctorants :** consultation et soumission de publications (aucun accès aux modules administratifs internes).
* **Membres Associés :** collaborateurs externes avec accès ciblé selon autorisation.

---

## 🛠️ Technologies Utilisées

* **Backend :** Java 17, Spring Boot 3 (Spring MVC, Spring Data JPA)
* **Frontend :** Thymeleaf, Bootstrap 4, FontAwesome
* **Base de données :** MySQL (`lias_db`)
* **Architecture :** Modèle-Vue-Contrôleur (MVC), `@ControllerAdvice`, `HandlerInterceptor`
* **Gestionnaire de dépendances :** Maven

---

## 💻 Installation et Démarrage Local

### 1. Base de données

Créer une base de données MySQL locale :

```sql
CREATE DATABASE lias_db;
```

### 2. Dossier de stockage (Uploads)

Créer le dossier suivant à la racine du disque `C:` pour stocker les fichiers PDF et les photos :

```
C:/lias_uploads/
```

### 3. Lancer l'application

Exécuter l'application depuis IntelliJ IDEA ou en ligne de commande, à la racine du projet :

```bash
./mvnw spring-boot:run
```

L'application est accessible à l'adresse : <http://localhost:8082>

---

## 🔑 Comptes de Démonstration

**Mot de passe unique : `123`**

| Rôle                 | Email               | Description                                               |
| :------------------- | :------------------ | :-------------------------------------------------------- |
| **Administrateur**   | `admin@lias.ma`     | Paramétrage dynamique des modules, gestion des comptes    |
| **Directeur**        | `directeur@lias.ma` | Validation des adhésions, rapport annuel, conventions     |
| **Vice-Directeur**   | `vicedir@lias.ma`   | Supervision globale en lecture seule                      |
| **Membre Permanent** | `permanent@lias.ma` | Gestion des publications, besoins en matériel             |
| **Doctorant**        | `phd@lias.ma`       | Production scientifique (modules internes restreints)     |
| **Membre Associé**   | `associe@lias.ma`   | Collaborateur externe avec accès aux événements autorisés |
