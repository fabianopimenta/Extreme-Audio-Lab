#requires -Version 5.1
<#
  Gera as texturas de harsh noise originais que acompanham o app.
  São criações do próprio PMNT (síntese), não gravações de terceiros:
  nada é extraído de YouTube, streaming ou álbuns comerciais.

  Uso:  powershell -ExecutionPolicy Bypass -File scripts/generate_samples.ps1
#>
$ErrorActionPreference = 'Stop'

$source = @'
using System;
using System.IO;

public static class PmntSampleGen
{
    const int SR = 48000;

    static uint state = 0x9e3779b9u;
    static float Rnd()
    {
        state ^= state << 13; state ^= state >> 17; state ^= state << 5;
        return (state >> 8) / 16777216f * 2f - 1f;
    }
    static float Clamp(float v, float a, float b) { return Math.Max(a, Math.Min(b, v)); }
    static float Tanh(float x) { return (float)Math.Tanh(x); }
    static float TwoPi = (float)(Math.PI * 2.0);

    struct SVF
    {
        public float low, band;
        public void Process(float inp, float cutoff, float q, float sr)
        {
            float f = 2f * (float)Math.Sin(Math.PI * Clamp(cutoff, 20f, sr * 0.45f) / sr);
            float qq = 1f / Clamp(q, 0.5f, 12f);
            low += f * band;
            float high = inp - low - qq * band;
            band += f * high;
        }
    }

    static float[] CrossfadeLoop(float[] src, int loopLen)
    {
        int fade = SR / 4;                    // 250 ms de crossfade
        if (fade > loopLen / 2) fade = loopLen / 2;
        var outp = new float[loopLen];
        Array.Copy(src, outp, loopLen);
        for (int i = 0; i < fade; i++)
        {
            float a = (float)i / fade;
            outp[i] = outp[i] * a + src[loopLen + i] * (1f - a);
        }
        return outp;
    }

    static void Normalize(float[] b, float peak)
    {
        float max = 0f;
        for (int i = 0; i < b.Length; i++) max = Math.Max(max, Math.Abs(b[i]));
        if (max < 1e-6f) return;
        float g = peak / max;
        for (int i = 0; i < b.Length; i++) b[i] *= g;
    }

    static void WriteWav(string path, float[] mono)
    {
        using (var fs = new FileStream(path, FileMode.Create, FileAccess.Write))
        using (var bw = new BinaryWriter(fs))
        {
            int dataBytes = mono.Length * 2;
            bw.Write(new[] { (byte)'R', (byte)'I', (byte)'F', (byte)'F' });
            bw.Write(36 + dataBytes);
            bw.Write(new[] { (byte)'W', (byte)'A', (byte)'V', (byte)'E' });
            bw.Write(new[] { (byte)'f', (byte)'m', (byte)'t', (byte)' ' });
            bw.Write(16);
            bw.Write((short)1);               // PCM
            bw.Write((short)1);               // mono
            bw.Write(SR);
            bw.Write(SR * 2);                 // byte rate
            bw.Write((short)2);               // block align
            bw.Write((short)16);              // bits
            bw.Write(new[] { (byte)'d', (byte)'a', (byte)'t', (byte)'a' });
            bw.Write(dataBytes);
            for (int i = 0; i < mono.Length; i++)
            {
                short s = (short)Math.Round(Clamp(mono[i], -1f, 1f) * 32767f);
                bw.Write(s);
            }
        }
    }

    // Parede: ruído branco + marrom, filtro ressonante que varre, saturação e estalos.
    static float[] Parede()
    {
        const float secs = 10f;
        int n = (int)(secs * SR);
        int total = n + SR / 4;
        var b = new float[total];
        var svf = new SVF();
        float brown = 0f, crack = 0f;
        for (int i = 0; i < total; i++)
        {
            float t = (float)i / SR;
            float w = Rnd();
            brown = Clamp(brown * 0.996f + w * 0.025f, -1f, 1f);
            float cutoff = 850f
                + 700f * (float)Math.Sin(TwoPi * 0.07f * t)
                + 260f * (float)Math.Sin(TwoPi * 0.013f * t + 1.3f);
            svf.Process(w * 0.75f + brown * 0.9f, cutoff, 1.35f, SR);
            float env = 0.62f + 0.38f * (float)Math.Sin(TwoPi * 0.23f * t + 0.4f);
            float x = Tanh((svf.band * 1.7f + brown * 0.55f) * (3.6f * env));
            if (Rnd() > 0.99935f) crack = 0.75f;
            crack *= 0.982f;
            x += crack * Rnd() * 0.55f;
            b[i] = x;
        }
        var loop = CrossfadeLoop(b, n);
        Normalize(loop, 0.85f);
        return loop;
    }

