package com.nebbo.registry;

import com.nebbo.common.tools.URIUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class URITest {
    public static void main(String[] args) {
        try {
            URI uri =  new URI("nebbo://127.0.0.1:10088/com.study.dubbo.sms.api.SmsService?transporter=Netty4Transporter&serialization=JsonSerialization");
            String uriPath = uri.getScheme();
            System.out.println(uriPath);
            String service = URIUtils.getService(uri);
            System.out.println(service);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
