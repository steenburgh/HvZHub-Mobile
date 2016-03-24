package com.hvzhub.app.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DB {
    public static final String HUMAN_CHAT = "human";
    public static final String ZOMBIE_CHAT = "zombie";
    public static final String MOD_CHAT = "mod";
    public static final String TAG = "DB";

    private static DB mInstance;
    private final Context mCtx;

    DaoMaster.DevOpenHelper hvzHubDBHelper;
    SQLiteDatabase hvzHubDB;
    DaoMaster daoMaster;
    DaoSession daoSession;
    MessageDao messageDao;
    ChatDao chatDao;

    private DB(Context context) {
        mCtx = context;
        initDatabase();
    }

    public static synchronized DB getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DB(context);
        }
        return mInstance;
    }

    private void initDatabase() {
        hvzHubDBHelper = new DaoMaster.DevOpenHelper(mCtx, "ORM.sqlite", null);
        hvzHubDB = hvzHubDBHelper.getWritableDatabase();

        // Get DaoMaster
        daoMaster = new DaoMaster(hvzHubDB);

        // Create database and tables if non existent
        // Use methods in DaoMaster to create initial database table
        //
        DaoMaster.createAllTables(hvzHubDB, true);

        // Create DaoSession instance
        //Use method in DaoMaster to create a database access session
        daoSession = daoMaster.newSession();

        // From DaoSession instance, get instance of EventDao
        messageDao = daoSession.getMessageDao();
        chatDao = daoSession.getChatDao();
    }

    public void wipeDatabase() {
        chatDao.deleteAll();
        messageDao.deleteAll();
        closeReopenDatabase();
        Log.d(TAG, "Wiped all tables.");
    }


    private void createChat(String name) {
        chatDao.insert(new Chat(
                null,
                name
        ));
        closeReopenDatabase();
    }

    private Chat getChat(String chatName) {
        List<Chat> chats = chatDao.queryBuilder()
                .where(ChatDao.Properties.Name.eq(chatName))
                .list();

        // If the chat doesn't exist, create it
        if (chats.isEmpty()) {
            createChat(chatName);
            chats = chatDao.queryBuilder()
                    .where(ChatDao.Properties.Name.eq(chatName))
                    .list();
        }

        return chats.get(0);
    }

    public List<Message> getMessages(String chatName) {
        Chat chat = getChat(chatName);
        List<Message> messages = chat.getMessages();

        // (cont.) If list is null, then database tables were created for first time in
        // (cont.) previous lines, so call "closeReopenDatabase()"
        if (messages == null) {
            closeReopenDatabase();
            return new LinkedList<>();
        } else if (messages.isEmpty()) {
            // If messageList is empty, return an empty LinkedList instead of an EmptyList
            return new LinkedList<>();
        } else {
            return messages;
        }
    }

    public com.hvzhub.app.API.model.Chat.Message addMessageToChat(com.hvzhub.app.API.model.Chat.Message message, String chatName) {
        addMessageToChat(
                message.userId,
                message.name,
                message.message,
                message.timestamp,
                message.msgId,
                chatName
        );
        return message;
    }

    public void addMessageToChat(
            int userId,
            String name,
            String message,
            Date timestamp,
            int msgId,
            String chatName
    ) {
        openDatabase();
        Chat chat = getChat(chatName);
        List<Message> messages = chat.getMessages();
        Message msgObject = new Message(
                null,
                userId,
                name,
                message,
                timestamp,
                msgId,
                chat.getId()
        );
        daoSession.insert(msgObject);

        // If messages is empty, greenDAO returns an immutable, empty list object
        // To fix this up, we need to reset
        if (messages.isEmpty()) {
            chat.resetMessages(); // This also adds the message object to the list
        } else {
            messages.add(msgObject);
        }

        closeReopenDatabase();
    }

    private void closeReopenDatabase() {
        closeDatabase();
        openDatabase();
    }

    private void closeDatabase() {
        daoSession.clear();
        hvzHubDB.close();
        hvzHubDBHelper.close();
    }

    public void openDatabase() {
        hvzHubDBHelper = new DaoMaster.DevOpenHelper(mCtx, "ORM.sqlite", null);
        hvzHubDB = hvzHubDBHelper.getWritableDatabase();

        //Get DaoMaster
        daoMaster = new DaoMaster(hvzHubDB);

        // Create DaoSession instance
        //Use method in DaoMaster to create a database access session
        daoSession = daoMaster.newSession();

        // From DaoSession instance, get instance of EventDao
        messageDao = daoSession.getMessageDao();
        chatDao = daoSession.getChatDao();
    }
}