    // Metal: ring mod de duas portadoras + ressonância varrendo + gestos de raspagem.
    static float[] Metal()
    {
        const float secs = 10f;
        int n = (int)(secs * SR);
        int total = n + SR / 4;
        var b = new float[total];
        var svf = new SVF();
        for (int i = 0; i < total; i++)
        {
            float t = (float)i / SR;
            float w = Rnd();
            float center = 1500f
                + 900f * (float)Math.Sin(TwoPi * 0.05f * t)
                + 350f * (float)Math.Sin(TwoPi * 0.017f * t + 0.8f);
            svf.Process(w, center, 3.2f, SR);
            float ring = (float)Math.Sin(TwoPi * 213f * t)
                       * (float)Math.Sin(TwoPi * 318f * t + 0.6f * (float)Math.Sin(TwoPi * 0.11f * t));
            float scrape = (float)Math.Pow(0.5 + 0.5 * Math.Sin(TwoPi * 0.37f * t + 0.9f), 3.0);
            float x = Tanh((svf.band * 1.5f + w * 0.32f) * ring * (2.4f + 3.2f * scrape));
            b[i] = x;
        }
        var loop = CrossfadeLoop(b, n);
        Normalize(loop, 0.82f);
        return loop;
    }

    // Squeal: apito de realimentação com glissando, harmônicos e ruído áspero.
    static float[] Squeal()
    {
        const float secs = 10f;
        int n = (int)(secs * SR);
        int total = n + SR / 4;
        var b = new float[total];
        var svf = new SVF();
        double ph1 = 0, ph2 = 0, ph3 = 0;
        for (int i = 0; i < total; i++)
        {
            float t = (float)i / SR;
            float f1 = 1150f
                + 420f * (float)Math.Sin(TwoPi * 0.06f * t)
                + 90f * (float)Math.Sin(TwoPi * 0.31f * t);
            float f2 = f1 * 1.51f;
            float f3 = f1 * 2.03f;
            ph1 += TwoPi * f1 / SR; if (ph1 > TwoPi) ph1 -= TwoPi;
            ph2 += TwoPi * f2 / SR; if (ph2 > TwoPi) ph2 -= TwoPi;
            ph3 += TwoPi * f3 / SR; if (ph3 > TwoPi) ph3 -= TwoPi;
            float tone = (float)Math.Sin(ph1) + 0.65f * (float)Math.Sin(ph2) + 0.45f * (float)Math.Sin(ph3);
            float w = Rnd();
            svf.Process(w, 2800f + 900f * (float)Math.Sin(TwoPi * 0.09f * t), 2.0f, SR);
            float am = 0.75f + 0.25f * (float)Math.Sin(TwoPi * 6.3f * t);
            float x = Tanh((tone * 0.5f + svf.band * 0.55f + w * 0.15f)
                           * (2.2f + 1.6f * (float)Math.Sin(TwoPi * 0.13f * t)) * am);
            b[i] = x;
        }
        var loop = CrossfadeLoop(b, n);
        Normalize(loop, 0.80f);
        return loop;
    }

    public static void Generate(string dir)
    {
        Directory.CreateDirectory(dir);
        WriteWav(Path.Combine(dir, "parede_01.wav"), Parede());
        WriteWav(Path.Combine(dir, "metal_01.wav"), Metal());
        WriteWav(Path.Combine(dir, "squeal_01.wav"), Squeal());
    }
}
'@

Add-Type -TypeDefinition $source -Language CSharp
$outDir = Join-Path $PSScriptRoot '..\app\src\main\assets\samples'
[PmntSampleGen]::Generate((Resolve-Path (New-Item -ItemType Directory -Force -Path $outDir)).Path)
Get-ChildItem $outDir | Select-Object Name, Length
