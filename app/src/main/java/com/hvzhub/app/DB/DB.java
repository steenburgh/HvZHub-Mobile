package com.hvzhub.app.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.util.List;

public class DB {
    private static DB mInstance;
    private final Context mCtx;

    DaoMaster.DevOpenHelper hvzHubDBHelper;
    SQLiteDatabase hvzHubDB;
    DaoMaster daoMaster;
    DaoSession daoSession;
    MessageDao messageDao;
    ChatDao chatDao;
    List<Message> messageList;

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
        //Use methods in DaoMaster to create initial database table
        //
        DaoMaster.createAllTables(hvzHubDB, true);

        // Create DaoSession instance
        //Use method in DaoMaster to create a database access session
        daoSession = daoMaster.newSession();

        // From DaoSession instance, get instance of EventDao
        messageDao = daoSession.getMessageDao();
        chatDao = daoSession.getChatDao();
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
        return chats.get(0);
    }

    private List<Message> getMessages(String chatName) {
        Chat chat = getChat(chatName);
        messageList = chat.getMessages();

        // (cont.) If list is null, then database tables were created for first time in
        // (cont.) previous lines, so call "closeReopenDatabase()"
        if (messageList == null) {
            closeReopenDatabase();
        }
        return messageList;
    }

    public Message addMessageToChat(
            int userId,
            String name,
            String message,
            Date timestamp,
            int msgId,
            String chatName
    ) {
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
        messages.add(msgObject);

        //Close and reopen database to ensure object is saved
        closeReopenDatabase();

        return msgObject;
    }

    private void closeDatabase() {
        daoSession.clear();
        hvzHubDB.close();
        hvzHubDBHelper.close();
    }

    private void closeReopenDatabase() {
        closeDatabase();

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
