# QAxion SGI — App Android

App Android nativo para o **QAxion Quality Management System** com WebView sofisticado.

---

## 📱 Funcionalidades

| Recurso | Descrição |
|---|---|
| **Splash Screen** | Tela animada com logo QAxion + fade/scale |
| **Seletor de Servidor** | Chips para ZAIT / INOVA TS |
| **Grid de Obras** | Cards com todas as obras disponíveis |
| **URL Personalizada** | FAB para acessar qualquer obra por URL |
| **WebView Avançado** | JavaScript, DOM storage, uploads, câmera |
| **Swipe to Refresh** | Arraste para baixo para recarregar |
| **Toolbar Customizada** | Logo, título da obra, botão voltar, menu |
| **Barra de Progresso** | Loading indicator abaixo da toolbar |
| **Estado de Erro** | Tela bonita quando sem conexão |
| **Upload de Arquivos** | Escolher da galeria OU tirar foto |
| **Dialogs Nativos** | alert/confirm do JS renderizam como Material dialogs |
| **CSS Mobile Injection** | Injeta melhorias CSS automaticamente |

---

## 🛠️ Como Abrir no Android Studio

1. Abra o **Android Studio**
2. Clique em `File → Open`
3. Selecione a pasta `QAxionApp`
4. Aguarde o Gradle sync
5. Clique em ▶️ **Run** ou `Shift+F10`

---

## ➕ Como Adicionar Novas Obras

Abra `SelectorActivity.kt` e adicione na lista `obras`:

```kotlin
private val obras = listOf(
    // Existentes...
    Obra("Z.25.06", "Obra Z.25.06", "https://zait.qaxion.com.br/Z.25.06/index.php", "zait"),
    Obra("Z.25.07", "Obra Z.25.07", "https://zait.qaxion.com.br/Z.25.07/index.php", "zait"),
    // Obras INOVA TS:
    Obra("I.25.01", "Obra INOVA 01", "https://inova.qaxion.com.br/obra221.html", "inova"),
)
```

O parâmetro `server` deve ser `"zait"` ou `"inova"` para filtrar corretamente.

---

## 🔧 Requisitos

- Android Studio Hedgehog (2023.1) ou superior
- Android SDK 34
- Kotlin 1.9+
- Dispositivo/emulador Android 7.0+ (API 24)

---

## 📦 Estrutura do Projeto

```
QAxionApp/
├── app/src/main/
│   ├── java/com/qaxion/app/
│   │   ├── SplashActivity.kt     ← Tela de splash animada
│   │   ├── SelectorActivity.kt   ← Seletor de servidor/obra
│   │   └── MainActivity.kt       ← WebView principal
│   ├── res/
│   │   ├── layout/               ← XMLs das telas
│   │   ├── drawable/             ← Ícones e backgrounds
│   │   ├── anim/                 ← Animações de transição
│   │   ├── values/               ← Cores, strings, temas
│   │   └── xml/                  ← Segurança de rede
│   └── AndroidManifest.xml
└── build.gradle
```

---

## 🎨 Design

- **Cores:** Azul primário `#1565C0` (identidade QAxion)
- **Tema:** Material Design 3 Light
- **Animações:** Fade + Scale na splash, Slide nas transições
- **Cards:** Material Cards com bordas sutis

---

*Desenvolvido para QAxion Quality Management — v1.0.0*
