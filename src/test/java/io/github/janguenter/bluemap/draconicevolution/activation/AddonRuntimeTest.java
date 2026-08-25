/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.draconicevolution.activation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

class AddonRuntimeTest {

    @Test
    void failureIsTerminalAndReasonsAreWireValues() throws ReflectiveOperationException {
        Constructor<AddonRuntime> constructor = AddonRuntime.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AddonRuntime runtime = constructor.newInstance();
        runtime.activate();
        runtime.fail("registry collision");
        runtime.activate();
        runtime.inactive("later");

        assertFalse(runtime.active());
        assertEquals(AddonRuntime.State.FAILED, runtime.state());
        assertEquals("registry-collision", runtime.detail());
        assertThrows(IllegalArgumentException.class, () -> runtime.fail("bad value!"));
    }
}
