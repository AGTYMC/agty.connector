package org.agty.connector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ConnectorPostValue {
    private Map<String, String> post = new HashMap<>();
    private String ENCODING = "UTF-8";

    public ConnectorPostValue(Map<String, String> post) {
        this.post = post;
    }

    public ConnectorPostValue(Map<String, String> post, String ENCODING) {
        this.post = post;
        this.ENCODING = ENCODING;
    }

    public String getPostForm()  {
        StringJoiner post = new StringJoiner("&");

        for(Map.Entry<String, String> entry: this.post.entrySet()) {
            try {
                post.add(
                        URLEncoder.encode(entry.getKey(), ENCODING)
                                + "="
                                + URLEncoder.encode(entry.getValue(), ENCODING)
                );
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return post.toString();
    }

    public String getPostJson()  {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this.post);
    }
}