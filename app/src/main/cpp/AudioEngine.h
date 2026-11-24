#pragma once
#include <array>
#include <atomic>
#include <mutex>
#include <vector>
#include <string>
#include "Utils.h"
#include "NoiseGenerator.h"
#include "MicFeedback.h"
#include "DrumSequencer.h"
#include "HyperFuzz.h"
#include "DodDeathMetal.h"
#include "KaossChain.h"
#include "GlitchProcessor.h"
#include "ModalResonator.h"
#include "GranularEngine.h"
#include "Vocoder.h"
#include "DestructionLab.h"
#include "ModMatrix.h"
#include "SubSynth.h"
#include "Limiter.h"
#include "WavWriter.h"
#include "UdpSender.h"

namespace hn {

struct Params {
    std::atomic<float> noiseType{0}, noiseLevel{0.35f}, noiseCrush{16.f}, noiseHold{40.f}, noiseComb{0.25f};
    std::atomic<float> micGain{0.72f}, micDelayMs{18.f}, micTone{1200.f}, micMix{0.5f}, micGate{0.08f};
    std::atomic<float> fuzzDrive{0.8f}, fuzzOctave{0.35f}, fuzzGate{0.2f}, fuzzTone{0.55f};
    std::atomic<float> dodGain{0.85f}, dodLowCut{0.25f}, dodScoop{0.45f}, dodPresence{0.6f}, dodGate{0.15f};
    std::atomic<float> glitchMode{0}, glitchRate{6.f}, glitchSize{60.f}, glitchDecimate{8.f};
    std::atomic<float> glitchBits{8.f}, glitchMix{0.45f}, glitchProbability{0.35f}, glitchGate{0.3f};
    std::atomic<float> modalTune{220.f}, modalInharm{0.6f}, modalDecay{0.7f};
    std::atomic<float> modalBright{0.5f}, modalMix{0.3f};
    std::atomic<float> granMode{0}, granSize{80.f}, granDensity{12.f}, granPitch{0.f};
    std::atomic<float> granSpread{0.4f}, granFeedback{0.2f}, granMix{0.f};
    std::atomic<float> vocSource{0}, vocPitch{110.f}, vocShift{0.f};
    std::atomic<float> vocSpeed{0.5f}, vocQ{3.f}, vocMix{0.f};
    // Extreme Audio Lab: Destruction Lab, Mod Matrix e voices tonais.
    std::atomic<float> destMode{0}, destDrive{4.f}, destWet{0.f};
    std::atomic<float> modEnabled{0}, modRate{2.f}, modDepth{0.f}, modDest{0}, modShape{0};
    std::atomic<float> subFreq{55.f}, subLevel{0.f}, subWave{0};
    std::atomic<float> synthFreq{110.f}, synthLevel{0.f}, synthWave{3};
    std::atomic<float> threshold{-2.f}, masterGain{0.7f};
    // Respiração global (movimento lento) e cor final controlável pelo usuário.
    std::atomic<float> motionDepth{0.50f}, motionRate{0.60f}, saturation{0.40f};
    // Intensidade global (controla 6 módulos de uma vez) e pump ritmico pelo BPM.
    std::atomic<float> intensity{0.65f}, pump{0.25f};
    // Estado que antes era escrito direto na thread da UI: agora vai por atomico
    // e so e aplicado dentro de applyParams_(), na thread de audio.
    std::atomic<float> bpm{138.f}, swing{0.12f};
    std::atomic<float> kaossProgram{0}, kaossX{0.5f}, kaossY{0.5f}, kaossMix{1.f};
    std::atomic<bool> kaossLatch{false};
    std::atomic<bool> resetDspFlag{true}, triggerGlitchFlag{false}, triggerGranularFlag{false};
};

class AudioEngine {
public:
    explicit AudioEngine(float sr)
        : sr_(sr), noise_(sr), mic_(sr), drums_(sr), kaoss_(sr),
          glitch_(sr), modal_(sr), granular_(sr), vocoder_(sr),
          micBuf_(4096, 0.f), mixBuf_(1024 * 2, 0.f) {
        drums_.setTempo(138.f, 0.12f);
    }

    void setParam(const std::string& module, const std::string& param, float v);
    void setStep(int i, bool on, int voice, float pitch, float velocity, float probability);
    void setTempo(float bpm, float swing);
    void setKaoss(int program, float x, float y, bool latch, float mix);
    void triggerGlitch();
    void triggerGranular();
    /** Só sinaliza; o reset real acontece na thread de áudio, no primeiro render. */
    void resetDspState() {
        p_.resetDspFlag.store(true, std::memory_order_relaxed);
        p_.triggerGlitchFlag.store(false, std::memory_order_relaxed);
        p_.triggerGranularFlag.store(false, std::memory_order_relaxed);
        micR_.store(0, std::memory_order_relaxed);
        micW_.store(0, std::memory_order_relaxed);
    }

    void pushMic(float s);
    void render(float* out, int frames, int channels);

    bool startRecording(const std::string& path);
    std::string stopRecording();
    bool setStream(const std::string& host, int port);
    void stopStream();
    float meter() const { return meter_.load(std::memory_order_relaxed); }

private:
    float popMic_();
    /** Aplica comandos vindos da UI somente dentro da thread de áudio. */
    void applyDeferred_();
    void applyParams_();

    float sr_;
    Params p_;
    NoiseGenerator noise_;
    MicFeedback mic_;
    DrumSequencer drums_;
    HyperFuzz fuzz_;
    DODDeathMetal dod_;
    KaossChain kaoss_;
    GlitchProcessor glitch_;
    ModalResonator modal_;
    GranularEngine granular_;
    Vocoder vocoder_;
    DestructionLab destruction_;
    ModMatrix mod_;
    SubSynth tones_;
    Limiter limiter_;

    std::array<StepData, 16> steps_;
    mutable std::mutex stepMutex_;

    std::vector<float> micBuf_;
    std::atomic<size_t> micW_{0}, micR_{0};
    std::vector<float> mixBuf_;
    std::atomic<float> meter_{0.f};
    float motionPhase_ = 0.f, pumpPhase_ = 0.f, crackHold_ = 0.f;
    RNG crackRng_;

    WavWriter wav_;
    UdpSender udp_;
};
}
