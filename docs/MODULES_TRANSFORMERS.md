# Módulos para sonoridade "Transformers" (robótica, breakdowns e glitch)

Derivado da análise da amostra: 70% da energia em 0–200 Hz, flatness 0,034 (tonal),
fluxo espectral 0,26 (muitos ataques/quedas), 0% de clipping. Ou seja: **não é ruído
aleatório — é uma portadora tonal sendo rasgada por artefatos digitais e modulação**.

## Módulos sugeridos (ordem de prioridade no APP)

| # | Módulo no APP | Função | Por quê | Equivalente Eurorack (referência) |
|---|---|---|---|---|
| 1 | `GlitchProcessor` | stutter, reverse, pitch-freeze, bit/sample-rate reduction | cria os "engasgos" e breakdowns da amostra | Make Noise **Data Bender**, **2hp Glitch** |
| 2 | `RingMod / FreqShifter` | multiplicação por portadora, deslocamento de frequência | dá o timbre metálico/robótico de Transformer | usar o ring mod do Kaoss, mas standalone |
| 3 | `ModalResonator` | ressonadores modais (sino/metal) | clanks, serviços, "transformação" metálica | Mutable Instruments **Rings** |
| 4 | `GranularEngine` | grãos, freeze, reverse, scatter | glitches texturais e quedas de fita | Mutable **Clouds**, Make Noise **Morphagene** |
| 5 | `Vocoder / FormantBank` | banca de formantes com carrier | "voz" de robô falando | Doepfer **A-129** vocoder, formantes |
| 6 | `StochasticClock + Burst` | clock divider + gates probabilísticos + rajadas | gera breakdowns rítmicos imprevisíveis | Make Noise **Wogglebug**, **Maths**, 4ms **RCD**, ALM **Pamela's**, Intellijel **Shifty**, Befaco **Muxlicer** |
| 7 | `Bitcrusher / S&H` | decimação, jitter, sample&hold | aspereza digital e "dados corrompidos" | Doepfer **A-112**, 2hp Glitch |
| 8 | `BreakdownGate` | envelope/slew que mute ou duck canais | controla a queda e a volta da cadeia | Make Noise **Maths** (função), VCA + envelope |
| 9 | `PitchDrop / TapeStop` | pitch glide descendente | clássico "transformando/se desligando" | Data Bender, Clouds, delay com modulação |
| 10 | `SubPreservLimiter` | multibanda: protege 0–200 Hz | mantém os 70% de grave sem clipping | limiter atual + compressão de bandas |

## Cadeia recomendada

```text
[Mic / Osc / Ruído]
        │
        ├─> [RingMod / FreqShifter] ─> [Vocoder / Formantes]
        │                                  │
        └─> [Bitcrush + S&H] <─────────────┘
                     │
        [StochasticClock + Burst] ──> [Bateria com pitch]
                     │
        [GlitchProcessor: stutter/reverse/freeze]
                     │
        [ModalResonator: clanks metálicos]
                     │
        [HyperFuzz] ─> [DOD Death Metal] ─> [Kaoss Chain]
                     │
        [BreakdownGate / Sidechain duck]
                     │
        [SubPreservLimiter] ─> saída / WAV / UDP
```

Cadeia **implementada** nesta versão (subconjunto acima):
`Mixer → Vocoder → GlitchProcessor → GranularEngine → ModalResonator → HyperFuzz → DODDeathMetal → KaossChain → Breakdown duck → Limiter`.

## Presets sugeridos

- **Prime (herói)**: vocoder + formantes suaves, ring mod sutil, fuzz controlado, delay curto.
- **Starscream (vilão agudo)**: ring mod em ~2–4 kHz + pitch shifter + granular squeal + outs.
- **Transformation**: sweep de ruído → ressonador modal → stutter com pitch drop → impacto de bateria com pitch.
- **Breakdown / Corrupted data**: clock aleatório + burst gates + bitcrush agressivo + mute intermitente.
- **Servo / Motor**: ressonador modal em banda estreita + tremolo rápido + feedback de microfone curto.

## Implementação no APP

### Concluído

1. ✅ `dsp/GlitchProcessor.h` — ring buffer de 2 s com os 4 modos (stutter, reverse, freeze, decimate), clock com probabilidade, crossfade anti-click e envelope `activity()` que ducka a cadeia (breakdown).
2. ✅ `dsp/ModalResonator.h` — banco de 6 ressonadores modais com razões inarmônicas, Q/brightness ajustáveis e excitação por transiente + `excite()` manual.
3. ✅ `dsp/GranularEngine.h` — 24 vozes, ring buffer de 3 s, janelas Hann, modos scatter/freeze/reverse/pitchDrop, escalonador de densidade, spread, feedback e rajada manual.
4. ✅ `dsp/Vocoder.h` — 12 formantes com envelopes de ataque/release, portadora de ruído/osc/ambos e transposição de formantes; modulador é o microfone.
5. ✅ Integração em `AudioEngine`: `Mixer → Vocoder → Glitch → Granular → Modal → Fuzz → DOD → Kaoss → Duck → Limiter`.
6. ✅ Parâmetros espelhados em `Patch.kt` / `AudioController.kt` / `PresetRepository.kt` / `JniBridge.cpp` e controles na aba *Patch* (seletor **Presets**, **Disparar glitch** e **Disparar grãos**). O carregador usa `org.json` (sem novas dependências).
7. ✅ Presets em `app/src/main/assets/patches/default.json`: Prime, Starscream, Transformation, Corrupted Data/Breakdown e Servo/Motor — agora com valores próprios de granular/vocoder.

### Ainda a implementar

8. Extensão de `DrumSequencer` — `burst` e `clockDiv` (a probabilidade por passo já existe).
9. `RingMod / FreqShifter` standalone (hoje disponível via programa 5 do Kaoss).
10. `SubPreservLimiter` multibanda para proteger 0–200 Hz.

Ver `ROADMAP.md` para integração com MIDI e sidechain.
