package org.twinkie.phbot.library.lavaplayer.source.youtube.format;

import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeSignatureResolver;
import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeTrackFormat;
import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeTrackJsonData;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.util.List;

public interface OfflineYoutubeTrackFormatExtractor extends YoutubeTrackFormatExtractor {
  List<YoutubeTrackFormat> extract(YoutubeTrackJsonData data);

  @Override
  default List<YoutubeTrackFormat> extract(
      YoutubeTrackJsonData data,
      HttpInterface httpInterface,
      YoutubeSignatureResolver signatureResolver
  ) {
    return extract(data);
  }
}
