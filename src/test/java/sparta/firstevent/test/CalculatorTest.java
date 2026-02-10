package sparta.firstevent.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class CalculatorTest {

    @Test
    void calculator() {
        // given
        Calculator calculator = spy(new Calculator());

        given(calculator.add(1, 2)).willReturn(10);

        // when
        int result = calculator.add(1, 2);

        // then
        assertEquals(10, result);

    }

    @Test
    void calculatorMock() {

        Calculator calculator = mock(Calculator.class);

        given(calculator.add(1, 2)).willReturn(10);

        // when
        int result = calculator.add(1, 2);

        int result2 = calculator.minus(1, 2);

        System.out.println(result2);

        // then
        assertEquals(10, result);

    }

    @Test
    void calculatorSpy() {
        // given
        Calculator calculator = spy(new Calculator());

        willReturn(10).given(calculator).add(1, 2);


        // when
        int result = calculator.add(1, 2);

        int result2 = calculator.minus(1, 2);

        System.out.println(result2);

        // then
        assertEquals(10, result);

    }

}