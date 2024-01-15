package org.twinkie.phbot.library.lavaplayer.player.hook;

import org.twinkie.phbot.library.lavaplayer.player.AudioPlayer;
import org.twinkie.phbot.library.lavaplayer.track.playback.AudioFrame;

/**
 * Hook for intercepting outgoing audio frames from AudioPlayer.
 */
public interface AudioOutputHook {
  /**
   * @param player Audio player where the frame is coming from
   * @param frame Audio frame
   * @return The frame to pass onto the actual caller
   */
  AudioFrame outgoingFrame(AudioPlayer player, AudioFrame frame);
}
