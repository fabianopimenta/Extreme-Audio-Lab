# Melhorias e próximos passos

## Concluído
- ✅ `GlitchProcessor` (stutter/reverse/freeze/decimate) + duck de breakdown.
- ✅ `ModalResonator` (6 modos inarmônicos) com excitação automática e manual.
- ✅ Presets de disparo na UI (glitch/grãos) e catálogo de presets genéricos: `PULSE DEMON / Parede`, `CAOS CONTROLADO` e `DRONE SUJO`. Os presets "Transformers" foram removidos a pedido do usuário.
- ✅ `GranularEngine` (scatter/freeze/reverse/pitch-drop, 24 vozes) e `Vocoder` (12 formantes, portadora ruído/osc) integrados à cadeia.
- ✅ Fusão com Extreme Audio Lab: `DestructionLab`, `ModMatrix`, `SubSynth`, 6 vozes de bateria, CI com 4 ABIs, kill switch e `resetDspState()`.
- ✅ CI validado em 24/11/2026: artefato `ExtremeAudioLab-debug-v0.5.0` gerado (run #6), com as 4 ABIs.
- ✅ Correções de build: `GlitchProcessor::rate_` (variável inexistente), `Vocoder::formants_.size()` (array C) e `android-actions/setup-android@v3` (pacote `tools` descontinuado) substituído por instalação direta via `sdkmanager`.
- ✅ Dinâmica sonora (run #8): LFO de respiração global, saturação final opcional, limitador transparente e volume antes do teto — os faders passaram a alterar o nível de verdade.
- ✅ UX: faders logarítmicos para frequência/tempo, valor sempre visível e botão **?** com explicação em linguagem simples em todos os controles; guia rápido no topo; tema visual laranja/escuro com cartões, medidor e Kaoss pad com grade.
- ✅ Identidade **PMNT NOISE LAB**: interface eurorack (case com trilhos/parafusos, painéis com cores Make Noise/Intellijel/Mutable/Buchla/Doepfer/Erica, visores e escalas nos faders).
- ✅ Dinâmica extrema: fader **Intensidade** (6 módulos de uma vez), **Pump** sincronizado ao BPM, estalos abrasivos estilo *Pulse Demon*, defaults mais agressivos e preset **PULSE DEMON / Parede**.
- ✅ Kaoss: latch confiável — arrastar usa sempre o estado atual, não reaplica programa antigo e não destrava o pad; travar congela x/y e trocar efeito preserva a escolha.
- ✅ Estabilidade (feedback do usuário 24/11, run #12): `setTempo`, `setKaoss`, disparos de glitch/grãos e reset param de escrever direto no DSP pela thread da UI. Tudo passa por `std::atomic` e é aplicado em `applyDeferred_()`, dentro do callback. Saída com `isfinite` + clamp para um NaN nunca chegar ao Oboe.
- ✅ `PULSE DEMON / Parede` reequilibrado: ruído branco contínuo no lugar do tipo "crackle" esparso, estalos menos frequentes, pump/saturação/volume reduzidos — parede cheia sem volume estourado.
- ✅ Presets "Transformers" **removidos** a pedido do usuário; o catálogo agora tem `PULSE DEMON / Parede`, `CAOS CONTROLADO` e `DRONE SUJO`.
- ✅ Amostra do usuário: player nativo (`Sampler.h`) recebe WAV local (PCM 8/16/24/32 e float), toca como fonte antes do vocoder/glitch/granular/fuzz/destruição/Kaoss, com mix, afinação (±24 st), ponto de início e loop. Nenhum áudio de terceiros é distribuído no APK.
- ✅ Três texturas harsh noise **originais** embutidas (`assets/samples/parede_01.wav`, `metal_01.wav`, `squeal_01.wav`), sintetizadas por `scripts/generate_samples.ps1` — sem rip de YouTube ou de álbuns comerciais — e acessíveis por botões na aba PATCH. O seletor de arquivos continua disponível para o WAV do próprio usuário.
- ✅ Case eurorack realmente visível: orelhas laterais de madeira com parafusos, trilhos de 24 dp e faixa laranja; a Scaffold deixa de cobrir a moldura.
- ✅ Barra inferior corrigida: `windowOptOutEdgeToEdgeEnforcement` + `navigationBarsPadding()` mantêm PATCH/SEQ/KAOSS/GRAVAR acima da navegação do Android 15 (targetSdk 35).

## Prioridade alta
1. **Validar em dispositivo real** (o APK já compila no CI): confirmar que os fechamentos sumiram ao trocar presets e mexer em faders, além de latência Oboe, permissões de microfone e estabilidade do Feedback/Destruction Lab.
2. **Identificar os módulos da foto**: preciso de uma imagem do rack em maior resolução/recorte dos painéis. O mapa funcional já está em `ANALYSIS.md`, mas os nomes de fabricante não foram legíveis.
3. **Segurança auditiva**: confirmar em dispositivo que o limitador atua com `micGain=0.95`, `masterGain=1.0`, `intensity=1` e saturação no máximo; o "panic button" (KILL) já zera volume, microfone, intensidade, pump e efeitos.
4. **Fallback de áudio**: se `AAudio` falhar, forçar `OpenSL ES`; se microfone exclusivo falhar, abrir em `SharingMode::Shared`.

## Funcionalidades
- **BLE MIDI / USB MIDI** para controlar patch e XY pad de um controlador externo.
- **Randomizador de patch** com restrições musicais (evita ganho > 0.9 no feedback sem limiter).
- **Morph entre presets** com crossfade de parâmetros.
- **Escalonamento por velocidade** do passo (pitch/velocity já existe; adicionar timbre por velocity).
- **Visualizador de espectro/osciloscópio** em tempo real na UI.
- **Sidechain**: bateria "ducando" o ruído/feedback para criar o pulsado da amostra.
- **RingMod/FreqShifter standalone** e **SubPreservLimiter** multibanda (0–200 Hz) para completar a cadeia de texturas metálicas.
- **Amostra por MediaStore/Compartilhar**: receber um áudio direto do "Compartilhar com" do Android, além do seletor de arquivos.

## Transmissão / exportação
- Trocar PCM por **Opus** (libopus) para reduzir banda de ~1,5 Mbps para ~96 kbps mantendo qualidade.
- Descoberta automática por **mDNS/Wi-Fi Direct** (hoje o IP é digitado manualmente).
- Modo **Receiver** com buffer de jitter e `AudioTrack` em baixa latência.
- Exportar **WAV 24-bit/48 kHz** opcional e **preset JSON** compartilhável.

## Robustez
- Testes unitários C++ para DSP (clipping, estabilidade do feedback, N/8 em filtros).
- Testes instrumentados para permissões e exportação via MediaStore.
- Tratamento de `onAudioReady` com `Disconnected`/`InvalidRate` reconectando o stream.
