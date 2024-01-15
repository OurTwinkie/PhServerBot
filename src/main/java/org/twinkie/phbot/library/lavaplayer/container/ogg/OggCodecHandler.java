package org.twinkie.phbot.library.lavaplayer.container.ogg;

import org.twinkie.phbot.library.lavaplayer.tools.io.DirectBufferStreamBroker;

import java.io.IOException;

public interface OggCodecHandler {
  boolean isMatchingIdentifier(int identifier);

  int getMaximumFirstPacketLength();

  OggTrackBlueprint loadBlueprint(OggPacketInputStream stream, DirectBufferStreamBroker broker) throws IOException;

  OggMetadata loadMetadata(OggPacketInputStream stream, DirectBufferStreamBroker broker) throws IOException;
}
