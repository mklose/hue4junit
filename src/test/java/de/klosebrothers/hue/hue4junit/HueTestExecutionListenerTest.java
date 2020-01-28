package de.klosebrothers.hue.hue4junit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HueTestExecutionListenerTest {

    @Mock
    HueController hueController;

    @InjectMocks
    HueTestExecutionListener hueTestExecutionListener;

    String lamp = "1";

    @BeforeEach
    void init() {
        when(hueController.getLampIds())
                .thenReturn(singletonList(lamp));

        hueTestExecutionListener.testPlanExecutionStarted(null);
    }

    @Test
    void it_should_set_red_on_failed() {
        hueTestExecutionListener.executionFinished(null, TestExecutionResult.failed(null));

        verify(hueController)
                .changeColourTo(lamp, HueController.RED);
    }

    @Test
    void it_should_set_green_on_success() {
        hueTestExecutionListener.executionFinished(null, TestExecutionResult.successful());

        verify(hueController)
                .changeColourTo(lamp, HueController.GREEN);
    }
}