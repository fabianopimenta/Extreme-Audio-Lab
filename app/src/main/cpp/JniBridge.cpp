#include <jni.h>
#include <oboe/Oboe.h>
#include <memory>
#include <string>
#include "AudioEngine.h"

using namespace hn;

class InputCallback;
class OutputCallback;

struct EngineHandle {
    std::unique_ptr<AudioEngine> engine;
    std::shared_ptr<oboe::AudioStream> input;
    std::shared_ptr<oboe::AudioStream> output;
    std::shared_ptr<InputCallback> inputCallback;
    std::shared_ptr<OutputCallback> outputCallback;
};

class InputCallback : public oboe::AudioStreamCallback {
public:
    EngineHandle* handle = nullptr;
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream*, void* audioData, int32_t numFrames) override {
        auto* samples = static_cast<float*>(audioData);
        for (int32_t i = 0; i < numFrames; ++i) handle->engine->pushMic(samples[i]);
        return oboe::DataCallbackResult::Continue;
    }
};

class OutputCallback : public oboe::AudioStreamCallback {
public:
    EngineHandle* handle = nullptr;
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream* stream, void* audioData, int32_t numFrames) override {
        auto* out = static_cast<float*>(audioData);
        handle->engine->render(out, numFrames, stream->getChannelCount());
        return oboe::DataCallbackResult::Continue;
    }
};

static EngineHandle* asHandle(jlong h) { return reinterpret_cast<EngineHandle*>(h); }

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeCreate(JNIEnv*, jobject, jint sampleRate, jint) {
    auto* h = new EngineHandle();
    h->engine = std::make_unique<AudioEngine>((float)sampleRate);

    h->inputCallback = std::make_shared<InputCallback>();
    h->inputCallback->handle = h;
    oboe::AudioStreamBuilder inBuilder;
    inBuilder.setDirection(oboe::Direction::Input)
             ->setChannelCount(1)
             ->setSampleRate(sampleRate)
             ->setFormat(oboe::AudioFormat::Float)
             ->setSharingMode(oboe::SharingMode::Exclusive)
             ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
             ->setCallback(h->inputCallback.get());
    if (inBuilder.openStream(h->input) != oboe::Result::OK ||
        !h->input || h->input->getFormat() != oboe::AudioFormat::Float) {
        h->input.reset(); // microfone é opcional: o app continua sem feedback
    }

    h->outputCallback = std::make_shared<OutputCallback>();
    h->outputCallback->handle = h;
    oboe::AudioStreamBuilder outBuilder;
    outBuilder.setDirection(oboe::Direction::Output)
              ->setChannelCount(2)
              ->setSampleRate(sampleRate)
              ->setFormat(oboe::AudioFormat::Float)
              ->setSharingMode(oboe::SharingMode::Exclusive)
              ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
              ->setCallback(h->outputCallback.get());
    if (outBuilder.openStream(h->output) != oboe::Result::OK ||
        !h->output || h->output->getFormat() != oboe::AudioFormat::Float ||
        h->output->getChannelCount() < 2) {
        h->output.reset(); // saída inválida: nativeStart() vai reportar falha
    }

    return reinterpret_cast<jlong>(h);
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeDestroy(JNIEnv*, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (!h) return;
    if (h->input) { h->input->requestStop(); h->input->close(); }
    if (h->output) { h->output->requestStop(); h->output->close(); }
    delete h;
}

JNIEXPORT jboolean JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeStart(JNIEnv*, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (!h || !h->output) return JNI_FALSE;
    h->engine->resetDspState();
    if (h->input) h->input->requestStart();
    if (h->output) h->output->requestStart();
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeStop(JNIEnv*, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (!h) return;
    if (h->input) h->input->requestStop();
    if (h->output) h->output->requestStop();
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeSetParam(
        JNIEnv* env, jobject, jlong handle, jstring module, jstring param, jfloat value) {
    auto* h = asHandle(handle);
    if (!h) return;
    const char* m = env->GetStringUTFChars(module, nullptr);
    const char* p = env->GetStringUTFChars(param, nullptr);
    h->engine->setParam(m, p, value);
    env->ReleaseStringUTFChars(module, m);
    env->ReleaseStringUTFChars(param, p);
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeSetStep(
        JNIEnv*, jobject, jlong handle, jint i, jboolean on, jint voice,
        jfloat pitch, jfloat velocity, jfloat probability) {
    auto* h = asHandle(handle);
    if (h) h->engine->setStep(i, on == JNI_TRUE, voice, pitch, velocity, probability);
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeSetTempo(
        JNIEnv*, jobject, jlong handle, jfloat bpm, jfloat swing) {
    auto* h = asHandle(handle);
    if (h) h->engine->setTempo(bpm, swing);
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeSetKaoss(
        JNIEnv*, jobject, jlong handle, jint program, jfloat x, jfloat y, jboolean latch, jfloat mix) {
    auto* h = asHandle(handle);
    if (h) h->engine->setKaoss(program, x, y, latch == JNI_TRUE, mix);
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeTriggerGlitch(JNIEnv*, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (h) h->engine->triggerGlitch();
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeTriggerGranular(JNIEnv*, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (h) h->engine->triggerGranular();
}

JNIEXPORT jboolean JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeStartRecording(
        JNIEnv* env, jobject, jlong handle, jstring path) {
    auto* h = asHandle(handle);
    if (!h) return JNI_FALSE;
    const char* p = env->GetStringUTFChars(path, nullptr);
    bool ok = h->engine->startRecording(p);
    env->ReleaseStringUTFChars(path, p);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeStopRecording(JNIEnv* env, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (!h) return nullptr;
    std::string r = h->engine->stopRecording();
    return r.empty() ? nullptr : env->NewStringUTF(r.c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeSetStream(
        JNIEnv* env, jobject, jlong handle, jstring host, jint port) {
    auto* h = asHandle(handle);
    if (!h) return JNI_FALSE;
    const char* hp = env->GetStringUTFChars(host, nullptr);
    bool ok = h->engine->setStream(hp, port);
    env->ReleaseStringUTFChars(host, hp);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeStopStream(JNIEnv*, jobject, jlong handle) {
    auto* h = asHandle(handle);
    if (h) h->engine->stopStream();
}

JNIEXPORT jfloatArray JNICALL
Java_com_extremeaudiolab_audio_NativeEngine_nativeMeter(JNIEnv* env, jobject, jlong handle) {
    jfloatArray arr = env->NewFloatArray(2);
    float v[2] = {0.f, 0.f};
    auto* h = asHandle(handle);
    if (h) v[0] = h->engine->meter();
    env->SetFloatArrayRegion(arr, 0, 2, v);
    return arr;
}
}
