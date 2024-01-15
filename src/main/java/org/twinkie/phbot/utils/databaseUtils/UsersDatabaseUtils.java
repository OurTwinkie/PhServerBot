package org.twinkie.phbot.utils.databaseUtils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Member;
import org.bson.Document;
import org.twinkie.phbot.Main;
import org.twinkie.phbot.utils.FormatUtils.OtherUtil;

public class UsersDatabaseUtils {
    private UsersDatabaseUtils() {
        throw new IllegalStateException("Utility class");
    }
    public static UserInfo getUserInfo(Member member) {
        MongoDatabase mongoDatabase = Main.getMongoClient().getDatabase("PHBot");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("Users");
        Document document = mongoCollection.find(new Document("id",OtherUtil.getDatabaseId(member))).first();
        if (document == null) {
            UserInfo usersInfo = OtherUtil.newUserInfo(member);
            createNewUser(usersInfo);
            return usersInfo;
        }
        return OtherUtil.documentToUsersInfo(document);
    }

    public static void setUserInfo(UserInfo usersInfo) {
        MongoDatabase mongoDatabase = Main.getMongoClient().getDatabase("PHBot");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("Users");
        Document document = mongoCollection.find(new Document("id", usersInfo.getDatabaseId())).first();
        if (document == null) {
            createNewUser(usersInfo);
            return;
        }
        document = OtherUtil.userInfoToDocument(usersInfo);
        mongoCollection.replaceOne(Filters.eq("id", usersInfo.getDatabaseId()), document);
    }

    public static void createNewUser(UserInfo usersInfo) {
        MongoDatabase mongoDatabase = Main.getMongoClient().getDatabase("PHBot");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("Users");
        Document document = mongoCollection.find(new Document("id", usersInfo.getDatabaseId())).first();
        if (document == null) {
            document = OtherUtil.userInfoToDocument(usersInfo);
            mongoCollection.insertOne(document);
        }
    }
}
