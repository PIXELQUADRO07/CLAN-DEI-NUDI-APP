<div align="center">
<img width="377" height="852" alt="Screenshot_20260606_080354" src="https://github.com/user-attachments/assets/d3345010-c5fe-406c-af8b-d81536e241f3" />
<img width="374" height="853" alt="Screenshot_20260606_080528" src="https://github.com/user-attachments/assets/f719bd95-fa12-43b3-ac55-abf3c409dfa5" />
<img width="377" height="839" alt="Screenshot_20260606_080555" src="https://github.com/user-attachments/assets/6db86954-0464-4868-bf91-06f22c22b14a" />


# 🎭 Clan Dei Nudi App
**L'app ufficiale del Clan Dei Nudi**

[English Version](#english) | [Versione Italiana](#versione-italiana)

</div>

---

## Versione Italiana

### 📱 Descrizione
L'app ufficiale di Clan Dei Nudi, sviluppata con **Google AI Studio** e **Kotlin** per Android. Progettata per offrire un'esperienza moderna e intuitiva ai membri della comunità.

### 🚀 Funzionalità
- Interfaccia nativa Android
- Integrazione con API Gemini di Google
- Architettura moderna in Kotlin

### ✅ Prerequisiti
- **Android Studio** ([Scarica qui](https://developer.android.com/studio))
- **Android SDK 21+**
- Chiave API Gemini (gratuita da [Google AI Studio](https://ai.studio))

### 📖 Guida di Installazione

1. **Clona il repository**
   ```bash
   git clone https://github.com/PIXELQUADRO07/CLAN-DEI-NUDI-APP.git
   cd CLAN-DEI-NUDI-APP
   ```

2. **Apri Android Studio**
   - Seleziona **File → Open**
   - Scegli la cartella del progetto
   - Attendi che Android Studio risolva le dipendenze automaticamente

3. **Configura la chiave API**
   - Crea un file `.env` nella root del progetto
   - Aggiungi la tua chiave API Gemini:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```
   - Consulta `.env.example` per un esempio

4. **Rimuovi la configurazione di firma per il debug** (facoltativo)
   - Apri `build.gradle.kts` nell'app
   - Rimuovi la linea: `signingConfig = signingConfigs.getByName("debugConfig")`

5. **Avvia l'app**
   - Seleziona un emulatore o un dispositivo fisico
   - Clicca **Run → Run 'app'** o premi `Shift + F10`

### 📋 File Importanti
- `build.gradle.kts` - Configurazione del progetto e dipendenze
- `.env.example` - Esempio di variabili d'ambiente
- `.env` - **Non committare!** Mantieni la tua API key privata

### 🛠️ Tecnologie Utilizzate
- **Linguaggio**: Kotlin 100%
- **Framework**: Android Native
- **AI**: Google Gemini API
- **Build System**: Gradle

### 📚 Documentazione Utile
- [Google AI Studio](https://ai.studio/apps/39cce7ce-e771-4eb9-85f2-0b1582d76c1d)
- [Documentazione Android](https://developer.android.com/docs)
- [Guida Kotlin](https://kotlinlang.org/docs/home.html)
- [Google Gemini API](https://ai.google.dev)

### 💡 Risoluzione Problemi
- **Errori di compilazione**: Pulisci il progetto con `Build → Clean Project` e riavvia
- **Problemi di emulatore**: Crea un nuovo dispositivo virtuale da AVD Manager
- **Errore API Key**: Verifica che `.env` sia nella cartella root e contenga la chiave corretta

### 🤝 Contribuire
Le pull request sono benvenute! Per modifiche significative, apri prima un issue per discutere i cambiamenti proposti.

### 📄 Licenza
Consulta il file LICENSE per i dettagli.

---

## English

### 📱 Description
The official app of Clan Dei Nudi, developed with **Google AI Studio** and **Kotlin** for Android. Designed to provide a modern and intuitive experience for community members.

### 🚀 Features
- Native Android Interface
- Google Gemini API Integration
- Modern Kotlin Architecture

### ✅ Prerequisites
- **Android Studio** ([Download here](https://developer.android.com/studio))
- **Android SDK 21+**
- Gemini API Key (free from [Google AI Studio](https://ai.studio))

### 📖 Installation Guide

1. **Clone the repository**
   ```bash
   git clone https://github.com/PIXELQUADRO07/CLAN-DEI-NUDI-APP.git
   cd CLAN-DEI-NUDI-APP
   ```

2. **Open Android Studio**
   - Select **File → Open**
   - Choose the project directory
   - Wait for Android Studio to resolve dependencies automatically

3. **Configure your API Key**
   - Create an `.env` file in the project root
   - Add your Gemini API key:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```
   - Check `.env.example` for an example

4. **Remove debug signing configuration** (optional)
   - Open `build.gradle.kts` in the app
   - Remove the line: `signingConfig = signingConfigs.getByName("debugConfig")`

5. **Run the app**
   - Select an emulator or physical device
   - Click **Run → Run 'app'** or press `Shift + F10`

### 📋 Important Files
- `build.gradle.kts` - Project configuration and dependencies
- `.env.example` - Environment variables example
- `.env` - **Don't commit!** Keep your API key private

### 🛠️ Technologies Used
- **Language**: Kotlin 100%
- **Framework**: Android Native
- **AI**: Google Gemini API
- **Build System**: Gradle

### 📚 Useful Documentation
- [Google AI Studio](https://ai.studio/apps/39cce7ce-e771-4eb9-85f2-0b1582d76c1d)
- [Android Documentation](https://developer.android.com/docs)
- [Kotlin Guide](https://kotlinlang.org/docs/home.html)
- [Google Gemini API](https://ai.google.dev)

### 💡 Troubleshooting
- **Compilation errors**: Clean the project with `Build → Clean Project` and restart
- **Emulator issues**: Create a new virtual device from AVD Manager
- **API Key error**: Verify that `.env` is in the root folder and contains the correct key

### 🤝 Contributing
Pull requests are welcome! For significant changes, please open an issue first to discuss proposed changes.

### 📄 License
See the LICENSE file for details.

---

<div align="center">
  
**Made with ❤️ for Clan Dei Nudi**

</div>
