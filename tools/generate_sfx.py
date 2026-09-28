"""Generate Finny's original, royalty-free interface sounds using only stdlib.

The waveforms are synthesized from sine partials; no third-party recording,
sample pack, sound font, or model output is used. Run with Python 3.10+.
"""

from __future__ import annotations

import math
import struct
import wave
from pathlib import Path


RATE = 22_050
OUT = Path(__file__).resolve().parents[1] / "app/src/main/res/raw"


def note(buffer: list[float], start: float, length: float, frequency: float,
         volume: float, timbre: str = "bell") -> None:
    first = int(start * RATE)
    count = int(length * RATE)
    for i in range(count):
        t = i / RATE
        phase = 2 * math.pi * frequency * t
        attack = min(1.0, t / 0.006)
        release = min(1.0, (length - t) / 0.045)
        envelope = attack * max(0.0, release) * math.exp(-3.1 * t / length)
        if timbre == "wood":
            voice = math.sin(phase) + .20 * math.sin(2 * phase) + .06 * math.sin(3 * phase)
        elif timbre == "soft":
            voice = math.sin(phase) + .10 * math.sin(2 * phase)
        else:
            voice = math.sin(phase) + .34 * math.sin(2.01 * phase) + .12 * math.sin(3.02 * phase)
        buffer[first + i] += volume * envelope * voice


def make(name: str, length: float, events: list[tuple[float, float, float, float, str]]) -> None:
    frames = [0.0] * int(length * RATE)
    for start, duration, frequency, volume, timbre in events:
        note(frames, start, duration, frequency, volume, timbre)
    pcm = bytearray()
    for sample in frames:
        # A soft limiter leaves headroom and prevents harsh clipping.
        pcm.extend(struct.pack("<h", round(math.tanh(sample * 1.15) * 20_000)))
    with wave.open(str(OUT / f"sfx_{name}.wav"), "wb") as audio:
        audio.setnchannels(1)
        audio.setsampwidth(2)
        audio.setframerate(RATE)
        audio.writeframes(pcm)


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    make("tap", .15, [(0, .12, 587.33, .42, "wood")])
    make("navigate", .28, [(0, .17, 440.00, .29, "soft"), (.07, .19, 659.25, .30, "bell")])
    make("place", .30, [(0, .16, 392.00, .34, "wood"), (.11, .17, 523.25, .30, "bell")])
    make("coin", .38, [(0, .24, 783.99, .36, "bell"), (.095, .27, 1174.66, .31, "bell")])
    make("warning", .42, [(0, .20, 392.00, .24, "soft"), (.15, .24, 329.63, .21, "soft")])
    make("pet", .55, [(0, .18, 523.25, .24, "soft"), (.14, .19, 659.25, .25, "soft"), (.29, .22, 783.99, .22, "soft")])
    make("success", .80, [(0, .27, 523.25, .31, "bell"), (.16, .29, 659.25, .31, "bell"), (.34, .42, 783.99, .37, "bell")])
    make("level_up", 1.05, [(0, .24, 392.00, .28, "bell"), (.14, .26, 523.25, .29, "bell"),
                             (.30, .29, 659.25, .31, "bell"), (.48, .52, 1046.50, .34, "bell")])


if __name__ == "__main__":
    main()
