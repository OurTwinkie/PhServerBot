package org.twinkie.phbot.library.lavaplayer.container.ogg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.BaseAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.AudioProcessingContext;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.io.IOException;

import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

/**
 * Audio track which handles an OGG stream.
 */
public class OggAudioTrack extends BaseAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(OggAudioTrack.class);

  private final SeekableInputStream inputStream;

  /**
   * @param trackInfo Track info
   * @param inputStream Input stream for the OGG stream
   */
  public OggAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    super(trackInfo);

    this.inputStream = inputStream;
  }

  @Override
  public void process(final LocalAudioTrackExecutor localExecutor) throws IOException {
    OggPacketInputStream packetInputStream = new OggPacketInputStream(inputStream, false);
    OggTrackBlueprint blueprint = OggTrackLoader.loadTrackBlueprint(packetInputStream);

    log.debug("Starting to play an OGG track {}", getIdentifier());

    if (blueprint == null) {
      throw new IOException("Stream terminated before the first packet.");
    }

    OggTrackHandler handler = blueprint.loadTrackHandler(packetInputStream);
    localExecutor.executeProcessingLoop(() -> {
      try {
        processTrackLoop(packetInputStream, localExecutor.getProcessingContext(), handler, blueprint);
      } catch (IOException e) {
        throw new FriendlyException("Stream broke when playing OGG track.", SUSPICIOUS, e);
      }
    }, handler::seekToTimecode, true);
  }

  private void processTrackLoop(OggPacketInputStream packetInputStream, AudioProcessingContext context, OggTrackHandler handler, OggTrackBlueprint blueprint) throws IOException, InterruptedException {
    while (blueprint != null) {
      handler.initialise(context, 0, 0);
      handler.provideFrames();
      blueprint = OggTrackLoader.loadTrackBlueprint(packetInputStream);
    }
  }
}
