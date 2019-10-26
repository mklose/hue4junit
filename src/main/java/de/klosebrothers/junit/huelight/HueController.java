package de.klosebrothers.junit.huelight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HueController {

    public static final String RED = "1";
    public static final String GREEN = "23000";

    private static final String DEFAULT_HUE_CLIENT = System.getProperty("hue.client");
    private static final String DEFAULT_IP = System.getProperty("hue.ip");
    private static final int TIMEOUT = Integer.getInteger("hue.timeout", 5000);
    static Logger logger = Logger.getLogger(HueController.class.getName());
    private final String hueClient;
    private final String hueConnectorIp;
    private boolean disabled = false;
    public HueController() {
        this(DEFAULT_HUE_CLIENT, determineConnectorIp());
    }

    public HueController(String client, String connectorIp) {
        validateClient(client);
        validateConnectorIp(connectorIp);
        logConnectionParameters(client, connectorIp);
        hueClient = client;
        hueConnectorIp = connectorIp;
    }

    public static String determineConnectorIp() {
        if (DEFAULT_IP != null) {
            return DEFAULT_IP;
        }
        try {
            URL url = new URL("https://www.meethue.com/api/nupnp");
            String response = sendHttpRequest(url, "GET", null);
            return extractIp(response);
        } catch (IOException e) {
            throw new RuntimeException("Cannot determine IP for hue connector", e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HueController controller = new HueController();

        controller.switchOn("3");
        Thread.sleep(1000);
        for (int i = 0; i < 3; i++) {
            controller.changeColourTo("3", RED);
            Thread.sleep(1000);
            controller.changeColourTo("3", GREEN);
            Thread.sleep(1000);
        }
        controller.switchOff("3");
    }

    private static String sendHttpRequest(URL url, String method, String body) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setConnectTimeout(TIMEOUT);
        httpCon.setRequestMethod(method);
        if (body != null) {
            httpCon.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            out.write(body);
            out.close();
        }
        return getResponse(httpCon);
    }

    private static String getResponse(HttpURLConnection httpCon) throws IOException {
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
        return responseReader.lines().collect(Collectors.joining(System.getProperty("line.separator")));
    }

    private static String extractIp(String response) {
        if (response == null) {
            return null;
        }
        try {
            //TODO handle multiple hues
            String ip = response.replace("[", "")
                    .replace("]", "")
                    .replace("{", "")
                    .replace("}", "");
            ip = ip.split(",")[1].split(":")[1];
            ip = ip.replace("\"", "");
            return ip;
        } catch (Exception any) {
            return null;
        }
    }

    private void logConnectionParameters(String client, String connectorIp) {
        if (!disabled) {
            String message = String.format(
                    "Connecting to hue with client [%s] on IP address [%s]%n",
                    client,
                    connectorIp
            );
            logger.info(message);
        }
    }

    private void validateConnectorIp(String connectorIp) {
        if (connectorIp == null) {
            String message = String.format(
                    "HueController requires an IP address.%n" +
                            "   Consider to use system property 'hue.ip'%n");
            logger.warning(message);
            disable("missing Hue IP address");
        }
    }

    private void validateClient(String client) {
        if (client == null) {
            String message = String.format(
                    "HueController requires a Hue client.%n   " +
                            "Consider to use system property 'hue.client'%n");
            logger.warning(message);
            disable("missing Hue client");
        }
    }

    public void switchOff(String lampId) {
        String switchOffBody = "{\"on\":false}";
        sendToHue(lampId, switchOffBody);
    }

    public void switchOn(String lampId) {
        String switchOnBody = "{\"on\":true}";
        sendToHue(lampId, switchOnBody);
    }

    public void changeColourTo(String lamp, String colour) {
        String changeColourBody =
                "{"
                        + "\"on\":true, "
                        + "\"sat\":254, "
                        + "\"bri\":120, "
                        + "\"hue\":" + colour + ", "
                        + "\"effect\":\"none\""
                        + "}";
        sendToHue(lamp, changeColourBody);
    }

    private void sendToHue(String lamp, String changeColourBody) {
        if (disabled) {
            return;
        }
        try {
            String urlString = String.format("http://%s/api/%s/lights/%s/state", hueConnectorIp, hueClient, lamp);
            URL url = new URL(urlString);
            String response = sendHttpRequest(url, "PUT", changeColourBody);
            if (response.contains("error")) {
                logger.warning(response);
            }
        } catch (SocketTimeoutException ioe) {
            disable("connection timeout");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void disable(String reason) {
        disabled = true;
        logger.warning(String.format("HueController disabled due to %s%n", reason));
    }

}
