package org.twinkie.phbot.library.lavaplayer.source.soundcloud;

public interface SoundCloudTrackFormat {
  String getTrackId();

  String getProtocol();

  String getMimeType();

  String getLookupUrl();
}
