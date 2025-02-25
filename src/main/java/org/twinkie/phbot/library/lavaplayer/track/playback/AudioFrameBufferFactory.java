package org.twinkie.phbot.library.lavaplayer.track.playback;

import org.twinkie.phbot.library.lavaplayer.format.AudioDataFormat;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory for audio frame buffers.
 */
public interface AudioFrameBufferFactory {
  /**
   * @param bufferDuration Maximum duration of the buffer. The buffer may actually hold less in case the average size of
   *                       frames exceeds {@link AudioDataFormat#expectedChunkSize()}.
   * @param format The format of the frames held in this buffer.
   * @param stopping Atomic boolean which has true value when the track is in a state of pending stop.
   * @return A new frame buffer instance.
   */
  AudioFrameBuffer create(int bufferDuration, AudioDataFormat format, AtomicBoolean stopping);
}
