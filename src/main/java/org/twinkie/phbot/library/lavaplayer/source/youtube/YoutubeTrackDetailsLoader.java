package org.twinkie.phbot.library.lavaplayer.source.youtube;

import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

public interface YoutubeTrackDetailsLoader {
  YoutubeTrackDetails loadDetails(HttpInterface httpInterface, String videoId, boolean requireFormats, YoutubeAudioSourceManager sourceManager);
}
