package de.klosebrothers.hue.hue4junit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HueControllerTest {

    @Mock
    HttpAdapter httpAdapter;

    @Mock
    PropertyProvider propertyProvider;

    @Test
    void it_should_call_hue_on_lamps_on() throws IOException {
        when(propertyProvider.getHueBridgeIp()).thenReturn(Optional.of("192.168.2.2"));
        when(propertyProvider.getHueUsername()).thenReturn(Optional.of("someUsername"));
        when(propertyProvider.hasMandatoryProperties()).thenReturn(true);

        when(httpAdapter
                .sendPutRequest(any(), any()))
                .thenReturn("");

        HueController hueController = new HueController(httpAdapter, propertyProvider);
        assertThat(hueController.isDisabled()).isFalse();
        hueController.switchOn("1");

        verify(httpAdapter)
                .sendPutRequest(
                        eq(new URL("http://192.168.2.2/api/someUsername/lights/1/state")),
                        eq("{\"on\":true}"));

    }

    //TODO test for :
    //de.klosebrothers.hue.hue4junit.HueController sendToHue
    //WARNING: [{"error":{"type":1,"address":"/lights","description":"unauthorized user"}}]

    //TODO test for getLamps from : [1, 2, 4]
}