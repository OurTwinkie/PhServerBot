package org.twinkie.phbot.library.lavaplayer.container.mpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.mpeg.reader.MpegFileTrackProvider;
import org.twinkie.phbot.library.lavaplayer.tools.ExceptionTools;
import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.BaseAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.AudioProcessingContext;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.util.List;

import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.FAULT;
import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

/**
 * Audio track that handles the processing of MP4 format
 */
public class MpegAudioTrack extends BaseAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(MpegAudioTrack.class);

  private final SeekableInputStream inputStream;

  /**
   * @param trackInfo Track info
   * @param inputStream Input stream for the MP4 file
   */
  public MpegAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    super(trackInfo);

    this.inputStream = inputStream;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) {
    MpegFileLoader file = new MpegFileLoader(inputStream);
    file.parseHeaders();

    MpegTrackConsumer trackConsumer = loadAudioTrack(file, localExecutor.getProcessingContext());

    try {
      MpegFileTrackProvider fileReader = file.loadReader(trackConsumer);
      if (fileReader == null) {
        throw new FriendlyException("Unknown MP4 format.", SUSPICIOUS, null);
      }

      accurateDuration.set(fileReader.getDuration());

      localExecutor.executeProcessingLoop(fileReader::provideFrames, fileReader::seekToTimecode);
    } finally {
      trackConsumer.close();
    }
  }

  protected MpegTrackConsumer loadAudioTrack(MpegFileLoader file, AudioProcessingContext context) {
    MpegTrackConsumer trackConsumer = null;
    boolean success = false;

    try {
      trackConsumer = selectAudioTrack(file.getTrackList(), context);

      if (trackConsumer == null) {
        StringBuilder error = new StringBuilder();
        error.append("The audio codec used in the track is not supported, options:\n");
        file.getTrackList().forEach(track -> error.append(track.handler).append("|").append(track.codecName).append("\n"));
        throw new FriendlyException(error.toString(), SUSPICIOUS, null);
      } else {
        log.debug("Starting to play track with codec {}", trackConsumer.getTrack().codecName);
      }

      trackConsumer.initialise();
      success = true;
      return trackConsumer;
    } catch (Exception e) {
      throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong when loading an MP4 format track.", FAULT, e);
    } finally {
      if (!success && trackConsumer != null) {
        trackConsumer.close();
      }
    }
  }

  private MpegTrackConsumer selectAudioTrack(List<MpegTrackInfo> tracks, AudioProcessingContext context) {
    for (MpegTrackInfo track : tracks) {
      if ("soun".equals(track.handler) && "mp4a".equals(track.codecName)) {
        return new MpegAacTrackConsumer(context, track);
      }
    }
    return null;
  }
}
