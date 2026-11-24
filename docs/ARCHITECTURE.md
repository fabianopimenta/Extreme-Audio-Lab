# Arquitetura

## Visão geral

```text
[Noise] ──┐
[Mic] ────┤
[Drums] ──┼─> [Mixer] ─> [Vocoder*] ─> [Glitch] ─> [Granular] ─> [Modal] ─> [HyperFuzz] ─> [DOD]
[Sub] ────┤                                                                                       │
[Synth] ──┘                                                                                       v
                         [ModMatrix] ──(ruído/sub/synth/mic)──> [DestructionLab] ─> [KaossChain]
                                                                                        │
                                                                   [Breakdown duck] <──┘
                                                                           │
                                                                      [Limiter] ─> ┬─> Oboe out
                                                                                   ├─> WAV writer
                                                                                   └─> UDP sender

* Vocoder: modulador = Mic, portadora = Noise/osc interno; blend controlado por `vocoderMix`.
```

- **Controle**: Kotlin/Compose envia parâmetros via JNI para `AudioEngine`.
- **Áudio**: callback do Oboe chama `AudioEngine::render(float* out, int frames, int channels)` na thread de áudio.
- **Estado**: um `Patch` imutável no Kotlin é espelhado por setters atômicos no C++ (`std::atomic<float>` por parâmetro), sem locks no callback.
- **Novos módulos do Extreme Audio Lab**:
  - `DestructionLab`: 6 algoritmos de destruição (bypass, hard clip, assimétrico, wavefold, bitcrush/S&H, gate) com drive e wet/dry, inserido entre DOD e Kaoss.
  - `ModMatrix`: LFO com 4 formas e 4 destinos (ruído, sub, synth, microfone), com escalas limitadas.
  - `SubSynth`: Sub Bass (10–200 Hz) e Synth (30–1200 Hz), 4 formas de onda, nível default 0 para preservar o patch da amostra.
  - `DrumSequencer`: 6 vozes (kick, snare, hat, tom, industrial hit, noise burst).
- **Reset de estado**: `AudioEngine::resetDspState()` é chamado em `nativeStart` para zerar LFO, voices e ring buffer do microfone.
- **Kaoss**: `latch` corrigido — o valor recebido é agora aplicado ao estado do pad.
- **Gravação/stream**: a thread de áudio escreve no `WavWriter` e no `UdpSender` pré-alocados, sem alocação por amostra.

## Latência

- `framesPerBuffer` alvo: 96–192 @ 48 kHz (2–4 ms) quando o dispositivo suportar.
- Oboe usa `AAudio` com `PerformanceMode::LowLatency` e faz fallback para OpenSL ES se necessário.
- Nenhuma alocação na thread de áudio: todos os buffers são pré-alocados.

## Segurança

- Sem dependências de rede externa em runtime.
- `INTERNET` apenas para `DatagramSocket` local (Wi-Fi Direct/hotspot).
- Limiter obrigatório no fim da cadeia; ganho de feedback limitado por software.
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
