package org.twinkie.phbot.library.lavaplayer.tools.io;

import org.twinkie.phbot.library.lavaplayer.tools.http.ExtendedHttpConfigurable;

import java.io.Closeable;

/**
 * A thread-safe manager for HTTP interfaces.
 */
public interface HttpInterfaceManager extends ExtendedHttpConfigurable, Closeable {
  /**
   * @return An HTTP interface for use by the current thread.
   */
  HttpInterface getInterface();
}
