package org.twinkie.phbot.library.lavaplayer.format.transcoder;

import org.twinkie.phbot.library.lavaplayer.format.AudioDataFormat;
import org.twinkie.phbot.library.lavaplayer.natives.opus.OpusDecoder;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Audio chunk decoder for Opus codec.
 */
public class OpusChunkDecoder implements AudioChunkDecoder {
  private final OpusDecoder decoder;
  private final ByteBuffer encodedBuffer;

  /**
   * @param format Source audio format.
   */
  public OpusChunkDecoder(AudioDataFormat format) {
    encodedBuffer = ByteBuffer.allocateDirect(4096);
    decoder = new OpusDecoder(format.sampleRate, format.channelCount);
  }

  @Override
  public void decode(byte[] encoded, ShortBuffer buffer) {
    encodedBuffer.clear();
    encodedBuffer.put(encoded);
    encodedBuffer.flip();

    buffer.clear();
    decoder.decode(encodedBuffer, buffer);
  }

  @Override
  public void close() {
    decoder.close();
  }
}
