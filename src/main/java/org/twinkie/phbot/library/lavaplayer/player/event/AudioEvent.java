package org.twinkie.phbot.library.lavaplayer.player.event;

import org.twinkie.phbot.library.lavaplayer.player.AudioPlayer;

/**
 * An event related to an audio player.
 */
public abstract class AudioEvent {
  /**
   * The related audio player.
   */
  public final AudioPlayer player;

  /**
   * @param player The related audio player.
   */
  public AudioEvent(AudioPlayer player) {
    this.player = player;
  }
}
