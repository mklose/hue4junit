package de.klosebrothers.hue.hue4junit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.util.stream.Collectors.joining;

public class HttpAdapter {
    private int timeout = 5000;

    String sendGetRequest(URL url) throws IOException {
        return sendHttpRequest(url, "GET", null);
    }

    String sendPutRequest(URL url, String body) throws IOException {
        return sendHttpRequest(url, "PUT", body);
    }

    private String sendHttpRequest(URL url, String method, String body) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setConnectTimeout(timeout);
        httpCon.setRequestMethod(method);
        if (body != null) {
            httpCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            out.write(body);
            out.close();
        }
        return getResponse(httpCon);
    }

    private String getResponse(HttpURLConnection httpCon) throws IOException {
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
        return responseReader.lines()
                .collect(joining(System.lineSeparator()));
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
