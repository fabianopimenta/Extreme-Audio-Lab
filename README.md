# EXTREME AUDIO LAB — Android (offline)

Laboratório portátil de áudio experimental para Android, resultado da fusão entre o **HarshNoise Lab** (motor avançado, serviço de áudio, microfone, gravação, stream e presets) e o **Extreme Audio Lab v0.5.0** (Destruction Lab, Mod Matrix, Sub/Synth, 5 percussões e CI validado).

Package: `com.extremeaudiolab` · versão `0.5.0` (`versionCode 5`).

## O que ele faz

- **Motor nativo** C++20 + Oboe/AAudio, 48 kHz, 192 frames, Service em primeiro plano.
- **Fontes**: ruído (white/pink/brown/crackle/s&h) · microfone com feedback loop · **Sub Bass + Synth** (4 formas) · sequenciador de 16 passos com 6 vozes (Kick, Snare, Hat, Tom, Industrial, Noise Burst), pitch/velocity/probabilidade por passo.
- **Cadeia DSP**:
  `Mixer → Vocoder → Glitch → Granular → Modal Resonator → Hyper Fuzz → DOD Death Metal → Destruction Lab → Kaoss Chain → Breakdown duck → Limiter → saída/WAV/UDP`
- **Mod Matrix**: LFO (sine/tri/square/S&H, 0,05–20 Hz) com destinos limitados a ruído, sub, synth e microfone.
- **Sonoridade Transformers**: 5 presets em `app/src/main/assets/patches/default.json`, com disparo manual de **glitch** e **grãos**.
- **Kaoss Pad virtual**: XY pad, 8 programas, latch (corrigido) e mix.
- **Gravação** WAV PCM 16-bit com exportação via MediaStore/Compartilhar.
- **Transmissão offline** Android → Android por UDP (Wi-Fi Direct/hotspot), com modo Receiver no mesmo APK; também compartilhamento do WAV.
- **100% offline em runtime**: sem analytics, ads ou chamadas externas. `INTERNET`/Wi-Fi são usados apenas para sockets locais.

## Status

| Item | Estado |
|---|---|
| Análise do áudio anexo | concluída |
| Identificação dos módulos da foto | pendente (OCR só leu a UI do Instagram) |
| DSP nativo (todos os módulos acima) | implementado |
| UI Compose (Patch, Seq, Kaoss, Stream) | implementado |
| Presets (6 patches) + JSON validado | implementado |
| CI GitHub Actions (4 ABIs) | implementado — aguardando primeira execução |
| Build/execução em dispositivo | pendente |

## Estrutura

```text
app/src/main/cpp/       AudioEngine, JniBridge, dsp/ (16 headers)
app/src/main/java/com/extremeaudiolab/
    audio/              AudioService, NativeEngine, Recorder, StreamReceiver
    model/              Patch, PresetRepository
    ui/                 PatchScreen, SequencerScreen, KaossScreen, StreamScreen
app/src/main/assets/    patches/default.json
.github/workflows/      android-debug.yml (build remoto)
docs/                   análise, arquitetura, módulos, build, roadmap
scripts/                fetch_deps.ps1 (clona Oboe em tempo de build)
```

## Como compilar

Ver [`docs/BUILD.md`](docs/BUILD.md). Resumo:

```powershell
.\scripts\fetch_deps.ps1
```

Depois: abra a pasta no Android Studio e use **Build → Make Project**, ou envie para o GitHub e acompanhe **Actions → Android Debug APK** (o workflow clona o Oboe automaticamente).

## Aviso

Gain extremo e feedback podem danificar alto-falantes e causar desconforto auditivo. Comece com volume baixo, use o limiter e evite realimentação prolongada. O limiter está sempre no fim da cadeia.
