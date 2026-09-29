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
    gain = .68 / peak if peak > 0 else 1.0
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
    make("tap", .13, [(0, .115, 310.0, .30, "wood", 245.0)])
    make("navigate", .22, [(0, .18, 420.0, .25, "soft", 545.0)])
    make("place", .27, [(0, .15, 285.0, .30, "wood", 235.0), (.09, .16, 455.0, .19, "soft", 505.0)])
    make("coin", .40, [(0, .25, 720.0, .27, "bell", 810.0), (.10, .27, 1080.0, .24, "bell", 1210.0)])
    make("warning", .46, [(0, .25, 350.0, .22, "soft", 325.0), (.18, .24, 305.0, .18, "soft", 285.0)])
    make("pet", .50, [(0, .22, 500.0, .20, "soft", 590.0), (.17, .27, 650.0, .22, "soft", 735.0)])
    make("success", .74, [(0, .30, 523.25, .24, "bell", None), (.16, .32, 659.25, .24, "bell", None), (.34, .35, 783.99, .26, "bell", None)])
    make("level_up", .96, [(0, .27, 392.0, .21, "bell", None), (.15, .29, 523.25, .22, "bell", None),
                            (.32, .31, 659.25, .23, "bell", None), (.51, .40, 880.0, .25, "bell", 990.0)])


if __name__ == "__main__":
    main()
