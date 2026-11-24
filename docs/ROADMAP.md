# Melhorias e próximos passos

## Concluído
- ✅ `GlitchProcessor` (stutter/reverse/freeze/decimate) + duck de breakdown.
- ✅ `ModalResonator` (6 modos inarmônicos) com excitação automática e manual.
- ✅ Presets Transformers (Prime, Starscream, Transformation, Corrupted Data, Servo) e botões de disparo na UI.
- ✅ `GranularEngine` (scatter/freeze/reverse/pitch-drop, 24 vozes) e `Vocoder` (12 formantes, portadora ruído/osc) integrados à cadeia.
- ✅ Fusão com Extreme Audio Lab: `DestructionLab`, `ModMatrix`, `SubSynth`, 6 vozes de bateria, CI com 4 ABIs, kill switch e `resetDspState()`.
- ✅ CI validado em 24/11/2026: artefato `ExtremeAudioLab-debug-v0.5.0` gerado (run #6), com as 4 ABIs.
- ✅ Correções de build: `GlitchProcessor::rate_` (variável inexistente), `Vocoder::formants_.size()` (array C) e `android-actions/setup-android@v3` (pacote `tools` descontinuado) substituído por instalação direta via `sdkmanager`.
- ✅ Dinâmica sonora (run #8): LFO de respiração global, saturação final opcional, limitador transparente e volume antes do teto — os faders passaram a alterar o nível de verdade.
- ✅ UX: faders logarítmicos para frequência/tempo, valor sempre visível e botão **?** com explicação em linguagem simples em todos os controles; guia rápido no topo; tema visual laranja/escuro com cartões, medidor e Kaoss pad com grade.

## Prioridade alta
1. **Validar em dispositivo real** (o APK já compila no CI): latência Oboe, permissões de microfone e estabilidade do Feedback/Destruction Lab.
2. **Identificar os módulos da foto**: preciso de uma imagem do rack em maior resolução/recorte dos painéis. O mapa funcional já está em `ANALYSIS.md`, mas os nomes de fabricante não foram legíveis.
3. **Segurança auditiva**: confirmar em dispositivo que o limitador atua com `micGain=0.95`, `masterGain=1.0` e saturação no máximo; o "panic button" (KILL) já zera volume, microfone e efeitos.
4. **Fallback de áudio**: se `AAudio` falhar, forçar `OpenSL ES`; se microfone exclusivo falhar, abrir em `SharingMode::Shared`.

## Funcionalidades
- **BLE MIDI / USB MIDI** para controlar patch e XY pad de um controlador externo.
- **Randomizador de patch** com restrições musicais (evita ganho > 0.9 no feedback sem limiter).
- **Morph entre presets** com crossfade de parâmetros.
- **Escalonamento por velocidade** do passo (pitch/velocity já existe; adicionar timbre por velocity).
- **Visualizador de espectro/osciloscópio** em tempo real na UI.
- **Sidechain**: bateria "ducando" o ruído/feedback para criar o pulsado da amostra.
- **RingMod/FreqShifter standalone** e **SubPreservLimiter** multibanda (0–200 Hz) para completar a cadeia Transformers.

## Transmissão / exportação
- Trocar PCM por **Opus** (libopus) para reduzir banda de ~1,5 Mbps para ~96 kbps mantendo qualidade.
- Descoberta automática por **mDNS/Wi-Fi Direct** (hoje o IP é digitado manualmente).
- Modo **Receiver** com buffer de jitter e `AudioTrack` em baixa latência.
- Exportar **WAV 24-bit/48 kHz** opcional e **preset JSON** compartilhável.

## Robustez
- Testes unitários C++ para DSP (clipping, estabilidade do feedback, N/8 em filtros).
- Testes instrumentados para permissões e exportação via MediaStore.
- Tratamento de `onAudioReady` com `Disconnected`/`InvalidRate` reconectando o stream.
