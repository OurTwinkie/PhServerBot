package org.twinkie.phbot.library.lavaplayer.filter.equalizer;

import org.twinkie.phbot.library.lavaplayer.filter.AudioFilter;
import org.twinkie.phbot.library.lavaplayer.filter.PcmFilterFactory;
import org.twinkie.phbot.library.lavaplayer.filter.UniversalPcmAudioFilter;
import org.twinkie.phbot.library.lavaplayer.format.AudioDataFormat;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.List;

/**
 * PCM filter factory which creates a single {@link Equalizer} filter for every track. Useful in case the equalizer is
 * the only custom filter used.
 */
public class EqualizerFactory extends EqualizerConfiguration implements PcmFilterFactory {
  /**
   * Creates a new instance no gains applied initially.
   */
  public EqualizerFactory() {
    super(new float[Equalizer.BAND_COUNT]);
  }

  @Override
  public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
    if (Equalizer.isCompatible(format)) {
      return Collections.singletonList(new Equalizer(format.channelCount, output, bandMultipliers));
    } else {
      return Collections.emptyList();
    }
  }
}
