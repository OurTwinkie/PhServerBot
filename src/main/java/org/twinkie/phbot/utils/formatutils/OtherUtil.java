
package org.twinkie.phbot.utils.formatutils;

import net.dv8tion.jda.api.entities.Member;
import org.bson.Document;
import org.twinkie.phbot.utils.databaseutils.UserInfo;


public class OtherUtil
{

    public static int parseTime(String timestr)
    {
        timestr = timestr.replaceAll("(?i)(\\s|,|and)","")
                .replaceAll("(?is)(-?\\d+|[a-z]+)", "$1 ")
                .trim();
        String[] vals = timestr.split("\\s+");
        int timeinseconds = 0;
        try
        {
            for(int j=0; j<vals.length; j+=2)
            {
                int num = Integer.parseInt(vals[j]);

                if(vals.length > j+1)
                {
                    if(vals[j+1].toLowerCase().startsWith("m"))
                        num*=60;
                    else if(vals[j+1].toLowerCase().startsWith("h"))
                        num*=60*60;
                    else if(vals[j+1].toLowerCase().startsWith("d"))
                        num*=60*60*24;
                }

                timeinseconds+=num;
            }
        }
        catch(Exception ex)
        {
            return -1;
        }
        return timeinseconds;
    }

    public static UserInfo documentToUsersInfo(Document document){
        return new UserInfo(document.getString("id"),
                            document.getLong("minutes"),
                            document.getLong("coins"),
                            document.getLong("lvl"),
                            document.getLong("newRank"),
                            document.getLong("message"),
                            document.getString("guildId"),
                            document.getString("memberId"),
                            document.getLong("rank"));
    }
    public static Document userInfoToDocument(UserInfo usersInfo) {
        Document document = new Document("id", usersInfo.getDatabaseId());
        document.put("minutes",usersInfo.getVoiceMinutes());
        document.put("coins", usersInfo.getMoneyCount());
        document.put("lvl", usersInfo.getLevel());
        document.put("newRank", usersInfo.getNewRank());
        document.put("rank", usersInfo.getRank());
        document.put("message", usersInfo.getMessageCount());
        document.put("guildId", usersInfo.getGuildId());
        document.put("memberId", usersInfo.getMemberId());
        return document;
    }

    public static UserInfo newUserInfo(Member member) {
        return new UserInfo(OtherUtil.getDatabaseId(member),
                            0,
                            0,
                            1,
                            3000,
                            0,
                            member.getGuild().getId(),
                            member.getId(),
                            0);
    }


    public static String getDatabaseId(Member member) {
        return member.getId() + member.getGuild().getId();
    }

    public static String getMemberIdByDatabaseId(String databaseId) {
        return databaseId.substring(0,18);
    }
}
