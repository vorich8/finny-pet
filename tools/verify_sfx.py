"""Validate every bundled effect without external Python dependencies."""

from __future__ import annotations

import array
import wave
from pathlib import Path


RAW = Path(__file__).resolve().parents[1] / "app/src/main/res/raw"
NAMES = {"tap", "navigate", "place", "coin", "warning", "pet", "success", "level_up"}


def main() -> None:
    actual = {p.stem.removeprefix("sfx_") for p in RAW.glob("sfx_*.wav")}
    assert actual == NAMES, (actual, NAMES)
    for name in sorted(NAMES):
        path = RAW / f"sfx_{name}.wav"
        with wave.open(str(path), "rb") as sound:
            assert sound.getnchannels() == 1, path
            assert sound.getsampwidth() == 2, path
            assert sound.getframerate() == 22_050, path
            assert 0.1 <= sound.getnframes() / sound.getframerate() <= 1.1, path
            samples = array.array("h", sound.readframes(sound.getnframes()))
            assert max(map(abs, samples)) > 100, path
            assert max(map(abs, samples)) < 32_767, path
    print("8 WAV effects verified")


if __name__ == "__main__":
    main()
