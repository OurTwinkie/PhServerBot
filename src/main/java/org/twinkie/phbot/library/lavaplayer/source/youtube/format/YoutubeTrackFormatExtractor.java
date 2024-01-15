package org.twinkie.phbot.library.lavaplayer.source.youtube.format;

import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeSignatureResolver;
import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeTrackFormat;
import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeTrackJsonData;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.util.List;

public interface YoutubeTrackFormatExtractor {
  String DEFAULT_SIGNATURE_KEY = "signature";

  List<YoutubeTrackFormat> extract(
      YoutubeTrackJsonData response,
      HttpInterface httpInterface,
      YoutubeSignatureResolver signatureResolver
  );
}
