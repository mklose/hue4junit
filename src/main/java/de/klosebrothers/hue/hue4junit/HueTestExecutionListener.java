package de.klosebrothers.hue.hue4junit;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class HueTestExecutionListener implements TestExecutionListener {

    private volatile boolean executionFailed;
    private HueController hueController;

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
        if (hueController == null) return;
        hueController.getLampIds()
                .forEach(lamp -> hueController.switchOff(lamp));
    }

    private void on() {
        if (hueController == null) return;
        hueController.getLampIds()
                .forEach(lamp -> hueController.switchOn(lamp));
    }

    private void changeColourTo(String colour) {
        if (hueController == null) return;
        hueController.getLampIds()
                .forEach(lamp -> hueController.changeColourTo(lamp, colour));
    }

    private void green() {
        changeColourTo(HueController.GREEN);
    }

    private void red() {
        changeColourTo(HueController.RED);
    }

    private void blink() {
        off();
        delay500ms();
        on();
    }

    private void initializeTestRun() {
        executionFailed = false;
        if (hueController != null) return;
        hueController = new HueController();
    }

    private void delay500ms() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

}
