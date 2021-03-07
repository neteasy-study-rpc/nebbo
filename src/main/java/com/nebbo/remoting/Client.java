package com.nebbo.remoting;

import java.net.URI;

public interface Client {
    void connect(URI uri, Codec codec, Handler handler);
    NebboChannel getChannel();
}
