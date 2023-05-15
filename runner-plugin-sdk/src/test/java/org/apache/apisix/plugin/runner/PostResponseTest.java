package org.apache.apisix.plugin.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PostResponseTest {

    @Test
    @DisplayName("test encode with set charset")
    void testEncodeWithSetCharset() {
        long requestId = 1L;
        String body = "dummy body";
        Charset charset = StandardCharsets.UTF_16;

        PostResponse postResponse = new PostResponse(requestId);
        postResponse.setBody(body);
        postResponse.setCharset(charset);

        ByteBuffer encoded = postResponse.encode();

        assertTrue(Collections.indexOfSubList(byteArrayToList(encoded.array()), byteArrayToList(body.getBytes(charset))) >= 0);
    }

    @Test
    @DisplayName("test encode without set charset")
    void testEncodeWithoutSetCharset() {
        long requestId = 1L;
        String body = "dummy body";
        Charset charset = StandardCharsets.UTF_8;

        PostResponse postResponse = new PostResponse(requestId);
        postResponse.setBody(body);

        ByteBuffer encoded = postResponse.encode();

        assertTrue(Collections.indexOfSubList(byteArrayToList(encoded.array()), byteArrayToList(body.getBytes(charset))) >= 0);
    }

    private List<Byte> byteArrayToList(byte[] array) {
        List<Byte> list = new ArrayList<>();
        for (byte b : array) {
            list.add(b);
        }
        return list;
    }
}
