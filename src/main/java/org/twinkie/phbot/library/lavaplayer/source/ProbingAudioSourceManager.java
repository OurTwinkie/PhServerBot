package org.twinkie.phbot.library.lavaplayer.source;

import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDescriptor;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerRegistry;
import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.track.AudioItem;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.COMMON;

/**
 * The base class for audio sources which use probing to detect container type.
 */
public abstract class ProbingAudioSourceManager implements AudioSourceManager {
  private static final char PARAMETERS_SEPARATOR = '|';

  protected final MediaContainerRegistry containerRegistry;

  protected ProbingAudioSourceManager(MediaContainerRegistry containerRegistry) {
    this.containerRegistry = containerRegistry;
  }

  protected AudioItem handleLoadResult(MediaContainerDetectionResult result) {
    if (result != null) {
      if (result.isReference()) {
        return result.getReference();
      } else if (!result.isContainerDetected()) {
        throw new FriendlyException("Unknown file format.", COMMON, null);
      } else if (!result.isSupportedFile()) {
        throw new FriendlyException(result.getUnsupportedReason(), COMMON, null);
      } else {
        return createTrack(result.getTrackInfo(), result.getContainerDescriptor());
      }
    }

    return null;
  }

  protected abstract AudioTrack createTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerTrackFactory);

  protected void encodeTrackFactory(MediaContainerDescriptor factory, DataOutput output) throws IOException {
    String probeInfo = factory.probe.getName() + (factory.parameters != null ? PARAMETERS_SEPARATOR +
        factory.parameters : "");

    output.writeUTF(probeInfo);
  }

  protected MediaContainerDescriptor decodeTrackFactory(DataInput input) throws IOException {
    String probeInfo = input.readUTF();
    int separatorPosition = probeInfo.indexOf(PARAMETERS_SEPARATOR);

    String probeName = separatorPosition < 0 ? probeInfo : probeInfo.substring(0, separatorPosition);
    String parameters = separatorPosition < 0 ? null : probeInfo.substring(separatorPosition + 1);

    MediaContainerProbe probe = containerRegistry.find(probeName);
    if (probe != null) {
      return new MediaContainerDescriptor(probe, parameters);
    }

    return null;
  }
}
