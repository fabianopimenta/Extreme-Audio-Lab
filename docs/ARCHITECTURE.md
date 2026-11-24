# Arquitetura

## Visão geral

```text
[Noise] ──┐
[Mic] ────┤
[Drums] ──┼─> [Mixer] ─> [Vocoder*] ─> [Glitch] ─> [Granular] ─> [Modal] ─> [HyperFuzz] ─> [DOD]
[Sub] ────┤                                                                                       │
[Synth] ──┤                                                                                       v
[Sample] ─┘
                         [ModMatrix] ──(ruído/sub/synth/mic)──> [DestructionLab] ─> [KaossChain]
                                                                                        │
                                                                   [Breakdown duck] <──┘
                                                                           │
                                                                      [Crackle] ─> [Respiração LFO + Pump BPM] ─> [Volume] ─> [Limiter transparente] ─> [Saturação opcional] ─> ┬─> Oboe out
                                                                                   ├─> WAV writer
                                                                                   └─> UDP sender

* Vocoder: modulador = Mic, portadora = Noise/osc interno; blend controlado por `vocoderMix`.
* Sample: WAV local carregado pelo usuário; entra no mesmo mixer antes de toda a cadeia.
```

- **Controle**: Kotlin/Compose envia parâmetros via JNI para `AudioEngine`.
- **Áudio**: callback do Oboe chama `AudioEngine::render(float* out, int frames, int channels)` na thread de áudio.
- **Estado**: um `Patch` imutável no Kotlin é espelhado por setters atômicos no C++ (`std::atomic<float>` por parâmetro), sem locks no callback. Comandos com estado interno (tempo, Kaoss, triggers e reset) também entram por atomics e são aplicados por `applyDeferred_()` **dentro** da thread de áudio — a UI nunca toca nos módulos DSP diretamente.
- **Novos módulos do Extreme Audio Lab**:
  - `DestructionLab`: 6 algoritmos de destruição (bypass, hard clip, assimétrico, wavefold, bitcrush/S&H, gate) com drive e wet/dry, inserido entre DOD e Kaoss.
  - `ModMatrix`: LFO com 4 formas e 4 destinos (ruído, sub, synth, microfone), com escalas limitadas.
  - `SubSynth`: Sub Bass (10–200 Hz) e Synth (30–1200 Hz), 4 formas de onda, nível default 0 para preservar o patch da amostra.
  - `DrumSequencer`: 6 vozes (kick, snare, hat, tom, industrial hit, noise burst).
  - `Sampler`: reproduz um WAV do usuário como fonte, com mix, afinação (±24 st), ponto de início e loop. O arquivo é lido no Kotlin (`WavImporter`, PCM 8/16/24/32 e float, até 20 s) e entregue ao C++ por `nativeLoadSample`; a troca de buffer acontece no callback com `try_lock`, e o player nunca aloca na thread de áudio.
- **Reset de estado**: `AudioEngine::resetDspState()` apenas sinaliza `resetDspFlag` em `nativeStart`; o reset real de LFO, voices e ring buffer do microfone acontece no primeiro `render()`, na thread de áudio.
- **Kaoss**: `latch` corrigido nas duas camadas — a UI congela x/y e usa sempre o estado atual (sem closure velho), e o C++ segue os valores recebidos. Arrastar nunca troca o programa nem destrava o pad.
- **Gravação/stream**: a thread de áudio escreve no `WavWriter` e no `UdpSender` pré-alocados, sem alocação por amostra.

## Latência

- `framesPerBuffer` alvo: 96–192 @ 48 kHz (2–4 ms) quando o dispositivo suportar.
- Oboe usa `AAudio` com `PerformanceMode::LowLatency` e faz fallback para OpenSL ES se necessário.
- Nenhuma alocação na thread de áudio: todos os buffers são pré-alocados.

## Segurança

- Sem dependências de rede externa em runtime.
- `INTERNET` apenas para `DatagramSocket` local (Wi-Fi Direct/hotspot).
- Limitador transparente após o volume; saturação final opcional; ganho de feedback limitado por software. O botão KILL zera volume, microfone, intensidade, pump e efeitos.
- CI GitHub Actions compila as 4 ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).

## Permissões

| Permissão | Motivo |
|---|---|
| `RECORD_AUDIO` | feedback loop de microfone |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MICROPHONE` | captura contínua |
| `POST_NOTIFICATIONS` | notificação do serviço (Android 13+) |
| `INTERNET` | sockets UDP locais para streaming P2P |
| `NEARBY_WIFI_DEVICES` / `ACCESS_FINE_LOCATION` | Wi-Fi Direct (Android 13+ / legado) |
| `WRITE_EXTERNAL_STORAGE` (maxSdk 28) | compatibilidade de exportação |
