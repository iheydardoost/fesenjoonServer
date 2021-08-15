CREATE TABLE "User" (
  "userID" bigint UNIQUE,
  "firstName" text,
  "lastName" text,
  "userName" text UNIQUE,
  "passwordHash" integer,
  "dateOfBirth" date,
  "email" text UNIQUE,
  "phoneNumber" text,
  "bio" text,
  "lastSeenStatus" integer,
  "lastSeen" timestamp,
  "accountPrivate" boolean,
  "accountActive" boolean,
  "userImage" bytea,
  PRIMARY KEY ("userID")
);

CREATE TABLE "Tweet" (
  "tweetText" text,
  "tweetDateTime" timestamp,
  "userID" bigint,
  "tweetID" bigint UNIQUE,
  "parentTweetID" bigint,
  "retweeted" boolean,
  "reportedNumber" integer,
  "tweetImage" bytea,
  PRIMARY KEY ("tweetID"),
  CONSTRAINT "FK_Tweet.userID"
    FOREIGN KEY ("userID")
      REFERENCES "User"("userID")
);


CREATE TABLE "like/spam" (
  "TweetID" bigint ,
  "userID" bigint ,
  "actionType" integer,
  CONSTRAINT "FK_like/spam.userID"
    FOREIGN KEY ("userID")
      REFERENCES "User"("userID"),
  CONSTRAINT "FK_like/spam.TweetID"
    FOREIGN KEY ("TweetID")
      REFERENCES "Tweet"("tweetID")
);

CREATE TABLE "collection" (
  "ownerID" bigint ,
  "collectionID" bigint ,
  "memberID" bigint ,
  CONSTRAINT "FK_collection.ownerID"
    FOREIGN KEY ("ownerID")
      REFERENCES "User"("userID")
);

CREATE TABLE "chat" (
  "chatID" bigint UNIQUE,
  "chatType" integer,
  PRIMARY KEY ("chatID")
);

CREATE TABLE "chatMember" (
  "chatID" bigint ,
  "memberID" bigint ,
  CONSTRAINT "FK_chatMember.chatID"
    FOREIGN KEY ("chatID")
      REFERENCES "chat"("chatID"),
  CONSTRAINT "FK_chatMember.memberID"
    FOREIGN KEY ("memberID")
      REFERENCES "User"("userID")
);


CREATE TABLE "message" (
  "userID" bigint ,
  "chatID" bigint ,
  "msgID" bigint UNIQUE,
  "msgText" text,
  "msgImage" bytea,
  "msgDateTime" timestamp,
  "forwarded" boolean,
  "msgStatus" integer,
  PRIMARY KEY ("msgID"),
  CONSTRAINT "FK_message.userID"
    FOREIGN KEY ("userID")
      REFERENCES "User"("userID"),
  CONSTRAINT "FK_message.chatID"
    FOREIGN KEY ("chatID")
      REFERENCES "chat"("chatID")
);

CREATE TABLE "notifications" (
  "subjectID" bigint,
  "objectID" bigint,
  "notificationType" integer,
  CONSTRAINT "FK_notifications.objectID"
    FOREIGN KEY ("objectID")
      REFERENCES "User"("userID"),
  CONSTRAINT "FK_notifications.subjectID"
    FOREIGN KEY ("subjectID")
      REFERENCES "User"("userID")
);

CREATE TABLE "relationList" (
  "subjectID" bigint,
  "objectID" bigint,
  "relationType" integer,
  CONSTRAINT "FK_relationList.subjectID"
    FOREIGN KEY ("subjectID")
      REFERENCES "User"("userID"),
  CONSTRAINT "FK_relationList.objectID"
    FOREIGN KEY ("objectID")
      REFERENCES "User"("userID")
);