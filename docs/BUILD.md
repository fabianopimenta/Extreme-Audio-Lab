# Build — EXTREME AUDIO LAB 0.5.0

## Requisitos

- Android Studio (Ladybug ou superior) com **SDK 35**, **NDK 27.0.12077973** e **CMake 3.22.1**.
- JDK 17.
- Git (para baixar o Oboe).

## Dependências nativas

```powershell
.\scripts\fetch_deps.ps1
```

Isso clona o Oboe em `app/src/main/cpp/third_party/oboe`. É uma etapa apenas de build; o app continua 100% offline em runtime.

## Opção A — Android Studio (recomendado nesta máquina)

1. **File → Open** na pasta do projeto.
2. Aguarde o Gradle Sync.
3. **Build → Make Project** (`Ctrl+F9`).
4. **Run** em dispositivo real com deprecated USB ativado.

> O projeto não tem `gradlew`. Use o Gradle embutido do Android Studio ou o CI.

## Opção B — GitHub Actions

1. Crie um repositório e envie o conteúdo desta pasta para a raiz (inclua a pasta oculta `.github`).
2. Abra **Actions → Android Debug APK**.
3. O workflow instala SDK/NDK/CMake, clona o Oboe e compila as 4 ABIs.
4. Baixe o artefato `ExtremeAudioLab-debug-v0.5.0` e instale o `app-debug.apk`.

## Executar e testar

1. Conceda permissão de microfone e notificações.
2. Abra a aba **Patch** e escolha um preset. Para ouvir Sub/Synth/Destruction Lab, ajuste seus níveis/wet (default 0).
3. Abra **Seq** e experimente as vozes **Industrial** e **Burst**.
4. Ajuste o limiter antes de aumentar o ganho.
5. Para transmitir: **Stream** → informe IP/porta → **Start stream**.
6. No segundo dispositivo, instale o mesmo APK e use **Modo Receiver neste dispositivo** (o antigo botão "Listen" não existe mais).

## Troubleshooting

| Sintoma | Ação |
|---|---|
| `Could not find oboe/CMakeLists.txt` | rode `fetch_deps.ps1` e **Build → Refresh Linked C++ Projects** |
| `Plugin not found` | desmarque **Gradle → Offline work** e sincronize com internet |
| `SecurityException` ao iniciar | conceda `RECORD_AUDIO` manualmente e reabra o app |
| Erro em C++ | cole o trecho do painel **Build** para correção |
