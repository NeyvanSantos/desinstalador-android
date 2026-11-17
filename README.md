# DesinstalaJá

Aplicativo Android nativo para localizar, selecionar e solicitar a desinstalação de vários aplicativos em sequência. O app usa Kotlin, Jetpack Compose e os fluxos oficiais do Android; não exige root, ADB, Shizuku, conexão com a internet nem servidor.

## Como funciona

- O usuário escolhe os aplicativos e inicia a fila.
- O Android abre seu diálogo oficial de desinstalação para cada item; a confirmação do sistema continua sendo necessária.
- Opcionalmente, o usuário pode ativar o serviço de acessibilidade. Após divulgação e consentimento dentro do app, o serviço só tenta tocar no botão de desinstalação quando reconhece o instalador permitido, uma evidência exata do app atual e a ação de remoção. Apps do sistema, nomes ambíguos, diálogos de atualização ou evidência insuficiente ficam para confirmação manual.
- A fila verifica no `PackageManager` se o pacote realmente deixou de estar instalado. Se não puder confirmar, pausa e oferece tentar novamente, pular ou cancelar.
- Apps do sistema ficam ocultos por padrão. A lista de sistema exige aviso antes de ser exibida; pacotes essenciais conhecidos permanecem protegidos. A classificação considera também as flags do Android, mas não garante que qualquer outro app seja seguro para remover.
- O indicador de tamanho representa o tamanho dos APKs e splits, não o armazenamento total ocupado pelo app.

O serviço lê texto e botões somente da janela ativa do instalador, processa os dados localmente e não os armazena nem transmite. A fila e os identificadores dos apps selecionados são salvos em armazenamento privado para permitir recuperação após encerramento do processo, mas são excluídos de backup na nuvem e transferência entre dispositivos. O snapshot da fila é removido ao concluir ou cancelar; a seleção restante permanece até ser alterada. Não é mantido um histórico permanente de apps removidos.

## Privacidade e distribuição independente

O app não declara a permissão `INTERNET` e será distribuído como APK assinado pelo site oficial, sem publicação na Google Play. `QUERY_ALL_PACKAGES` é usada para listar os apps locais; a seleção e o progresso da fila permanecem no dispositivo. A confirmação manual continua disponível quando o instalador não oferece informações suficientes à automação.

## Estrutura

```text
app/src/main/java/com/neyvan/desinstalador/
├── accessibility/
│   ├── AccessibilityHelper.kt
│   ├── AccessibilityServiceAvailability.kt
│   ├── AccessibilityTextEvidence.kt
│   ├── AutoUninstallAccessibilityService.kt
│   └── UninstallAutomationPolicy.kt
├── data/
│   ├── AppRepository.kt
│   ├── InstalledApp.kt
│   └── InstalledAppCategory.kt
├── ui/
│   ├── UninstallerScreen.kt
│   ├── UninstallerViewModel.kt
│   ├── UninstallQueueState.kt
│   ├── UninstallQueueOperationCoordinator.kt
│   └── UninstallQueueStore.kt
└── MainActivity.kt
```

## Compilação e testes (Windows / PowerShell)

Pré-requisitos: Android Studio, Android SDK 35 e JDK 17.

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

## Assinatura Release

O Release usa uma chave própria de produção e nunca usa a chave Debug. **Não gere uma chave automaticamente.** A criação deve ser autorizada pelo responsável e feita uma única vez, em um local protegido fora do repositório. Exemplo do comando a executar manualmente após autorização:

```powershell
keytool -genkeypair -v `
  -keystore "C:\caminho-seguro\desinstalaja-release.jks" `
  -alias desinstalaja-prod `
  -keyalg RSA -keysize 4096 -validity 10000
```

Guarde pelo menos duas cópias de backup criptografadas em mídias/localizações independentes. Mantenha senhas em um gerenciador seguro, registre o fingerprint SHA-256 do certificado e teste a restauração do backup. A mesma keystore, alias e senha devem ser preservados para todas as atualizações; perder a chave impede assinar uma atualização compatível para os usuários existentes.

Configure os quatro valores em variáveis de ambiente (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) ou em um arquivo local `keystore.properties` ignorado pelo Git:

```properties
storeFile=C:/caminho-seguro/desinstalaja-release.jks
storePassword=...
keyAlias=desinstalaja-prod
keyPassword=...
```

Não coloque o arquivo de chave, senhas ou chaves privadas no repositório. A configuração atual usa a chave apenas quando os quatro valores estão preenchidos. Sem eles, `assembleRelease` pode compilar um APK **não assinado**, que serve somente para validação local e nunca deve ser copiado para o site.

Compile e verifique a assinatura usando o `apksigner` do Android SDK Build Tools:

```powershell
.\gradlew.bat :app:assembleRelease
& "$env:ANDROID_SDK_ROOT\build-tools\<versão>\apksigner.bat" verify --verbose --print-certs `
  .\app\build\outputs\apk\release\app-release.apk
Get-FileHash .\app\build\outputs\apk\release\app-release.apk -Algorithm SHA256
```

Para futuras versões, antes de publicar, guarde a senha em um gerenciador seguro, crie backups criptografados independentes da keystore, valide em aparelhos físicos AOSP, Samsung, Xiaomi e Motorola, verifique assinatura/certificado e confira versão/tamanho. Um emulador não substitui esses testes.

> [!warning] Exceção de publicação v1.2 — 17/11/2026
> Por autorização expressa do responsável, a versão 1.2 foi publicada apesar de ainda estarem pendentes os backups criptografados da keystore e a validação em aparelhos físicos. Essa exceção não substitui essas pendências nem comprova compatibilidade fora do AVD RustDroid Android 14/AOSP.

## APK oficial em distribuição

- **Versão**: 1.2 (versionCode 3), Android 8.0+.
- **Arquivo no site**: `web\public\downloads\desinstalaja-v1.2.apk`.
- **Tamanho**: 1.959.699 bytes (1,96 MB).
- **Assinatura**: chave própria de produção, esquema v2; certificado SHA-256 `a03e94c2eeb7439db5e9ac6326891cecd23a2d4595e5838a52a6dd61698ba741`.
- **Validação**: 28 testes unitários, lint e build Release passaram. A automação removeu o app descartável no AVD RustDroid Android 14/AOSP; validação física permanece pendente.
- **SHA-256 do APK**: `2fc05de7598ae2cabfce8497dcc86c87e8d7cb785b16f92a8db867464c8d8b09`.

O `targetSdk` permanece em 35; não foi alterado para requisitos de loja. A versão 1.2 (versionCode 3), com 1.959.699 bytes, está publicada em `web\public\downloads\desinstalaja-v1.2.apk`.

## Limitações de validação

Testes unitários verificam regras locais, não substituem testes do fluxo do instalador em aparelhos reais. O serviço restringe-se aos pacotes de instalador conhecidos de AOSP/Pixel, Xiaomi e Samsung; interfaces diferentes ou conteúdo inacessível exigirão confirmação manual. A validação em aparelhos físicos e os backups criptografados da keystore de produção permanecem pendentes apesar da publicação excepcional autorizada.
