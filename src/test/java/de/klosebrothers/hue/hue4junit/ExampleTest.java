package de.klosebrothers.hue.hue4junit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class ExampleTest {

    @Test
    @Disabled
    void expecting_red_lights() {
        fail("red light please");
    }

    @Test
    void expecting_green_lights() {
    }

    @Test
    @Disabled("TODO")
    void expecting_yellow_lights() throws Exception {
        Thread.sleep(3000);

    }
}