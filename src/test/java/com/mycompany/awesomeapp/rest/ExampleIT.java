package com.mycompany.awesomeapp.rest;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

/**
 * example integration test
 */
public class ExampleIT {

    /**
     * JUnit & Hamcrest
     */
    @Test
    public void exampleIT() {
        Assert.assertThat("check if true is true (JUnit/Hamcrest Example IT)", /* is */ true, /* expected is */ Is.is(true));
    }
}
