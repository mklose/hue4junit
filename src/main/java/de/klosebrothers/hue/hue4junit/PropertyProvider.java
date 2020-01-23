package de.klosebrothers.hue.hue4junit;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertyProvider {
    //TODO client->username
    private static final String PROP_NAME_HUE_CLIENT = "hue.client";
    private static final String PROP_NAME_HUE_IP = "hue.ip";
    private static final String PROP_NAME_HUE_LAMPS = "hue.listener.lamps";
    private static final String PROP_NAME_HUE_TIMEOUT = "hue.timeout";
    private static Logger logger = Logger.getLogger(PropertyProvider.class.getName());
    private String propertyFilename = "hue4junit.properties";
    private HttpAdapter httpadapter;
    private String hueBridgeIp = null;

    public PropertyProvider(HttpAdapter httpadapter) {
        this.httpadapter = httpadapter;
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

    public String determineBridgeIp() {
        try {
            URL url = new URL("https://www.meethue.com/api/nupnp");
            String response = httpadapter.sendGetRequest(url);
            return extractIp(response);
        } catch (IOException e) {
            throw new RuntimeException("Cannot determine IP for hue connector", e);
        }
    }

    Optional<String> getHueProperty(String propertyName) {
        return getHueProperty(getPropertyFilename(), propertyName);
    }

    Optional<String> getHueProperty(String filename, String propertyName) {
        //TODO docu
        // that it's looking for hue4junit.properties
        return Optional.ofNullable(getProperty(HueController.class.getClassLoader().getResourceAsStream(filename), propertyName)
                .orElse(getProperty(Paths.get(filename).toFile(), propertyName)
                        .orElse(System.getProperty(propertyName))));
    }

    Optional<String> getProperty(File file, String propertyName) {
        FileInputStream inputStream;
        if (!file.exists())
            return Optional.empty();
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
        return getProperty(inputStream, propertyName);
    }

    Optional<String> getProperty(InputStream inputStream, String propertyName) {
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

    public Optional<String> getHueBridgeIp() {
        Optional<String> property = Optional.empty();
        if (hueBridgeIp == null) {
            property = Optional.ofNullable(getHueProperty(PROP_NAME_HUE_IP)
                    .orElse(determineBridgeIp()));
        }
        if (!property.isPresent()) {
            missingMandatoryProperty("Hue IP address", PROP_NAME_HUE_IP);
        }
        return property;
    }

    public Optional<String> getHueTimeout() {
        return getHueProperty(PROP_NAME_HUE_TIMEOUT);
    }

    public Optional<String> getHueLamps() {
        return getHueProperty(PROP_NAME_HUE_LAMPS);
    }

    public Optional<String> getHueClient() {
        Optional<String> property = getHueProperty(PROP_NAME_HUE_CLIENT);
        if (!property.isPresent()) {
            missingMandatoryProperty("Hue client", PROP_NAME_HUE_CLIENT);
        }
        return property;
    }

    private void missingMandatoryProperty(final String name, final String propertyName) {
        String message = String.format(
                HueController.class.getName() + " requires a " + name + ".%n" +
                        "   Consider to use system property '" + propertyName + "'%n");
        logger.warning(message);
    }

    public String getPropertyFilename() {
        return propertyFilename;
    }

    public void setPropertyFilename(String propertyFilename) {
        this.propertyFilename = propertyFilename;
    }

    public boolean hasMandatoryProperties() {
        return getHueClient().isPresent() && getHueBridgeIp().isPresent();
    }
}
