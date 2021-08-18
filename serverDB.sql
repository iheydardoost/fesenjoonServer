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
      ON DELETE CASCADE
);


CREATE TABLE "Like/Spam" (
  "tweetID" bigint ,
  "userID" bigint ,
  "actionType" integer,
  CONSTRAINT "FK_Like/Spam.userID"
    FOREIGN KEY ("userID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE,
  CONSTRAINT "FK_Like/Spam.TweetID"
    FOREIGN KEY ("TweetID")
      REFERENCES "Tweet"("tweetID")
);

CREATE TABLE "Collection" (
  "ownerID" bigint ,
  "collectionID" bigint ,
  "memberID" bigint ,
  CONSTRAINT "FK_Collection.ownerID"
    FOREIGN KEY ("ownerID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE
);

CREATE TABLE "Chat" (
  "chatID" bigint UNIQUE,
  "chatType" integer,
  PRIMARY KEY ("chatID")
);

CREATE TABLE "ChatMember" (
  "chatID" bigint ,
  "memberID" bigint ,
  CONSTRAINT "FK_ChatMember.chatID"
    FOREIGN KEY ("chatID")
      REFERENCES "Chat"("chatID"),
  CONSTRAINT "FK_ChatMember.memberID"
    FOREIGN KEY ("memberID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE
);


CREATE TABLE "Message" (
  "userID" bigint ,
  "chatID" bigint ,
  "msgID" bigint UNIQUE,
  "msgText" text,
  "msgImage" bytea,
  "msgDateTime" timestamp,
  "forwarded" boolean,
  "msgStatus" integer,
  PRIMARY KEY ("msgID"),
  CONSTRAINT "FK_Message.userID"
    FOREIGN KEY ("userID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE,
  CONSTRAINT "FK_Message.chatID"
    FOREIGN KEY ("chatID")
      REFERENCES "Chat"("chatID")
);

CREATE TABLE "Notification" (
  "subjectID" bigint,
  "objectID" bigint,
  "notificationType" integer,
  CONSTRAINT "FK_Notification.objectID"
    FOREIGN KEY ("objectID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE,
  CONSTRAINT "FK_Notification.subjectID"
    FOREIGN KEY ("subjectID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE
);

CREATE TABLE "Relation" (
  "subjectID" bigint,
  "objectID" bigint,
  "relationType" integer,
  CONSTRAINT "FK_Relation.subjectID"
    FOREIGN KEY ("subjectID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE,
  CONSTRAINT "FK_Relation.objectID"
    FOREIGN KEY ("objectID")
      REFERENCES "User"("userID")
      ON DELETE CASCADE
);