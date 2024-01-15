package org.twinkie.phbot.library.lavaplayer.source.youtube;

import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.List;

public interface YoutubeTrackDetails {
  AudioTrackInfo getTrackInfo();

  List<YoutubeTrackFormat> getFormats(HttpInterface httpInterface, YoutubeSignatureResolver signatureResolver);

  String getPlayerScript();
}
