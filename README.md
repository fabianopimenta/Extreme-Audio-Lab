# PMNT NOISE LAB — Android (offline)

Laboratório portátil de áudio experimental para Android, resultado da fusão entre o **HarshNoise Lab** (motor avançado, serviço de áudio, microfone, gravação, stream e presets) e o **Extreme Audio Lab v0.5.0** (Destruction Lab, Mod Matrix, Sub/Synth, 5 percussões e CI validado).

Package: `com.extremeaudiolab` · versão `0.5.0` (`versionCode 5`).

## O que ele faz

- **Motor nativo** C++20 + Oboe/AAudio, 48 kHz, 192 frames, Service em primeiro plano.
- **Fontes**: ruído (white/pink/brown/crackle/s&h) · microfone com feedback loop · **Sub Bass + Synth** (4 formas) · sequenciador de 16 passos com 6 vozes (Kick, Snare, Hat, Tom, Industrial, Noise Burst), pitch/velocity/probabilidade por passo.
- **Cadeia DSP**:
  `Mixer → Vocoder → Glitch → Granular → Modal Resonator → Hyper Fuzz → DOD Death Metal → Destruction Lab → Kaoss Chain → Breakdown duck → Respiração → Volume → Limiter transparente → Saturação opcional → saída/WAV/UDP`
- **Dinâmica intensa**: **Intensidade** (empurra 6 módulos de uma vez), **Pump** (impacto sincronizado com o BPM), respiração lenta, estalos abrasivos, saturação final ajustável e limitador transparente. Os faders alteram o nível de verdade em vez de serem achatados por compressão constante.
- **Mod Matrix**: LFO (sine/tri/square/S&H, 0,05–20 Hz) com destinos limitados a ruído, sub, synth e microfone.
- **Presets**: 3 patches em `app/src/main/assets/patches/default.json`: **PULSE DEMON / Parede** (parede de ruído branco, referência Merzbow), **CAOS CONTROLADO** (glitch/granular) e **DRONE SUJO** (sub + granular congelado), com disparo manual de **glitch** e **grãos**.
- **Amostra do usuário**: 3 texturas originais já embutidas (**Parede**, **Metal**, **Squeal** — sintetizadas pelo próprio app, sem áudio de terceiros) e carregamento de qualquer WAV do aparelho (até 20 s). A fonte entra antes do vocoder, glitch, granular, fuzz, death metal, destruição e Kaoss — ou seja, é filtrada e ressignificada por toda a cadeia.
- **Kaoss Pad virtual**: XY pad, 8 programas, latch (corrigido: arrastar nunca troca o efeito nem destrava) e mix.
- **Interface eurorack**: case com orelhas de madeira, trilhos metálicos e parafusos visíveis, painéis coloridos inspirados em Make Noise, Intellijel, Mutable, Buchla, Doepfer e Erica, faders com visor, escala e ajuda **?** em linguagem simples. Os 4 botões inferiores ficam acima da barra de navegação do Android.
- **Estabilidade**: nenhum comando da UI toca o DSP diretamente — tempo, Kaoss, disparos e reset passam por estados atômicos aplicados dentro do callback de áudio; saída com proteção contra NaN.
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
| Presets (4 patches, sem Transformers) + JSON validado | implementado |
| Amostra local (WAV até 20 s) na cadeia DSP | implementado (validar no CI) |
| CI GitHub Actions (4 ABIs) | ✅ validado — APK gerado em 24/11/2026 (runs #6, #8, #9, #10 e #12) |
| UI com ajuda integrada e faders logarítmicos | ✅ implementado |
| Visual eurorack + nome PMNT NOISE LAB | ✅ implementado (run #12) |
| Correção de fechamentos ao trocar preset/fader | ✅ implementado (validar no CI) |
| Build/execução em dispositivo | pendente (instalar o APK e testar) |

## Como usar em 30 segundos

1. Abra **Patch** → toque em **Presets** → escolha **PULSE DEMON / Parede** para o som mais extremo.
2. Baixe o volume do celular **antes** de testar.
3. Mexa em **Intensidade**, **Pump** e **Movimento** — são os faders que mais transformam o som. Se o som ficar agudo demais, baixe **Pump** e **Intensidade** antes de mexer em mais nada.
4. Em **Amostra / Sample**, toque em **Parede**, **Metal** ou **Squeal** para usar as texturas originais do app. Para usar um arquivo seu, toque em **Carregar amostra (WAV)** — só áudio que você tem direito de usar. Suba o **Mix da amostra** para ela atravessar a cadeia.
5. Toque no **?** ao lado de qualquer fader para ler o que ele faz em linguagem simples.
6. Em **Kaoss**, arraste o dedo; **Travar posição** congela o ponto e trocar o efeito mantém a escolha. Em **Seq**, toque nos quadrados para montar a batida (o Pump acompanha o BPM).

## Estrutura

```text
app/src/main/cpp/       AudioEngine, JniBridge, dsp/ (16 headers)
app/src/main/java/com/extremeaudiolab/
    audio/              AudioService, NativeEngine, Recorder, StreamReceiver
    model/              Patch, PresetRepository
    ui/                 PatchScreen, SequencerScreen, KaossScreen, StreamScreen
app/src/main/assets/    patches/default.json + samples/ (3 texturas originais)
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

Gain extremo e feedback podem danificar alto-falantes e causar desconforto auditivo. Comece com volume baixo, use o teto de segurança e evite realimentação prolongada. O limitador atua logo após o fader de volume; a saturação final vem depois dele e é opcional (fader "Saturação final"). O botão **KILL / SILÊNCIO** zera volume, microfone, intensidade, pump e efeitos.
