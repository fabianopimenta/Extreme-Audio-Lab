# Análise dos anexos

Análise feita **localmente** na máquina do usuário (OCR Tesseract + Python/librosa). Nenhum arquivo foi enviado para serviços externos.

## 1. Imagem — identificação dos módulos

Arquivo: `WhatsApp Image 2026-11-20 at 04.04.24.jpeg` — 610×1356, captura de tela do Instagram.

**Resultado: não foi possível identificar os rótulos dos módulos com confiança.**

Tentativas realizadas:
- OCR nativo do Windows (pt-BR);
- Tesseract 5.4 (eng), 3 escalas (3×/4×/6×), 3 limiares, PSM 6/11/12;
- recorte da área do equipamento e filtro por confiança > 40.

O OCR só recuperou texto da interface do Instagram (`simonlitta`, `Seguir`, `4:04`, `I'm making an instrument for performing g...`, `Curtido por...`). Os rótulos dos módulos no rack estão abaixo da resolução necessária e/ou em ângulo, produzindo apenas ruído de OCR.

**Para identificação exata**, preciso de uma foto do rack em resolução maior (ou um recorte dos painéis frontais). Enquanto isso, o projeto adota a topologia funcional descrita na legenda e no pedido:

```text
fonte de ruído / feedback de microfone / bateria sequenciada com pitch
   → Hyper Fuzz → DOD Death Metal → Kaoss Pad (cadeia completa) → limiter
```

### Módulos mapeados para o APP (funcionais, não nomes de fabricante)

| Função na imagem/pedido | Equivalente no APP | Arquivo |
|---|---|---|
| Instrumento de performance / ruído | `NoiseGenerator` (white/pink/brown/crackle/S&H + bitcrush + comb) | `dsp/NoiseGenerator.h` |
| Feedback de microfone | `MicFeedback` (delay + BP + ganho limitado + gate) | `dsp/MicFeedback.h` |
| Bateria sequenciada com pitch | `DrumSequencer` (16 passos, 4 vozes, pitch/velocity/prob) | `dsp/DrumSequencer.h` |
| Hyper fuzz | `HyperFuzz` (octave + clipping assimétrico + gate + tone) | `dsp/HyperFuzz.h` |
| DOD Death Metal Distortion | `DODDeathMetal` (high gain + low cut + mid scoop + presence + gate) | `dsp/DodDeathMetal.h` |
| Korg Kaoss Pad (cadeia completa) | `KaossChain` (8 programas: filtro, phaser, flanger, chorus, ring, pitch, tremolo, vinyl + delay/reverb/slicer) | `dsp/KaossChain.h` |
| Saída segura | `Limiter` + `DCBlock` | `dsp/Limiter.h` |

## 2. Áudio — sonoridade da amostra

Arquivo: `WhatsApp Audio 2026-11-20 at 03.23.41.mpeg` — 441 KB.

| Métrica | Valor | Interpretação |
|---|---|---|
| Duração | 27,495 s | amostra de performance contínua |
| Sample rate / canais | 44,1 kHz / 2 | estéreo |
| RMS | 0,146 | nível alto, sem clipping |
| Peak | 0,696 | headroom de ~3,2 dB |
| Crest factor | 13,54 dB | dinâmica moderada (não é ruído branco achatado) |
| Clipping | 0,00 % | master limpo, provavelmente limitado |
| ZCR | 0,0496 | sinal predominantemente tonal/baixo |
| Spectral centroid | 3489 Hz (mediana 2953 Hz) | perceptually.brilhante, apesar da energia em baixa |
| Spectral flatness | 0,034 | **baixo** → tonal, não ruído estocástico puro |
| Spectral flux | 0,258 | muita variação/ataques → sequenciamento e instabilidade |
| Atividade | 78 % | quase contínuo com pausas curtas |
| Envelope dB (p10/p50/p90) | −38,7 / −20,2 / −14,2 | compressão forte, variação ~24 dB |

### Distribuição de energia por banda (potência)

| Banda | % da energia |
|---|---|
| 0–200 Hz | **70,0 %** |
| 200 Hz–2 kHz | 24,7 % |
| 2–8 kHz | 4,3 % |
| 8–16 kHz | 1,0 % |
| 16 kHz+ | 0,01 % |

Roll-off 95 % em ~2 kHz. Ou seja: **a base é grave/distorcida** (kick/feedback/fuzz), com uma camada fina de agudos (hash, hiss, crackle) que eleva o centroide percebido.

### Descriçãosonora derivada

Harsh noise **tonal e pulsado**, com forte presença de grave saturado, realimentação instável e textura metálica/industrial. Não é "white noise" puro: há pitch, envelope e modulação. Isso casa com a cadeia pedida (fuzz + death metal distortion + Kaoss Pad), que geraharmônicos e ressonâncias em vez de ruído aleatório.

## 3. Decisões de design derivadas

- Gerador de ruído com **pink/brown + crackle + comb** como cama, não white puro.
- Bateria com pitch agudo e ataque curto para os picos de fluxo espectral.
- Feedback de microfone com delay curto (10–40 ms) e BP em ~1–3 kHz para os howls.
- Hyper Fuzz com octave-up e gate para as bordas ásperas.
- DOD com low-cut e mid-scoop para preservar os 70 % de energia em 0–200 Hz sem lama.
- Kaoss Chain com delay/reverb curtos e slicer para a instabilidade dos 78 % de atividade.
- Limiter final obrigatório: a amostra tem 0 % de clipping, então o APP deve manter isso.
