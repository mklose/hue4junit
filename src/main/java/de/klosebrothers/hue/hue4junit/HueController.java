package de.klosebrothers.hue.hue4junit;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.Integer.getInteger;

public class HueController {

    public static final String RED = "1";
    public static final String GREEN = "23000";
    //TODO client->token
    static final String PROP_NAME_HUE_CLIENT = "hue.client";
    static final String PROP_NAME_HUE_IP = "hue.ip";
    static final String PROP_NAME_HUE_LAMPS = "hue.listener.lamps";
    static final String PROP_NAME_HUE_TIMEOUT = "hue.timeout";
    static final String PROP_FILENAME = "hue4java.properties";
    private static final String DEFAULT_IP = getHueProperty(PROP_NAME_HUE_IP);
    private static final List<String> LAMPS = getLamps();
    static Logger logger = Logger.getLogger(HueController.class.getName());
    private String hueClient;
    private HttpAdapter httpadapter;
    private String hueConnectorIp;
    private boolean disabled = false;

    public HueController() {
        this(new HttpAdapter(getInteger(getHueProperty(PROP_NAME_HUE_TIMEOUT), 5000)));
    }

    public HueController(HttpAdapter httpadapter) {
        this(
                httpadapter,
                getHueProperty(PROP_NAME_HUE_CLIENT)
        );
    }

    public HueController(HttpAdapter httpadapter, String hueClient) {
        this(
                hueClient,
                httpadapter,
                determineConnectorIp(httpadapter)
        );
    }

    public HueController(String client, HttpAdapter httpadapter, String connectorIp) {
        validateClient(client);
        validateConnectorIp(connectorIp);
        logConnectionParameters(client, connectorIp);
        hueClient = client;
        hueConnectorIp = connectorIp;
        this.httpadapter = httpadapter;
    }


    static String getHueProperty(String string) {
        //TODO extract in property provider
        return getHueProperty(string, PROP_FILENAME);
    }

    static String getHueProperty(String filename, String propertyName) {
        //TODO docu
        // that it's looking for hue4java.properties
        return getProperty(HueController.class.getClassLoader().getResourceAsStream(filename), propertyName)
                .orElse(getProperty(Paths.get(filename).toFile(), propertyName)
                        .orElse(System.getProperty(propertyName)));
    }

    static Optional<String> getProperty(File file, String propertyName) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
        return getProperty(inputStream, propertyName);
    }

    static Optional<String> getProperty(InputStream inputStream, String propertyName) {
        if (inputStream == null) return Optional.empty();
        Properties props = new Properties();
        try {
            props.load(inputStream);
            String property = props.getProperty(propertyName);
            if (property != null)
                return Optional.of(property);
        } catch (IOException ignored) {
        }
        return Optional.empty();
    }

    private static List<String> getLamps() {
        String lampsList = getHueProperty(PROP_NAME_HUE_LAMPS);
        if (lampsList == null) {
            return Arrays.asList("1", "2", "3");
        }
        return Arrays.stream(
                lampsList
                        .replace("[", "")
                        .replace("]", "")
                        .split(","))
                .map(String::trim)
                .collect(Collectors.toList());
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
            return ip.trim();
        } catch (Exception any) {
            return null;
        }
    }

    public static String determineConnectorIp(HttpAdapter httpadapter) {
        if (DEFAULT_IP != null) {
            return DEFAULT_IP;
        }

        try {
            URL url = new URL("https://www.meethue.com/api/nupnp");
            String response = httpadapter.sendGetRequest(url);
            return extractIp(response);
        } catch (IOException e) {
            throw new RuntimeException("Cannot determine IP for hue connector", e);
        }
    }

    public boolean isDisabled() {
        return disabled;
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
        disableIfNot(connectorIp);
    }

    private void disableIfNot(String connectorIp) {
        disableWhenNull(connectorIp, "Hue IP address", "hue.ip");
    }

    private void validateClient(String client) {
        disableWhenNull(client, "Hue client", "hue.client");
    }

    private void disableWhenNull(String client, final String name, final String propertyName) {
        if (client == null) {
            String message = String.format(
                    "HueController requires a " + name + ".%n" +
                            "   Consider to use system property '" + propertyName + "'%n");
            logger.warning(message);
            disable("missing " + name);
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
            String response = httpadapter.sendPutRequest(url, changeColourBody);
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

    public List<String> getLampIds() {
        return LAMPS;
    }
}
