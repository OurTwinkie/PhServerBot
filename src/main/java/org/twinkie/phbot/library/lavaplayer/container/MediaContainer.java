package org.twinkie.phbot.library.lavaplayer.container;

import org.twinkie.phbot.library.lavaplayer.container.adts.AdtsContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.flac.FlacContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.matroska.MatroskaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.mp3.Mp3ContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.mpeg.MpegContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.mpegts.MpegAdtsContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.ogg.OggContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.playlists.M3uPlaylistContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.playlists.PlainPlaylistContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.playlists.PlsPlaylistContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.wav.WavContainerProbe;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists currently supported containers and their probes.
 */
public enum MediaContainer {
  WAV(new WavContainerProbe()),
  MKV(new MatroskaContainerProbe()),
  MP4(new MpegContainerProbe()),
  FLAC(new FlacContainerProbe()),
  OGG(new OggContainerProbe()),
  M3U(new M3uPlaylistContainerProbe()),
  PLS(new PlsPlaylistContainerProbe()),
  PLAIN(new PlainPlaylistContainerProbe()),
  MP3(new Mp3ContainerProbe()),
  ADTS(new AdtsContainerProbe()),
  MPEGADTS(new MpegAdtsContainerProbe());

  /**
   * The probe used to detect files using this container and create the audio tracks for them.
   */
  public final MediaContainerProbe probe;

  MediaContainer(MediaContainerProbe probe) {
    this.probe = probe;
  }

  public static List<MediaContainerProbe> asList() {
    List<MediaContainerProbe> probes = new ArrayList<>();

    for (MediaContainer container : MediaContainer.class.getEnumConstants()) {
      probes.add(container.probe);
    }

    return probes;
  }
}
