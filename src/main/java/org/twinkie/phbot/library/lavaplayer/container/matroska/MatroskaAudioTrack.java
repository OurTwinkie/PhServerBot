package org.twinkie.phbot.library.lavaplayer.container.matroska;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.matroska.format.MatroskaFileTrack;
import org.twinkie.phbot.library.lavaplayer.tools.ExceptionTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.BaseAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.AudioProcessingContext;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.io.IOException;

/**
 * Audio track that handles the processing of MKV and WEBM formats
 */
public class MatroskaAudioTrack extends BaseAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(MatroskaAudioTrack.class);

  private final SeekableInputStream inputStream;

  /**
   * @param trackInfo Track info
   * @param inputStream Input stream for the file
   */
  public MatroskaAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    super(trackInfo);

    this.inputStream = inputStream;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) {
    MatroskaStreamingFile file = loadMatroskaFile();
    MatroskaTrackConsumer trackConsumer = loadAudioTrack(file, localExecutor.getProcessingContext());

    try {
      localExecutor.executeProcessingLoop(() -> {
        file.provideFrames(trackConsumer);
      }, position -> {
        file.seekToTimecode(trackConsumer.getTrack().index, position);
      });
    } finally {
      ExceptionTools.closeWithWarnings(trackConsumer);
    }
  }

  private MatroskaStreamingFile loadMatroskaFile() {
    try {
      MatroskaStreamingFile file = new MatroskaStreamingFile(inputStream);
      file.readFile();

      accurateDuration.set((int) file.getDuration());
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private MatroskaTrackConsumer loadAudioTrack(MatroskaStreamingFile file, AudioProcessingContext context) {
    MatroskaTrackConsumer trackConsumer = null;
    boolean success = false;

    try {
      trackConsumer = selectAudioTrack(file.getTrackList(), context);

      if (trackConsumer == null) {
        throw new IllegalStateException("No supported audio tracks in the file.");
      } else {
        log.debug("Starting to play track with codec {}", trackConsumer.getTrack().codecId);
      }

      trackConsumer.initialise();
      success = true;
    } finally {
      if (!success && trackConsumer != null) {
        ExceptionTools.closeWithWarnings(trackConsumer);
      }
    }

    return trackConsumer;
  }

  private MatroskaTrackConsumer selectAudioTrack(MatroskaFileTrack[] tracks, AudioProcessingContext context) {
    MatroskaTrackConsumer trackConsumer = null;

    for (MatroskaFileTrack track : tracks) {
      if (track.type == MatroskaFileTrack.Type.AUDIO) {
        if (MatroskaContainerProbe.OPUS_CODEC.equals(track.codecId)) {
          trackConsumer = new MatroskaOpusTrackConsumer(context, track);
          break;
        } else if (MatroskaContainerProbe.VORBIS_CODEC.equals(track.codecId)) {
          trackConsumer = new MatroskaVorbisTrackConsumer(context, track);
        } else if (MatroskaContainerProbe.AAC_CODEC.equals(track.codecId)) {
          trackConsumer = new MatroskaAacTrackConsumer(context, track);
        }
      }
    }

    return trackConsumer;
  }
}
