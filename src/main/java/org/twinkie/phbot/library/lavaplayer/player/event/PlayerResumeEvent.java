package org.twinkie.phbot.library.lavaplayer.player.event;

import org.twinkie.phbot.library.lavaplayer.player.AudioPlayer;

/**
 * Event that is fired when a player is resumed.
 */
public class PlayerResumeEvent extends AudioEvent {
  /**
   * @param player Audio player
   */
  public PlayerResumeEvent(AudioPlayer player) {
    super(player);
  }
}
