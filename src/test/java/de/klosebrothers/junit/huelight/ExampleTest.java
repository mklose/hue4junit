package de.klosebrothers.junit.huelight;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
  void expecting_yellow_lights() throws Exception {
    Thread.sleep(3000);
  }

}