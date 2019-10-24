package org.coins1920.group05;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        final boolean b = true;
        assertThat(b, is(true));
        assertThat(b, is(not(nullValue())));
    }

}
