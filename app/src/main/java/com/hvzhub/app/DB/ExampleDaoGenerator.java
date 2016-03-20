package com.hvzhub.app.DB;

import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;


public class ExampleDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.hvzhub.app"); //Scheme for GreenDAO ORM
        createDB(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java/");
    }

    private static void createDB(Schema schema) {

        //Add Guest
        Entity message = schema.addEntity("Message");
        message.addIdProperty();
        message.addIntProperty("userId").notNull();
        message.addStringProperty("name");
        message.addStringProperty("message");
        Property timestamp = message.addDateProperty("timestamp").notNull().getProperty();
        message.addIntProperty("msgId").notNull();
        Property chatId = message.addLongProperty("chatId").notNull().getProperty();


        // Add relation
        Entity chat = schema.addEntity("Chat");
        chat.addIdProperty();
        chat.addStringProperty("name"); // Zombie human or mod
        ToMany chatToMessages = chat.addToMany(message, chatId);
        chatToMessages.setName("messages");
        chatToMessages.orderAsc(timestamp); // Newest messages go at the bottom
    }
}