package de.klosebrothers.hue.hue4junit;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class HueController {

    public static final String RED = "1";
    public static final String GREEN = "23000";
    static Logger logger = Logger.getLogger(HueController.class.getName());
    private final String hueClient;
    private final HttpAdapter httpadapter;
    private final String hueConnectorIp;
    private PropertyProvider propertyProvider;
    private boolean disabled = false;

    public HueController() {
        this(new HttpAdapter());
    }

    public HueController(HttpAdapter httpadapter) {
        this(
                httpadapter,
                new PropertyProvider(httpadapter)
        );
    }

    public HueController(HttpAdapter httpadapter, PropertyProvider propertyProvider) {
        this.httpadapter = httpadapter;
        this.propertyProvider = propertyProvider;

        if (!propertyProvider.hasMandatoryProperties()) {
            disable("missing mandatory properties");
        }


        propertyProvider.getHueTimeout()
                .ifPresent(t -> httpadapter.setTimeout(parseInt(t)));

        hueClient = propertyProvider.getHueUsername().orElse("");
        hueConnectorIp = propertyProvider.getHueBridgeIp().orElse("");

        logConnectionParameters();
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

    private List<String> getLamps() {
        if (!propertyProvider.getHueLamps().isPresent()) {
            return Arrays.asList("1", "2", "3");
        }

        return Arrays.stream(
                propertyProvider.getHueLamps().get()
                        .replace("[", "")
                        .replace("]", "")
                        .split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public boolean isDisabled() {
        return disabled;
    }

    private void logConnectionParameters() {
        if (disabled) return;

        String message = String.format(
                "Connecting to hue with client [%s] on IP address [%s]%n",
                hueConnectorIp,
                hueClient
        );
        logger.info(message);
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
        if (disabled) return;

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
        return getLamps();
    }
}
