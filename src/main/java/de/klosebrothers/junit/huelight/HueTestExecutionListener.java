package de.klosebrothers.junit.huelight;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.klosebrothers.junit.huelight.HueController.GREEN;
import static de.klosebrothers.junit.huelight.HueController.RED;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;


public class HueTestExecutionListener implements TestExecutionListener {

  private static final List<String> LAMPS = getLamps();
  private volatile boolean executionFailed;
  private HueController hueController;

  private static List<String> getLamps() {
    String lampsList = System.getProperty("hue.listener.lamps");
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

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    initializeTestRun();
    green();
    blink();
  }

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    // delay500ms();
  }

  @Override
  public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    refreshExecutionState(testExecutionResult);
  }

  private void refreshExecutionState(TestExecutionResult executionResult) {
    boolean newExecutionFailed = executionFailed || executionResult.getStatus() == TestExecutionResult.Status.FAILED;
    if (newExecutionFailed == executionFailed) {
      return;
    } else {
      executionFailed = newExecutionFailed;
    }
    displayExecutionState();
  }

  private void displayExecutionState() {
    if (executionFailed) {
      red();
    } else {
      green();
    }
  }

  private void off() {
    if (hueController != null) {
      for (String lamp : LAMPS) {
        hueController.switchOff(lamp);
      }
    }
  }

  private void on() {
    if (hueController != null) {
      for (String lamp : LAMPS) {
        hueController.switchOn(lamp);
      }
    }
  }

  private void green() {
    if (hueController != null) {
      for (String lamp : LAMPS) {
        hueController.changeColourTo(lamp, GREEN);
      }
    }
  }

  private void red() {
    if (hueController != null) {
      for (String lamp : LAMPS) {
        hueController.changeColourTo(lamp, RED);
      }
    }
  }

  private void blink() {
    off();
    delay500ms();
    on();
  }

  private void initializeTestRun() {
    executionFailed = false;
    hueController = new HueController();
  }

  private void delay500ms() {
    try {
      Thread.sleep(500);
    } catch (InterruptedException ignore) {
    }
  }

}
