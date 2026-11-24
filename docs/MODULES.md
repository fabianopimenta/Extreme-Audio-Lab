# Módulos do APP

Cada módulo expõe parâmetros controláveis pela UI e pelo preset JSON.

## Fontes

### NoiseSource
- `type` — white / pink / brown / crackle / sample&hold
- `level` — ganho de saída
- `crush` — bit reduction (2–16 bits)
- `hold` — taxa de sample&hold (Hz)
- `comb` — ressonância metálica (0–1)

### MicFeedback
- `gain` — ganho de realimentação (0–0.95; limitado)
- `delayMs` — atraso da loop (1–200 ms)
- `tone` — filtro passa-faixa na loop
- `mix` — quantidade do sinal do microfone
- `gate` — supressão de ruído de fundo

### DrumSequencer
- `bpm`, `swing`, `steps` (16)
- por passo: `on`, `voice` (0 kick, 1 snare, 2 hat, 3 tom, 4 industrial hit, 5 noise burst), `pitch` (semitons), `velocity`, `probability`

### SubSynth (do Extreme Audio Lab)
- `sub.frequency` (10–200 Hz), `sub.level` (0–0.4), `sub.wave` (0 sine, 1 tri, 2 square, 3 saw)
- `synth.frequency` (30–1200 Hz), `synth.level` (0–0.3), `synth.wave`
- Níveis default `0` — o patch da amostra não muda até você ativar

### ModMatrix (do Extreme Audio Lab)
- `enabled`, `rate` (0,05–20 Hz), `depth` (0–1)
- `shape` — 0 sine, 1 triangle, 2 square, 3 sample & hold
- `destination` — 0 ruído, 1 sub, 2 synth, 3 microfone
- Escalas limitadas a `0..2×` para evitar picos

### DestructionLab (do Extreme Audio Lab)
- `mode` — 0 bypass, 1 hard clip, 2 asymmetric clip, 3 wavefold, 4 bitcrush + S&H, 5 gate
- `drive` (1–16), `wet` (0–1)
- Inserido entre `DODDeathMetal` e `KaossChain`; recomenda-se começar com `wet` baixo

## Efeitos

### HyperFuzz
- `drive`, `octave`, `gate`, `tone`, `mix`

### DODDeathMetal
- `gain`, `lowCut`, `scoop`, `presence`, `gate`

### GlitchProcessor (robótico / breakdowns)
- `mode` — 0 stutter, 1 reverse, 2 freeze, 3 decimate
- `rate` — clock de disparo (0,5–40 Hz)
- `size` — tamanho do slice em ms (10–400)
- `decimate` — fator de sample&hold (1–64)
- `bits` — redução de bits (2–16)
- `probability` — chance de disparo por tick
- `mix` — quantidade de glitch
- `gate` — profundidade do duck de breakdown
- `trigger()` — disparo manual pela UI

### ModalResonator (clanks metálicos)
- `tune` — frequência fundamental (40–2000 Hz)
- `inharmonicity` — desvio inarmônico dos 6 modos
- `decay` — ressonância/Q dos modos
- `brightness` — amortecimento dependente da frequência
- `mix` — quantidade do ressonador
- `excite()` — injeta energia nos modos

### GranularEngine (texturas de fita / transformação)
- `mode` — 0 scatter, 1 freeze, 2 reverse, 3 pitchDrop (tape stop)
- `size` — duração do grão em ms (20–800)
- `density` — grãos por segundo (1–80)
- `pitch` — transposição em semitons (-24 a +24)
- `spread` — dispersão das posições de leitura no histórico
- `feedback` — realimentação do granular no buffer (0–0.85)
- `mix` — quantidade de grãos no sinal
- `trigger()` — rajada manual de grãos pela UI

### Vocoder / FormantBank (voz robótica)
- 12 bandas de formantes (180 Hz a 6,2 kHz)
- `source` — 0 ruído, 1 oscilador interno, 2 ambos
- `pitch` — frequência do oscilador portador (40–1200 Hz)
- `shift` — transposição dos formantes em semitons (-24 a +24)
- `speed` — velocidade dos envelopes (ataque/release)
- `q` — seletividade das bandas
- `mix` — blend do sinal vocodado com o mix original (no `AudioEngine`)
- Modulador = microfone; portadora = ruído e/ou oscilador

### KaossChain (8 programas)
1. Filter + Delay
2. Phaser + Reverb
3. Flanger + Slicer
4. Chorus + Delay
5. Ring Mod + Filter
6. Pitch Shift + Reverb
7. Tremolo + Distortion
8. Vinyl Sim + Gate

Parâmetros globais do pad: `x`, `y`, `latch`, `program`, `mix`.

## Master

### Limiter
- `threshold` (dB), `gain` (master), `meter` (RMS + redução de ganho)
