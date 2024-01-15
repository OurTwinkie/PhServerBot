package org.twinkie.phbot.library.lavaplayer.container.playlists;

import org.twinkie.phbot.library.lavaplayer.source.stream.M3uStreamSegmentUrlProvider;
import org.twinkie.phbot.library.lavaplayer.source.stream.MpegTsM3uStreamAudioTrack;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterfaceManager;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

public class HlsStreamTrack extends MpegTsM3uStreamAudioTrack {
  private final HlsStreamSegmentUrlProvider segmentUrlProvider;
  private final HttpInterfaceManager httpInterfaceManager;

  /**
   * @param trackInfo Track info
   * @param httpInterfaceManager
   */
  public HlsStreamTrack(AudioTrackInfo trackInfo, String streamUrl, HttpInterfaceManager httpInterfaceManager,
                        boolean isInnerUrl) {

    super(trackInfo);

    segmentUrlProvider = isInnerUrl ?
        new HlsStreamSegmentUrlProvider(null, streamUrl) :
        new HlsStreamSegmentUrlProvider(streamUrl, null);

    this.httpInterfaceManager = httpInterfaceManager;
  }

  @Override
  protected M3uStreamSegmentUrlProvider getSegmentUrlProvider() {
    return segmentUrlProvider;
  }

  @Override
  protected HttpInterface getHttpInterface() {
    return httpInterfaceManager.getInterface();
  }
}
