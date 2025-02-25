package org.twinkie.phbot.library.lavaplayer.player;

import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.track.AudioPlaylist;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;

/**
 * Handles the result of loading an item from an audio player manager.
 */
public interface AudioLoadResultHandler {
  /**
   * Called when the requested item is a track and it was successfully loaded.
   * @param track The loaded track
   */
  void trackLoaded(AudioTrack track);

  /**
   * Called when the requested item is a playlist and it was successfully loaded.
   * @param playlist The loaded playlist
   */
  void playlistLoaded(AudioPlaylist playlist);

  /**
   * Called when there were no items found by the specified identifier.
   */
  void noMatches();

  /**
   * Called when loading an item failed with an exception.
   * @param exception The exception that was thrown
   */
  void loadFailed(FriendlyException exception);
}
