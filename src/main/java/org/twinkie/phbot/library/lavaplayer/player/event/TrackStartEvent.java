package org.twinkie.phbot.library.lavaplayer.player.event;

import org.twinkie.phbot.library.lavaplayer.player.AudioPlayer;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;

/**
 * Event that is fired when a track starts playing.
 */
public class TrackStartEvent extends AudioEvent {
  /**
   * Audio track that started
   */
  public final AudioTrack track;

  /**
   * @param player Audio player
   * @param track Audio track that started
   */
  public TrackStartEvent(AudioPlayer player, AudioTrack track) {
    super(player);
    this.track = track;
  }
}
