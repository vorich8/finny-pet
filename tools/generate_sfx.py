"""Generate Finny's original, royalty-free interface sounds using only stdlib.

The waveforms are synthesized from sine partials; no third-party recording,
sample pack, sound font, or model output is used. Run with Python 3.10+.
"""

from __future__ import annotations

import math
import struct
import wave
from pathlib import Path


RATE = 44_100
OUT = Path(__file__).resolve().parents[1] / "app/src/main/res/raw"


def note(buffer: list[float], start: float, length: float, frequency: float,
         volume: float, timbre: str = "bell", end_frequency: float | None = None) -> None:
    first = int(start * RATE)
    count = int(length * RATE)
    for i in range(count):
        t = i / RATE
        progress = t / length
        target = end_frequency if end_frequency is not None else frequency
        current_frequency = frequency + (target - frequency) * progress
        phase = 2 * math.pi * current_frequency * t
        # Raised-cosine edges remove clicks at the beginning and end of a sample.
        attack = .5 - .5 * math.cos(math.pi * min(1.0, t / .018))
        release_time = min(.11, length * .42)
        release_progress = min(1.0, max(0.0, (length - t) / release_time))
        release = .5 - .5 * math.cos(math.pi * release_progress)
        envelope = attack * release * math.exp(-2.25 * progress)
        if timbre == "wood":
            voice = math.sin(phase) + .12 * math.sin(2 * phase) + .025 * math.sin(3 * phase)
        elif timbre == "chime":
            # A rounded, airy upper partial.  It stays pleasant on laptop and
            # emulator speakers without the sharp metallic attack of a bell.
            voice = math.sin(phase) + .075 * math.sin(2.01 * phase) + .018 * math.sin(3.02 * phase)
        elif timbre == "bubble":
            # Soft toy-like tone for taps and pet interactions.
            voice = math.sin(phase) + .035 * math.sin(1.51 * phase)
        elif timbre == "soft":
            voice = math.sin(phase) + .045 * math.sin(2 * phase)
        else:
            voice = math.sin(phase) + .11 * math.sin(2 * phase) + .025 * math.sin(3 * phase)
        buffer[first + i] += volume * envelope * voice


def make(name: str, length: float, events: list[tuple[float, float, float, float, str, float | None]]) -> None:
    frames = [0.0] * int(length * RATE)
    for start, duration, frequency, volume, timbre, end_frequency in events:
        note(frames, start, duration, frequency, volume, timbre, end_frequency)
    peak = max((abs(sample) for sample in frames), default=1.0)
    # Leave comfortable headroom: these effects are layered over speech and
    # background audio, so they should never dominate the scene.
    gain = .34 / peak if peak > 0 else 1.0
    pcm = bytearray()
    for sample in frames:
        # Linear normalization leaves generous headroom and avoids limiter distortion.
        clean = max(-.72, min(.72, sample * gain))
        pcm.extend(struct.pack("<h", round(clean * 32_767)))
    with wave.open(str(OUT / f"sfx_{name}.wav"), "wb") as audio:
        audio.setnchannels(1)
        audio.setsampwidth(2)
        audio.setframerate(RATE)
        audio.writeframes(pcm)


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    make("tap", .11, [(0, .085, 235.0, .14, "bubble", 205.0)])
    make("navigate", .20, [(0, .16, 294.0, .12, "soft", 350.0), (.06, .13, 392.0, .07, "chime", 420.0)])
    make("place", .25, [(0, .13, 220.0, .13, "wood", 205.0), (.09, .14, 330.0, .08, "chime", 360.0)])
    make("coin", .32, [(0, .19, 523.0, .12, "chime", 587.0), (.10, .18, 698.0, .08, "chime", 784.0)])
    make("warning", .40, [(0, .22, 262.0, .09, "soft", 248.0), (.17, .18, 220.0, .07, "soft", 208.0)])
    make("pet", .42, [(0, .21, 392.0, .10, "bubble", 440.0), (.16, .22, 494.0, .08, "bubble", 523.0)])
    make("success", .60, [(0, .22, 392.0, .10, "soft", None), (.15, .23, 494.0, .09, "chime", None), (.30, .25, 587.0, .10, "chime", None)])
    make("level_up", .78, [(0, .22, 330.0, .08, "soft", None), (.15, .23, 392.0, .08, "soft", None),
                            (.30, .24, 494.0, .09, "chime", None), (.46, .27, 659.0, .09, "chime", 698.0)])


if __name__ == "__main__":
    main()
