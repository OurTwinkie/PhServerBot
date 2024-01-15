package org.twinkie.phbot.library.lavaplayer.container.ogg;

public interface OggTrackBlueprint {
  OggTrackHandler loadTrackHandler(OggPacketInputStream stream);
  int getSampleRate();
}
