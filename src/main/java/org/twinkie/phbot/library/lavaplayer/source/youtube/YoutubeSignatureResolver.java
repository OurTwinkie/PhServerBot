package org.twinkie.phbot.library.lavaplayer.source.youtube;

import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.io.IOException;
import java.net.URI;

public interface YoutubeSignatureResolver {
  YoutubeSignatureCipher getExtractedScript(HttpInterface httpInterface, String playerScript) throws IOException;

  URI resolveFormatUrl(HttpInterface httpInterface, String playerScript, YoutubeTrackFormat format) throws Exception;

  String resolveDashUrl(HttpInterface httpInterface, String playerScript, String dashUrl) throws Exception;
}
