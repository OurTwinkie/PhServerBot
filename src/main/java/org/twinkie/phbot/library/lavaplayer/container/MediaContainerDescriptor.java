package org.twinkie.phbot.library.lavaplayer.container;

import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

public class MediaContainerDescriptor {
  public final MediaContainerProbe probe;
  public final String parameters;

  public MediaContainerDescriptor(MediaContainerProbe probe, String parameters) {
    this.probe = probe;
    this.parameters = parameters;
  }

  public AudioTrack createTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    return probe.createTrack(parameters, trackInfo, inputStream);
  }
}
