package org.twinkie.phbot.library.lavaplayer.player;

import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.track.AudioPlaylist;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;

import java.util.function.Consumer;

/**
 * Helper class for creating an audio result handler using only methods that can be passed as lambdas.
 */
public class FunctionalResultHandler implements AudioLoadResultHandler {
  private final Consumer<AudioTrack> trackConsumer;
  private final Consumer<AudioPlaylist> playlistConsumer;
  private final Runnable emptyResultHandler;
  private final Consumer<FriendlyException> exceptionConsumer;

  /**
   * Refer to {@link AudioLoadResultHandler} methods for details on when each method is called.
   *
   * @param trackConsumer Consumer for single track result
   * @param playlistConsumer Consumer for playlist result
   * @param emptyResultHandler Empty result handler
   * @param exceptionConsumer Consumer for an exception when loading the item fails
   */
  public FunctionalResultHandler(Consumer<AudioTrack> trackConsumer, Consumer<AudioPlaylist> playlistConsumer,
                                 Runnable emptyResultHandler, Consumer<FriendlyException> exceptionConsumer) {

    this.trackConsumer = trackConsumer;
    this.playlistConsumer = playlistConsumer;
    this.emptyResultHandler = emptyResultHandler;
    this.exceptionConsumer = exceptionConsumer;
  }

  @Override
  public void trackLoaded(AudioTrack track) {
    if (trackConsumer != null) {
      trackConsumer.accept(track);
    }
  }

  @Override
  public void playlistLoaded(AudioPlaylist playlist) {
    if (playlistConsumer != null) {
      playlistConsumer.accept(playlist);
    }
  }

  @Override
  public void noMatches() {
    if (emptyResultHandler != null) {
      emptyResultHandler.run();
    }
  }

  @Override
  public void loadFailed(FriendlyException exception) {
    if (exceptionConsumer != null) {
      exceptionConsumer.accept(exception);
    }
  }
}
