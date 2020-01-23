package de.klosebrothers.hue.hue4junit;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyProviderTest {
    @Mock
    HttpAdapter httpAdapter;

    @InjectMocks
    private PropertyProvider propertyProvider;

    @Test
    void property_file_is() {
        assertThat(propertyProvider.getPropertyFilename())
                .isEqualTo("hue4junit.properties");
    }

    @Test
    void it_should_read_parameters_from_resource_file() {
        assertThat(propertyProvider.getHueBridgeIp())
                .hasValue("192.168.178.49");
        assertThat(propertyProvider.getHueClient())
                .hasValue("8GFTbkftIvDT0SWSyV4LliHJICB870JVRr5TkTk1");
        assertThat(propertyProvider.getHueLamps())
                .hasValue("[1, 2, 3]");
        assertThat(propertyProvider.getHueTimeout())
                .hasValue("2500");
    }

    @Test
    @SetSystemProperty(key = "hue.ip", value = "a")
    @SetSystemProperty(key = "hue.client", value = "b")
    @SetSystemProperty(key = "hue.listener.lamps", value = "c")
    @SetSystemProperty(key = "hue.timeout", value = "d")
    void it_should_read_parameters_from_system_properties_when_propertyfile_is_missing() {
        propertyProvider.setPropertyFilename("MISSING_PROP_FILE");

        assertThat(propertyProvider.getHueBridgeIp())
                .hasValue("a");
        assertThat(propertyProvider.getHueClient())
                .hasValue("b");
        assertThat(propertyProvider.getHueLamps())
                .hasValue("c");
        assertThat(propertyProvider.getHueTimeout())
                .hasValue("d");
    }

    @Test
    @ClearSystemProperty(key = "hue.ip")
    @ClearSystemProperty(key = "hue.client")
    @ClearSystemProperty(key = "hue.listener.lamps")
    @ClearSystemProperty(key = "hue.timeout")
    void it_should_return_null_when_propfile_is_missing_and_system_properties_are_not_set() {
        propertyProvider.setPropertyFilename("MISSING_PROP_FILE");

        assertThat(propertyProvider.getHueBridgeIp())
                .isEmpty();
        assertThat(propertyProvider.getHueClient())
                .isEmpty();
        assertThat(propertyProvider.getHueLamps())
                .isEmpty();
        assertThat(propertyProvider.getHueTimeout())
                .isEmpty();
    }

    @Test
    @ClearSystemProperty(key = "hue.ip")
    @ClearSystemProperty(key = "hue.client")
    void does_not_have_mandatory_properties() {
        propertyProvider.setPropertyFilename("MISSING_PROP_FILE");

        assertThat(propertyProvider.hasMandatoryProperties())
                .isFalse();
    }

    @Test
    void has_mandatory_properties() {
        assertThat(propertyProvider.hasMandatoryProperties())
                .isTrue();
    }

    @Test
    @ClearSystemProperty(key = "hue.ip")
    void it_should_parse_ip_from_hue_website_when_it_can_not_get_it_via_properties() throws IOException {
        propertyProvider.setPropertyFilename("MISSING_PROP_FILE");

        @Language("JSON") String jsonResponse = "[\n" +
                "  {\n" +
                "    \"id\": \"001788fffe618c1b\",\n" +
                "    \"internalipaddress\": \"192.168.2.2\"\n" +
                "  }\n" +
                "]";

        when(httpAdapter
                .sendGetRequest(eq(new URL("https://www.meethue.com/api/nupnp"))))
                .thenReturn(jsonResponse);

        assertThat(propertyProvider.getHueBridgeIp())
                .hasValue("192.168.2.2");
    }
}