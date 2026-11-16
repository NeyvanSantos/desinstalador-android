# 📱 Desinstalador de Android (Batch App Uninstaller)

[![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Vercel Site](https://img.shields.io/badge/Landing_Page-Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://desinstalador-android.vercel.app)
[![License](https://img.shields.io/badge/License-MIT-amber?style=for-the-badge)](LICENSE)

> Aplicativo nativo Android desenvolvido em **Kotlin** e **Jetpack Compose** para remoção rápida e em lote de múltiplos aplicativos com apenas 1 toque. 100% offline, sem necessidade de root, Shizuku ou conexão com computador.

---

## ⚡ Comparativo de Produtividade

| Métrica | Método Manual (Configurações do Android) | Desinstalador em Lote |
| :--- | :--- | :--- |
| **Tempo médio (15 apps)** | ~ 4 minutos e 20 segundos | **~ 12 segundos (95% mais rápido)** |
| **Interações do usuário** | 30+ toques manuais em diálogos | **1 toque para confirmar a fila** |
| **Dependência de PC** | Frequentemente exige cabo/ADB | **Zero dependência (100% no celular)** |
| **Privacidade** | - | **100% Offline (Sem permissão de internet)** |

---

## 🚀 Principais Recursos

- **Remoção em Fila Automatizada**: Utiliza o `AccessibilityService` do Android para automatizar os cliques de confirmação ("OK" / "Desinstalar") sequencialmente.
- **Carregamento Instantâneo**: Otimizado com leitura direta de tamanho via `File.length()` e *lazy loading* de ícones no `LazyColumn`, carregando 600+ apps em menos de 2 segundos.
- **Blindagem de Segurança (Anti-Tapjacking)**: Implementa `filterTouchesWhenObscured = true` para bloquear sobreposições maliciosas e ataques de toque fantasma.
- **Proteção do Sistema**: Whitelist de segurança que impede a exclusão acidental de componentes essenciais (Google Play Store, WebView, ajustes do sistema e launchers de fábrica).
- **Auto-Desarme (Fail-Closed)**: Timeout de 15 segundos que desativa o serviço de acessibilidade se a fila for interrompida ou concluída.
- **Compatibilidade Ampla**: Suporte oficial ao instalador nativo do Google (Pixel / AOSP), Xiaomi (MIUI / HyperOS) e Samsung (One UI).
- **Tema Dark OLED**: Interface limpa em Material Design 3 com pretos puros para economia de bateria em telas AMOLED.

---

## 🏗️ Arquitetura do Projeto

```text
com.neyvan.desinstalador/
├── accessibility/
│   └── AutoUninstallAccessibilityService.kt  # Automação do instalador nativo com timeout
├── data/
│   ├── model/
│   │   └── InstalledApp.kt                   # Modelo de dados de app instalado
│   └── repository/
│       └── AppRepository.kt                  # Varredura do PackageManager e lista protegida
├── ui/
│   ├── theme/                               # Cores, Dark Theme OLED e tipografia
│   ├── components/                           # Componentes reutilizáveis
│   ├── AppListScreen.kt                      # Tela principal com filtros e ordenação
│   └── MainViewModel.kt                      # ViewModel reativa com StateFlow
└── MainActivity.kt                           # Ponto de entrada com filtro anti-tapjacking
```

---

## 🌐 Landing Page & Download

O projeto conta com uma landing page oficial hospedada na Vercel com design minimalista e animações fluidas via GSAP 3:

👉 **[desinstalador-android.vercel.app](https://desinstalador-android.vercel.app)**

O APK de produção foi minificado com **ProGuard / R8**, reduzindo o tamanho de **20.5 MB** para apenas **1.13 MB**.

---

## 🛠️ Como Compilar o Projeto

### Pré-requisitos
- Android Studio Ladybug ou superior
- JDK 17
- Android SDK 35 (Android 15)

### Build via Gradle
```bash
# Clonar o repositório
git clone https://github.com/NeyvanSantos/desinstalador-android.git
cd desinstalador-android

# Compilar versão Debug
./gradlew assembleDebug

# Compilar versão Release Otimizada (R8)
./gradlew assembleRelease
```

---

## 📄 Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.

Desenvolvido por **[Neyvan Santos](https://github.com/NeyvanSantos)**.
