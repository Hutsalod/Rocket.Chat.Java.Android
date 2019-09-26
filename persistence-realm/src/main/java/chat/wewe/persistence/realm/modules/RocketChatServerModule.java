package chat.wewe.persistence.realm.modules;

import io.realm.annotations.RealmModule;

import chat.wewe.persistence.realm.models.RealmBasedServerInfo;

@RealmModule(library = true, classes = {RealmBasedServerInfo.class})
public class RocketChatServerModule {
}
