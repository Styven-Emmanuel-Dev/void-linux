# void-linux
Terminal Linux On Android 

<p align="center">
  <img src="void-linux-git.png" alt="Void-Linux" width="100%" />
</p>

<h1 align="center">Void-Linux</h1>
<p align="center">
  <strong>Environnement hacker tout-en-un sur Android</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-10%2B-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/Kali-Linux-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Kotlin-1.9-purple?style=flat-square" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" />
  <img src="https://img.shields.io/github/actions/workflow/status/VOTRE_USER/Void-Linux/build.yml?style=flat-square" />
</p>

---

## 🌌 Qu'est-ce que Void-Linux ?

**Void-Linux** est une application Android qui transforme ton téléphone en
un véritable environnement de travail pour la cybersécurité. Elle intègre
nativement :

- 🐉 **Kali Linux** installé automatiquement via `proot` (sans root)
- 💻 **Terminal complet** avec support ANSI/VT100
- 🪟 **Émulation Windows** via Wine + Box64 (exécution de `.exe`)
- 🧅 **Réseau Tor** et navigation `.onion` sécurisée
- 🛡️ **Surveillance de sécurité** en temps réel
- 📍 **Fausse position GPS** pour préserver ta vie privée

Le tout dans une interface sombre, minimaliste et pensée pour les
professionnels de la sécurité.

---

## ✨ Fonctionnalités

### 🐉 Linux auto-installé

- Kali Linux déployé automatiquement au premier lancement
- Aucun root nécessaire (proot)
- Support de Debian, Ubuntu, Alpine (extensible)
- Gestionnaire de paquets `apt` fonctionnel

### 💻 Terminal intégré

- Émulateur VT100/ANSI complet
- Support des couleurs 256 et true color
- Clavier étendu (ESC, TAB, CTRL, flèches)
- PTY natif pour un vrai shell

### 🪟 Windows (Wine + Box64)

- Exécution de programmes Windows `.exe`
- Box64 pour traduire x86_64 → ARM64
- Préfixe Wine isolé
- Sortie console en temps réel

### 🧅 Tor et Dark Web

- Détection et démarrage d'Orbot
- Proxy SOCKS global
- Navigateur `.onion` intégré
- User-Agent anti-fingerprinting

### 🛡️ Sécurité

- Surveillance des téléchargements (`FileObserver`)
- Analyse des APK (signature SHA-256 + permissions)
- Détection de drain de batterie
- Surveillance réseau par UID
- Notifications push immédiates

### 📍 Localisation

- Fausse position GPS avec presets mondiaux
- Position personnalisée
- Guide de durcissement système

---

## 📸 Aperçu

<p align="center">
  <img src="void-linux-git.png" alt="Void-Linux Preview" width="80%" />
</p>

---

## 📥 Installation

### Téléchargement direct

Récupère la dernière version depuis
<<<<<<< HEAD
[**Releases**](https://github.com/VOTRE_USER/Void-Linux/releases).
=======
[**Releases**](https://github.com/Wazestudio/void-Linux/releases).
>>>>>>> 5657826d0e16b81cb87f9483952d3a71c90a152a

### Compilation depuis les sources

```bash
# Clone
<<<<<<< HEAD
git clone https://github.com/VOTRE_USER/Void-Linux.git
=======
git clone https://github.com/Wazestudio/void-Linux.git
>>>>>>> 5657826d0e16b81cb87f9483952d3a71c90a152a
cd Void-Linux

# Génère les icônes mipmap
bash tools/generate-mipmaps.sh

# Télécharge le rootfs Kali (optionnel)
bash tools/fetch-kali-rootfs.sh arm64 app/src/main/assets

# Build
./gradlew assembleDebug

# APK généré dans :
# app/build/outputs/apk/debug/app-debug.apk
```

---

🏗️ Architecture

```
Void-Linux/
├── app/                    → Application principale
├── core/
│   ├── common/             → Utilitaires partagés
│   ├── designsystem/       → Thème Material 3
│   ├── data/               → Préférences + SQLite
│   └── native/             → Code C (proot loader, PTY)
├── feature/
│   ├── terminal/           → Émulateur terminal
│   ├── linux/              → Installation Kali
│   ├── windows/            → Wine + Box64
│   ├── tor/                → Réseau Tor
│   ├── security/           → Surveillance
│   ├── location/           → Fausse position
│   └── settings/           → Durcissement
├── library/
│   ├── proot-engine/       → Moteur proot
│   ├── termux-bootstrap/   → Bootstrap Termux
│   ├── wine/               → Binaires Wine
│   └── natives/            → Binaires proot
└── tools/                  → Scripts de build
```

---

🔧 Prérequis

Outil Version
Android SDK API 34
NDK r25c+
JDK 17
Gradle 8.5+
Kotlin 1.9.24

---

🚀 CI/CD

Le projet utilise GitHub Actions pour :

· ✅ Build automatique à chaque push
· ✅ Génération des icônes mipmap
· ✅ Téléchargement du rootfs Kali
· ✅ Compilation des binaires natifs
· ✅ Publication automatique sur les tags v*

Créer une release

```bash
git tag v1.0.0
git push origin v1.0.0
```

L'APK sera automatiquement attaché à la release GitHub.

---

⚠️ Avertissement légal

Void-Linux est un outil destiné à :

· ✅ Tests d'intrusion autorisés
· ✅ Recherche en sécurité
· ✅ Protection de la vie privée personnelle
· ✅ Apprentissage de la cybersécurité

Il est strictement interdit d'utiliser cet outil pour :

· ❌ Attaquer des systèmes sans autorisation
· ❌ Accéder à des données protégées
· ❌ Contourner des mesures de sécurité légales

Les auteurs ne sont pas responsables de l'usage qui en est fait.
Utilise-le de manière éthique et légale.

---

🤝 Contribution

Les contributions sont les bienvenues !

1. Fork le projet
2. Crée une branche (git checkout -b feature/ma-fonctionnalite)
3. Commit (git commit -m 'Ajout de ma fonctionnalité')
4. Push (git push origin feature/ma-fonctionnalite)
5. Ouvre une Pull Request

---

📄 Licence

MIT License — voir LICENSE pour plus de détails.

---

🙏 Remerciements

· Kali Linux — Distribution de pentesting
· Termux — Environnement Linux Android
· PRoot — Émulation de root sans root
· Wine — Couche de compatibilité Windows
· Box64 — Émulateur x86_64
· Tor Project — Anonymat réseau
· NetCipher — Intégration Tor Android

---

<p align="center">
  <strong>Void-Linux</strong> — Là où le vide devient puissance.
</p>

<p align="center">
  Fait Wazestudio pour la communauté cybersécurité
</p>


---