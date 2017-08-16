package ru.otus_matveev_anton.json_message_system;

import com.google.gson.*;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.Message;
import ru.otus_matveev_anton.genaral.MessageFormatException;

import java.lang.reflect.Type;

public class JsonMessage extends Message<String>{

    private static final String FIELD_NAME_FROM = "from";
    private static final String FIELD_NAME_TO = "to";
    private static final String FIELD_NAME_ADDRESS = "address";
    private static final String FIELD_NAME_GROUP_NAME = "groupName";
    private static final String FIELD_NAME_DATA_CLASS = "dataClass";
    private static final String FIELD_NAME_DATA = "data";

    private final static GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(JsonMessage.class, new jsonMessageSerializer());
    public static final String MESSAGE_SEPARATOR = "\n\n";

    private final static class jsonMessageSerializer implements JsonSerializer<JsonMessage>, JsonDeserializer<JsonMessage>{
        @Override
        public JsonElement serialize(JsonMessage message, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(FIELD_NAME_FROM, getJsonObjectFromAddressee(message.from));
            jsonObject.add(FIELD_NAME_TO, getJsonObjectFromAddressee(message.to));
            if (message.data != null){
                jsonObject.addProperty(FIELD_NAME_DATA_CLASS, message.data.getClass().getCanonicalName());
                jsonObject.add(FIELD_NAME_DATA, context.serialize(message.data));
            }
            return jsonObject;
        }

        private JsonObject getJsonObjectFromAddressee(Addressee addressee){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(FIELD_NAME_ADDRESS, String.valueOf(addressee.getAddress()));
            jsonObject.addProperty(FIELD_NAME_GROUP_NAME, addressee.getGroupName());
            return jsonObject;
        }

        @Override
        public JsonMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonMessage message = new JsonMessage();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            message.from = getAddresseeFromJsonObject(jsonObject.get(FIELD_NAME_FROM));
            message.to = getAddresseeFromJsonObject(jsonObject.get(FIELD_NAME_TO));

            JsonElement jsonElementData = jsonObject.get(FIELD_NAME_DATA_CLASS);
            if (jsonElementData != null) {
                String className = jsonElementData.getAsString();
                try {
                    message.data = context.deserialize(jsonObject.get(FIELD_NAME_DATA), Class.forName(className));
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException(e);
                }
            }
            return message;
        }

        private Addressee getAddresseeFromJsonObject(JsonElement jsonElement){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String strAddress = jsonObject.get(FIELD_NAME_ADDRESS).getAsString();
            String groupName = jsonObject.get(FIELD_NAME_GROUP_NAME).getAsString();
            return new AddresseeImpl(strAddress, groupName);
        }
    }

    public JsonMessage() {
    }

    public JsonMessage(Addressee from, Addressee to, Object data) {
        super(from, to, data);
    }

    @Override
    public String toPackedData() {
        return gsonBuilder
                .create()
                .toJson(this);
    }

    @Override
    public void loadFromPackagedData(String packagedData) throws MessageFormatException {
        try {
            JsonMessage message = gsonBuilder.create().fromJson(packagedData, JsonMessage.class);
            from = message.from;
            to = message.to;
            data = message.data;
        } catch (JsonSyntaxException e) {
            throw new MessageFormatException("Failed get message from json string:" + packagedData, e);
        }
    }
}
