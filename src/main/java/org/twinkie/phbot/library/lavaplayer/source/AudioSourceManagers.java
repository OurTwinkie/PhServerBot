package org.twinkie.phbot.library.lavaplayer.source;

import org.twinkie.phbot.library.lavaplayer.container.MediaContainerRegistry;
import org.twinkie.phbot.library.lavaplayer.player.AudioPlayerManager;
import org.twinkie.phbot.library.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.beam.BeamAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.http.HttpAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.local.LocalAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.yamusic.YandexMusicAudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.source.youtube.YoutubeAudioSourceManager;

/**
 * A helper class for registering built-in source managers to a player manager.
 */
public class AudioSourceManagers {
  /**
   * See {@link #registerRemoteSources(AudioPlayerManager, MediaContainerRegistry)}, but with default containers.
   */
  public static void registerRemoteSources(AudioPlayerManager playerManager) {
    registerRemoteSources(playerManager, MediaContainerRegistry.DEFAULT_REGISTRY);
  }

  /**
   * Registers all built-in remote audio sources to the specified player manager. Local file audio source must be
   * registered separately.
   *
   * @param playerManager Player manager to register the source managers to
   * @param containerRegistry Media container registry to be used by any probing sources.
   */
  public static void registerRemoteSources(AudioPlayerManager playerManager, MediaContainerRegistry containerRegistry) {
    playerManager.registerSourceManager(new YoutubeAudioSourceManager(true, null, null));
    playerManager.registerSourceManager(new YandexMusicAudioSourceManager(true));
    playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
    playerManager.registerSourceManager(new BandcampAudioSourceManager());
    playerManager.registerSourceManager(new VimeoAudioSourceManager());
    playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
    playerManager.registerSourceManager(new BeamAudioSourceManager());
    playerManager.registerSourceManager(new GetyarnAudioSourceManager());
    playerManager.registerSourceManager(new HttpAudioSourceManager(containerRegistry));
  }

  /**
   * Registers the local file source manager to the specified player manager.
   *
   * @param playerManager Player manager to register the source manager to
   */
  public static void registerLocalSource(AudioPlayerManager playerManager) {
    registerLocalSource(playerManager, MediaContainerRegistry.DEFAULT_REGISTRY);
  }

  /**
   * Registers the local file source manager to the specified player manager.
   *
   * @param playerManager Player manager to register the source manager to
   * @param containerRegistry Media container registry to be used by the local source.
   */
  public static void registerLocalSource(AudioPlayerManager playerManager, MediaContainerRegistry containerRegistry) {
    playerManager.registerSourceManager(new LocalAudioSourceManager(containerRegistry));
  }
}
