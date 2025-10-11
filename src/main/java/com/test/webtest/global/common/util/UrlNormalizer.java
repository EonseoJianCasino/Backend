package com.test.webtest.global.common.util;

import java.net.URI;

public final class UrlNormalizer {
    private UrlNormalizer(){}

    public static String extractDomain(String raw) {
        try {
            String t = raw.trim();
            if(!t.startsWith("http")) t = "https://" + t;
            URI u = URI.create(t);
            String host = u.getHost();
            String h  = host.toLowerCase();
            return h.startsWith("www.") ? h.substring(4) : h;
        } catch(Exception e) {
            return "";
        }
    }
}
