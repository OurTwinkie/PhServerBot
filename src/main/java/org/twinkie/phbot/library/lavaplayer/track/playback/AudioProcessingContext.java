package org.twinkie.phbot.library.lavaplayer.track.playback;

import org.twinkie.phbot.library.lavaplayer.format.AudioDataFormat;
import org.twinkie.phbot.library.lavaplayer.player.AudioConfiguration;
import org.twinkie.phbot.library.lavaplayer.player.AudioPlayerOptions;

/**
 * Context for processing audio. Contains configuration for encoding and the output where the frames go to.
 */
public class AudioProcessingContext {
  /**
   * Audio encoding or filtering related configuration
   */
  public final AudioConfiguration configuration;
  /**
   * Consumer for the produced audio frames
   */
  public final AudioFrameBuffer frameBuffer;
  /**
   * Mutable volume level for the audio
   */
  public final AudioPlayerOptions playerOptions;
  /**
   * Output format to use throughout this processing cycle
   */
  public final AudioDataFormat outputFormat;
  /**
   * Whether filter factory change is applied to already playing tracks.
   */
  public final boolean filterHotSwapEnabled;

  /**
   * @param configuration Audio encoding or filtering related configuration
   * @param frameBuffer Frame buffer for the produced audio frames
   * @param playerOptions State of the audio player.
   * @param outputFormat Output format to use throughout this processing cycle
   */
  public AudioProcessingContext(AudioConfiguration configuration, AudioFrameBuffer frameBuffer,
                                AudioPlayerOptions playerOptions, AudioDataFormat outputFormat) {

    this.configuration = configuration;
    this.frameBuffer = frameBuffer;
    this.playerOptions = playerOptions;
    this.outputFormat = outputFormat;
    this.filterHotSwapEnabled = configuration.isFilterHotSwapEnabled();
  }
}
