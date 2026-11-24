# EXTREME AUDIO LAB — Android (offline)

Laboratório portátil de áudio experimental para Android, resultado da fusão entre o **HarshNoise Lab** (motor avançado, serviço de áudio, microfone, gravação, stream e presets) e o **Extreme Audio Lab v0.5.0** (Destruction Lab, Mod Matrix, Sub/Synth, 5 percussões e CI validado).

Package: `com.extremeaudiolab` · versão `0.5.0` (`versionCode 5`).

## O que ele faz

- **Motor nativo** C++20 + Oboe/AAudio, 48 kHz, 192 frames, Service em primeiro plano.
- **Fontes**: ruído (white/pink/brown/crackle/s&h) · microfone com feedback loop · **Sub Bass + Synth** (4 formas) · sequenciador de 16 passos com 6 vozes (Kick, Snare, Hat, Tom, Industrial, Noise Burst), pitch/velocity/probabilidade por passo.
- **Cadeia DSP**:
  `Mixer → Vocoder → Glitch → Granular → Modal Resonator → Hyper Fuzz → DOD Death Metal → Destruction Lab → Kaoss Chain → Breakdown duck → Respiração → Volume → Limiter transparente → Saturação opcional → saída/WAV/UDP`
- **Dinâmica (v0.5.1)**: movimento lento automático (respiração), saturação final ajustável e limitador transparente — o volume e os faders agora alteram o nível de verdade em vez de serem achatados por compressão constante.
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
| CI GitHub Actions (4 ABIs) | ✅ validado — APK gerado em 24/11/2026 (runs #6 e #8) |
| UI com ajuda integrada e faders logarítmicos | ✅ implementado |
| Build/execução em dispositivo | pendente (instalar o APK e testar) |

## Como usar em 30 segundos

1. Abra **Patch** → toque em **Presets** e escolha um som pronto.
2. Baixe o volume do celular **antes** de testar.
3. Mexa em **Movimento** e **Nível do ruído** — são os faders que mais mudam o som.
4. Toque no **?** ao lado de qualquer fader para ler o que ele faz em linguagem simples.
5. O botão **?** no topo abre o guia rápido. Em **Kaoss**, arraste o dedo pela área; em **Seq**, toque nos quadrados para montar a batida.

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

Gain extremo e feedback podem danificar alto-falantes e causar desconforto auditivo. Comece com volume baixo, use o teto de segurança e evite realimentação prolongada. O limitador atua logo após o fader de volume; a saturação final vem depois dele e é opcional (fader "Saturação final"). O botão **KILL / SILÊNCIO** zera volume, microfone e efeitos.
