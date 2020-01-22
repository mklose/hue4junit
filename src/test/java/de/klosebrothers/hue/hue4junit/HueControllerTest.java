package de.klosebrothers.hue.hue4junit;

import org.assertj.core.api.AbstractStringAssert;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import java.io.IOException;
import java.net.URL;

import static de.klosebrothers.hue.hue4junit.HueController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HueControllerTest {

    @Test
    void it_should_read_parameters_from_resource_file() {
        readFromPropFile(PROP_NAME_HUE_IP)
                .isEqualTo("192.168.178.49");
        readFromPropFile(PROP_NAME_HUE_CLIENT)
                .isEqualTo("3O2Ce9zBt9ed7Ht9kdvAlYVbLr7ycmVtTOc6FICU");
        readFromPropFile(PROP_NAME_HUE_LAMPS)
                .isEqualTo("[\"1\", \"2\", \"3\"]");
        readFromPropFile(PROP_NAME_HUE_TIMEOUT)
                .isEqualTo("2500");
    }

    @Test
    @SetSystemProperty(key = PROP_NAME_HUE_IP, value = "a")
    @SetSystemProperty(key = PROP_NAME_HUE_CLIENT, value = "b")
    @SetSystemProperty(key = PROP_NAME_HUE_LAMPS, value = "c")
    @SetSystemProperty(key = PROP_NAME_HUE_TIMEOUT, value = "d")
    void it_should_read_parameters_from_system_properties_when_propfile_is_missing() {
        missingPropFile(PROP_NAME_HUE_IP)
                .isEqualTo("a");
        missingPropFile(PROP_NAME_HUE_CLIENT)
                .isEqualTo("b");
        missingPropFile(PROP_NAME_HUE_LAMPS)
                .isEqualTo("c");
        missingPropFile(PROP_NAME_HUE_TIMEOUT)
                .isEqualTo("d");
    }

    @Test
    @ClearSystemProperty(key = PROP_NAME_HUE_IP)
    @ClearSystemProperty(key = PROP_NAME_HUE_CLIENT)
    @ClearSystemProperty(key = PROP_NAME_HUE_LAMPS)
    @ClearSystemProperty(key = PROP_NAME_HUE_TIMEOUT)
    void it_should_return_null_when_propfile_is_missing_and_system_properties_are_not_set() {
        missingPropFile(PROP_NAME_HUE_IP)
                .isNull();
        missingPropFile(PROP_NAME_HUE_CLIENT)
                .isNull();
        missingPropFile(PROP_NAME_HUE_LAMPS)
                .isNull();
        missingPropFile(PROP_NAME_HUE_TIMEOUT)
                .isNull();
    }

    @Test
    void property_file_is() {
        assertThat(PROP_FILENAME).isEqualTo("hue4java.properties");
    }

    private AbstractStringAssert<?> readFromPropFile(String propertyName) {
        return assertThat(getHueProperty(PROP_FILENAME, propertyName));
    }

    private AbstractStringAssert<?> missingPropFile(String propertyName) {
        return assertThat(getHueProperty("MISSING_PROP_FILE", propertyName));
    }

    @Test
    void it_should_parse_ip_from_hue_website() throws IOException {
        HttpAdapter httpAdapter = mock(HttpAdapter.class);
        @Language("JSON") String jsonResponse = "[\n" +
                "  {\n" +
                "    \"id\": \"001788fffe618c1b\",\n" +
                "    \"internalipaddress\": \"192.168.2.2\"\n" +
                "  }\n" +
                "]";

        when(httpAdapter
                .sendGetRequest(eq(new URL("https://www.meethue.com/api/nupnp"))))
                .thenReturn(jsonResponse);

        assertThat(determineConnectorIp(httpAdapter))
                .isEqualTo("192.168.2.2");
    }

    @Test
    void it_should_call_hue_on_lamps_on() throws IOException {
        HttpAdapter httpAdapter = mock(HttpAdapter.class);
        @Language("JSON") String jsonResponse = "[\n" +
                "  {\n" +
                "    \"id\": \"001788fffe618c1b\",\n" +
                "    \"internalipaddress\": \"192.168.2.2\"\n" +
                "  }\n" +
                "]";

        when(httpAdapter
                .sendGetRequest(any()))
                .thenReturn(jsonResponse);
        when(httpAdapter
                .sendPutRequest(any(), any()))
                .thenReturn("");

        HueController hueController = new HueController(httpAdapter, "s");
        assertThat(hueController.isDisabled()).isFalse();
        hueController.switchOn("1");

        verify(httpAdapter)
                .sendPutRequest(
                        eq(new URL("http://192.168.2.2/api/s/lights/1/state")),
                        eq("{\"on\":true}"));

    }

    //TODO ci, sonar


}