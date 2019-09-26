package chat.wewe.android.service;

import chat.wewe.android.api.DDPClientWrapper;

/**
 * reference to get fresh DDPClient instance.
 */
public interface DDPClientRef {
  DDPClientWrapper get();
}
