cmake_minimum_required(VERSION 3.22.1)
project("extreme_audio")

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Oboe é baixado por scripts/fetch_deps.ps1 (apenas em tempo de build).
add_subdirectory(third_party/oboe)

add_library(extreme_audio SHARED
        AudioEngine.cpp
        JniBridge.cpp
        dsp/Utils.h
        dsp/NoiseGenerator.h
        dsp/MicFeedback.h
        dsp/DrumSequencer.h
        dsp/HyperFuzz.h
        dsp/DodDeathMetal.h
        dsp/KaossChain.h
        dsp/GlitchProcessor.h
        dsp/ModalResonator.h
        dsp/GranularEngine.h
        dsp/Vocoder.h
        dsp/DestructionLab.h
        dsp/ModMatrix.h
        dsp/SubSynth.h
        dsp/Limiter.h
        dsp/WavWriter.h
        dsp/UdpSender.h)

target_include_directories(extreme_audio PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/dsp
        ${CMAKE_CURRENT_SOURCE_DIR}/third_party/oboe/include)

target_link_libraries(extreme_audio oboe log)
