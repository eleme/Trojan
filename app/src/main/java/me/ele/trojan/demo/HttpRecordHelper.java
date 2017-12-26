package me.ele.trojan.demo;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import me.ele.trojan.Trojan;
import me.ele.trojan.log.Logger;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by panda on 2017/12/12.
 */

public class HttpRecordHelper {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void recordRequest(Request request) {
        String method = request.method();
        String url = request.url().toString();
        Logger.i("recordRequest-->method:" + method + ",url:" + url);
        List<String> msgList = new LinkedList<>();
        msgList.add(method);
        msgList.add(url);
        Trojan.log("TrojanRequest", msgList);
    }

    public static Response recordResponse(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();

        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }

        List<String> msgList = new LinkedList<>();
        msgList.add(response.request().method());
        msgList.add(response.request().url().toString());

        if (isPlaintext(buffer) && contentLength != 0) {
            msgList.add(buffer.clone().readString(charset));
        } else {
            msgList.add("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
        }
        Trojan.log("TrojanResponse", msgList);

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

}
