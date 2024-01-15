package org.twinkie.phbot.library.lavaplayer.source.soundcloud;

import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.io.IOException;

public interface SoundCloudDataLoader {
  JsonBrowser load(HttpInterface httpInterface, String url) throws IOException;
}
