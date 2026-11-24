#include "AudioEngine.h"
#include <cmath>
#include <cstring>

namespace hn {

void AudioEngine::setParam(const std::string& module, const std::string& param, float v) {
    // Um valor inválido vindo da UI nunca deve entrar no motor de áudio.
    if (!std::isfinite(v)) return;
    if (module == "noise") {
        if (param == "type") p_.noiseType = v;
        else if (param == "level") p_.noiseLevel = v;
        else if (param == "crush") p_.noiseCrush = v;
        else if (param == "hold") p_.noiseHold = v;
        else if (param == "comb") p_.noiseComb = v;
    } else if (module == "mic") {
        if (param == "gain") p_.micGain = v;
        else if (param == "delayMs") p_.micDelayMs = v;
        else if (param == "tone") p_.micTone = v;
        else if (param == "mix") p_.micMix = v;
        else if (param == "gate") p_.micGate = v;
    } else if (module == "fuzz") {
        if (param == "drive") p_.fuzzDrive = v;
        else if (param == "octave") p_.fuzzOctave = v;
        else if (param == "gate") p_.fuzzGate = v;
        else if (param == "tone") p_.fuzzTone = v;
    } else if (module == "dod") {
        if (param == "gain") p_.dodGain = v;
        else if (param == "lowCut") p_.dodLowCut = v;
        else if (param == "scoop") p_.dodScoop = v;
        else if (param == "presence") p_.dodPresence = v;
        else if (param == "gate") p_.dodGate = v;
    } else if (module == "glitch") {
        if (param == "mode") p_.glitchMode = v;
        else if (param == "rate") p_.glitchRate = v;
        else if (param == "size") p_.glitchSize = v;
        else if (param == "decimate") p_.glitchDecimate = v;
        else if (param == "bits") p_.glitchBits = v;
        else if (param == "mix") p_.glitchMix = v;
        else if (param == "probability") p_.glitchProbability = v;
        else if (param == "gate") p_.glitchGate = v;
    } else if (module == "modal") {
        if (param == "tune") p_.modalTune = v;
        else if (param == "inharmonicity") p_.modalInharm = v;
        else if (param == "decay") p_.modalDecay = v;
        else if (param == "brightness") p_.modalBright = v;
        else if (param == "mix") p_.modalMix = v;
    } else if (module == "granular") {
        if (param == "mode") p_.granMode = v;
        else if (param == "size") p_.granSize = v;
        else if (param == "density") p_.granDensity = v;
        else if (param == "pitch") p_.granPitch = v;
        else if (param == "spread") p_.granSpread = v;
        else if (param == "feedback") p_.granFeedback = v;
        else if (param == "mix") p_.granMix = v;
    } else if (module == "vocoder") {
        if (param == "source") p_.vocSource = v;
        else if (param == "pitch") p_.vocPitch = v;
        else if (param == "shift") p_.vocShift = v;
        else if (param == "speed") p_.vocSpeed = v;
        else if (param == "q") p_.vocQ = v;
        else if (param == "mix") p_.vocMix = v;
    } else if (module == "destruction") {
        // O reset por mudança de modo é feito pelo próprio DestructionLab,
        // dentro de setParams(), na thread de áudio. Nunca tocar no DSP da UI.
        if (param == "mode") p_.destMode = v;
        else if (param == "drive") p_.destDrive = v;
        else if (param == "wet") p_.destWet = v;
    } else if (module == "mod") {
        if (param == "enabled") p_.modEnabled = v;
        else if (param == "rate") p_.modRate = v;
        else if (param == "depth") p_.modDepth = v;
        else if (param == "destination") p_.modDest = v;
        else if (param == "shape") p_.modShape = v;
    } else if (module == "sub") {
        if (param == "frequency") p_.subFreq = v;
        else if (param == "level") p_.subLevel = v;
        else if (param == "wave") p_.subWave = v;
    } else if (module == "synth") {
        if (param == "frequency") p_.synthFreq = v;
        else if (param == "level") p_.synthLevel = v;
        else if (param == "wave") p_.synthWave = v;
    } else if (module == "master") {
        if (param == "threshold") p_.threshold = v;
        else if (param == "gain") p_.masterGain = v;
        else if (param == "motion") p_.motionDepth = v;
        else if (param == "motionRate") p_.motionRate = v;
        else if (param == "saturation") p_.saturation = v;
        else if (param == "intensity") p_.intensity = v;
        else if (param == "pump") p_.pump = v;
    } else if (module == "sample") {
        if (param == "mix") p_.sampleMix = v;
        else if (param == "tune") p_.sampleTune = v;
        else if (param == "start") p_.sampleStart = v;
        else if (param == "loop") p_.sampleLoop = v > 0.5f;
        else if (param == "trigger") p_.triggerSampleFlag = true;
    }
}

void AudioEngine::setStep(int i, bool on, int voice, float pitch, float velocity, float probability) {
    if (i < 0 || i > 15) return;
    std::lock_guard<std::mutex> lk(stepMutex_);
    steps_[i] = {on, voice, pitch, velocity, probability};
}

void AudioEngine::setTempo(float bpm, float swing) {
    // Nada de chamar drums_.setTempo() aqui: esta função roda na thread da UI.
    if (!std::isfinite(bpm) || !std::isfinite(swing)) return;
    p_.bpm.store(clamp(bpm, 20.f, 400.f), std::memory_order_relaxed);
    p_.swing.store(clamp(swing, 0.f, 0.6f), std::memory_order_relaxed);
}

void AudioEngine::setKaoss(int program, float x, float y, bool latch, float mix) {
    // A UI manda o estado completo; applyDeferred_() repassa ao DSP no áudio.
    if (!std::isfinite(x) || !std::isfinite(y) || !std::isfinite(mix)) return;
    p_.kaossProgram.store(clamp(program, 0, 7), std::memory_order_relaxed);
    p_.kaossX.store(clamp(x, 0.f, 1.f), std::memory_order_relaxed);
    p_.kaossY.store(clamp(y, 0.f, 1.f), std::memory_order_relaxed);
    p_.kaossLatch.store(latch, std::memory_order_relaxed);
    p_.kaossMix.store(clamp(mix, 0.f, 1.f), std::memory_order_relaxed);
}

void AudioEngine::triggerGlitch() {
    p_.triggerGlitchFlag.store(true, std::memory_order_relaxed);
}

void AudioEngine::triggerGranular() {
    p_.triggerGranularFlag.store(true, std::memory_order_relaxed);
}

void AudioEngine::pushMic(float s) {
    size_t w = micW_.load(std::memory_order_relaxed);
    micBuf_[w] = s;
    micW_.store((w + 1) % micBuf_.size(), std::memory_order_release);
}

float AudioEngine::popMic_() {
    size_t r = micR_.load(std::memory_order_relaxed);
    if (r == micW_.load(std::memory_order_acquire)) return 0.f;
    float s = micBuf_[r];
    micR_.store((r + 1) % micBuf_.size(), std::memory_order_release);
    return s;
}

void AudioEngine::applyDeferred_() {
    // Reset pedido ao iniciar/reiniciar o motor.
    if (p_.resetDspFlag.exchange(false, std::memory_order_relaxed)) {
        mod_.reset();
        tones_.reset();
        destruction_.reset();
        motionPhase_ = 0.f;
        pumpPhase_ = 0.f;
        crackHold_ = 0.f;
    }
    // Tempo e Kaoss antes eram escritos direto da thread da UI — corrida de dados.
    drums_.setTempo(p_.bpm.load(std::memory_order_relaxed),
                    p_.swing.load(std::memory_order_relaxed));
    kaoss_.setParams(
        (int)p_.kaossProgram.load(std::memory_order_relaxed),
        p_.kaossX.load(std::memory_order_relaxed),
        p_.kaossY.load(std::memory_order_relaxed),
        p_.kaossLatch.load(std::memory_order_relaxed),
        p_.kaossMix.load(std::memory_order_relaxed));
    // Disparos manuais viram eventos únicos, aplicados aqui no callback.
    if (p_.triggerGlitchFlag.exchange(false, std::memory_order_relaxed)) {
        glitch_.trigger();
        modal_.excite(0.9f);
    }
    if (p_.triggerGranularFlag.exchange(false, std::memory_order_relaxed)) {
        granular_.trigger();
        modal_.excite(0.6f);
    }
    // Amostra do usuário: assume o buffer novo e/ou reposiciona o playhead.
    {
        std::unique_lock<std::mutex> lk(sampleMutex_, std::try_to_lock);
        if (lk.owns_lock() && samplePending_) {
            sampler_.adopt(std::move(pendingSample_), pendingSampleSr_);
            pendingSample_.clear();
            samplePending_ = false;
        }
    }
    if (p_.triggerSampleFlag.exchange(false, std::memory_order_relaxed)) {
        sampler_.trigger();
    }
}

void AudioEngine::applyParams_() {
    applyDeferred_();
    // O fader Intensidade empurra vários módulos ao mesmo tempo: é o controle
    // "geral" que transforma um som contido em uma parede de ruído abrasiva.
    const float it = clamp(p_.intensity.load(), 0.f, 1.f);
    noise_.setParams((int)p_.noiseType.load(), p_.noiseLevel.load() * (0.35f + it * 1.45f),
                     p_.noiseCrush.load() - it * 2.f, p_.noiseHold.load(),
                     clamp(p_.noiseComb.load() + it * 0.25f, 0.f, 1.f));
    mic_.setParams(p_.micGain.load(), p_.micDelayMs.load(), p_.micTone.load(),
                   p_.micMix.load(), p_.micGate.load());
    fuzz_.setParams(clamp(p_.fuzzDrive.load() + it * 0.35f, 0.f, 1.f),
                    p_.fuzzOctave.load(), p_.fuzzGate.load(), p_.fuzzTone.load());
    dod_.setParams(clamp(p_.dodGain.load() + it * 0.35f, 0.f, 1.f), p_.dodLowCut.load(),
                   p_.dodScoop.load(), p_.dodPresence.load(), p_.dodGate.load());
    glitch_.setParams((int)p_.glitchMode.load(), p_.glitchRate.load(), p_.glitchSize.load(),
                      p_.glitchDecimate.load(), p_.glitchBits.load(),
                      clamp(p_.glitchMix.load() + it * 0.30f, 0.f, 1.f),
                      clamp(p_.glitchProbability.load() + it * 0.40f, 0.f, 1.f),
                      p_.glitchGate.load());
    modal_.setParams(p_.modalTune.load(), p_.modalInharm.load(), p_.modalDecay.load(),
                     p_.modalBright.load(), clamp(p_.modalMix.load() + it * 0.20f, 0.f, 1.f));
    granular_.setParams((int)p_.granMode.load(), p_.granSize.load(), p_.granDensity.load(),
                        p_.granPitch.load(), p_.granSpread.load(), p_.granFeedback.load(),
                        p_.granMix.load());
    vocoder_.setParams(p_.vocPitch.load(), p_.vocShift.load(), p_.vocSpeed.load(),
                       p_.vocQ.load(), (int)p_.vocSource.load());
    destruction_.setParams((int)p_.destMode.load(), p_.destDrive.load() * (1.f + it * 1.8f),
                           clamp(p_.destWet.load() + it * 0.35f, 0.f, 1.f));
    mod_.setParams(p_.modEnabled.load() > 0.5f, p_.modRate.load(),
                   clamp(p_.modDepth.load() + it * 0.20f, 0.f, 1.f),
                   (int)p_.modDest.load(), (int)p_.modShape.load());
    tones_.setSub(p_.subFreq.load(), p_.subLevel.load(), (int)p_.subWave.load());
    tones_.setSynth(p_.synthFreq.load(), p_.synthLevel.load(), (int)p_.synthWave.load());
    sampler_.setParams(p_.sampleMix.load(), p_.sampleTune.load(),
                       p_.sampleStart.load(), p_.sampleLoop.load());
}

void AudioEngine::render(float* out, int frames, int channels) {
    applyParams_();
    std::array<StepData, 16> localSteps;
    {
        std::lock_guard<std::mutex> lk(stepMutex_);
        localSteps = steps_;
    }
    if ((size_t)frames * channels > mixBuf_.size()) mixBuf_.resize((size_t)frames * channels * 2);

    float sumSq = 0.f, reduction = 0.f;
    const float it = clamp(p_.intensity.load(), 0.f, 1.f);
    const float motionDepth = clamp(p_.motionDepth.load(), 0.f, 1.f);
    const float motionInc = 6.2831853f * clamp(p_.motionRate.load(), 0.05f, 4.f) / sr_;
    const float sat = clamp(p_.saturation.load(), 0.f, 1.f);
    const float pumpAmt = clamp(p_.pump.load(), 0.f, 1.f);
    const float beatInc = 6.2831853f *
        (p_.bpm.load(std::memory_order_relaxed) / 60.f) * 2.f / sr_;
    for (int i = 0; i < frames; ++i) {
        // Respiração global: dois LFOs lentos dão vida ao som mesmo sem tocar nos faders.
        motionPhase_ += motionInc;
        if (motionPhase_ >= 6.2831853f) motionPhase_ -= 6.2831853f;
        const float swell = std::sin(motionPhase_) * motionDepth;
        const float shimmer = std::sin(motionPhase_ * 2.7f + 0.8f) * motionDepth;

        // Pump ritmico: pulsos curtos sincronizados com o BPM (oitavos) dão impacto.
        pumpPhase_ += beatInc;
        if (pumpPhase_ >= 6.2831853f) pumpPhase_ -= 6.2831853f;
        const float pumpEnv = std::pow(0.5f + 0.5f * std::cos(pumpPhase_), 4.f);
        const float pumpGain = 1.f + pumpAmt * pumpEnv * 1.4f;

        // Estalos abrasivos (referência: Pulse Demon): esporádicos, não uma chuva contínua.
        float crack = 0.f;
        if (crackHold_ <= 0.f && crackRng_.next() > 0.99985f - it * 0.001f) {
            crackHold_ = (crackRng_.next() * 0.25f + 0.08f) * 0.001f * sr_;
        }
        if (crackHold_ > 0.f) { crack = crackRng_.next(); crackHold_ -= 1.f; }

        // Mod Matrix: escalas limitadas, sem alterar os átomos base.
        const float lfo = mod_.next(sr_);
        float noiseScale = 1.f, subScale = 1.f, synthScale = 1.f, micScale = 1.f;
        switch (mod_.destination()) {
            case 0: noiseScale = clamp(1.f + lfo * 0.8f, 0.f, 2.f); break;
            case 1: subScale = clamp(1.f + lfo, 0.f, 2.f); break;
            case 2: synthScale = clamp(1.f + lfo, 0.f, 2.f); break;
            default: micScale = clamp(1.f + lfo, 0.f, 2.f); break;
        }
        float noise = noise_.process() * noiseScale * (1.f + shimmer * 0.65f);
        float mic = popMic_();
        float micOut = mic_.process(mic) * micScale;
        drums_.tick(localSteps[drums_.currentStep() < 0 ? 0 : drums_.currentStep()]);
        float drum = drums_.process(noise);
        float tone = tones_.process(sr_, subScale, synthScale);
        // Amostra do usuário: entra como mais uma fonte, antes de toda a cadeia.
        float smp = sampler_.process();

        // Ruído, vozes, amostra e estalos ganham presença para que os faders sejam ouvidos.
        float mix = noise * 1.05f + micOut * 0.90f + drum * 0.85f + tone * 0.90f
                    + smp + crack * (0.06f + it * 0.26f);
        // O vocoder usa o microfone como modulador e o ruído como portadora.
        float voiced = vocoder_.process(mic, noise);
        float src = lerp(mix, voiced, p_.vocMix.load());
        float g = glitch_.process(src);
        float grains = granular_.process(g);
        float metallic = modal_.process(grains);
        float x = fuzz_.process(metallic);
        x = dod_.process(x);
        x = destruction_.process(x);
        x = kaoss_.process(x);
        // Breakdown: o gate do glitch ducka a cadeia antes do limitador.
        x *= (1.f - glitch_.activity());
        x *= (1.f + swell * 0.85f) * pumpGain;
        // Volume antes do limitador: o fader de volume agora altera o nível real.
        x *= p_.masterGain.load();
        x = limiter_.process(x, p_.threshold.load(), reduction);
        // Cor final opcional: saturação controlável sem "achatar" a dinâmica abaixo dela.
        if (sat > 0.01f) {
            const float drive = 1.f + sat * 6.f;
            x = fastTanh(x * drive) / fastTanh(drive);
        }

        for (int c = 0; c < channels; ++c) {
            // Última rede de segurança: um NaN nunca deve chegar ao Oboe.
            if (!std::isfinite(x)) x = 0.f;
            float v = clamp(x, -1.f, 1.f);
            out[i * channels + c] = v;
            mixBuf_[i * channels + c] = v;
            sumSq += v * v;
        }
    }
    if (wav_.isOpen()) wav_.write(mixBuf_.data(), frames);
    if (udp_.connected()) udp_.send(mixBuf_.data(), frames, channels, (int)sr_);

    float rms = std::sqrt(sumSq / std::max(1, frames * channels));
    meter_.store(std::max(rms, reduction), std::memory_order_relaxed);
}

bool AudioEngine::loadSample(const float* data, int frames, float fileSr) {
    if (!data || frames <= 0) return false;
    if (!std::isfinite(fileSr) || fileSr < 1000.f || fileSr > 384000.f) return false;
    std::lock_guard<std::mutex> lk(sampleMutex_);
    pendingSample_.assign(data, data + frames);
    pendingSampleSr_ = fileSr;
    samplePending_ = true;
    return true;
}

bool AudioEngine::startRecording(const std::string& path) {
    if (wav_.isOpen()) wav_.close();
    return wav_.open(path, (int)sr_, 2);
}

std::string AudioEngine::stopRecording() {
    if (!wav_.isOpen()) return {};
    wav_.close();
    return "ok";
}

bool AudioEngine::setStream(const std::string& host, int port) {
    udp_.disconnect();
    return udp_.connect(host, port);
}

void AudioEngine::stopStream() { udp_.disconnect(); }
}
