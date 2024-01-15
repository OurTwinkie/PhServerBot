package org.twinkie.phbot.utils.databaseUtils;

import lombok.Getter;

@Getter
public class UserInfo {

    private final String databaseId;
    private long voiceMinutes;
    private long moneyCount;
    private long level;
    private long newRank;
    private long messageCount;
    private final String guildId;
    private final String memberId;
    private long rank;

    public UserInfo(String databaseId, long voiceMinutes, long moneyCount, long level, long newRank, long messageCount, String guildId, String memberId, long rank) {
        this.databaseId = databaseId;
        this.voiceMinutes = voiceMinutes;
        this.moneyCount = moneyCount;
        this.level = level;
        this.newRank = newRank;
        this.messageCount = messageCount;
        this.guildId = guildId;
        this.memberId = memberId;
        this.rank = rank;
    }
}

