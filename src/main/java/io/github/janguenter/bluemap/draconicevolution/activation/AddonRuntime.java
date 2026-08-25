/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.draconicevolution.activation;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared fail-closed exact-profile state with bounded diagnostics. */
public final class AddonRuntime {

    public static final AddonRuntime INSTANCE = new AddonRuntime();
    private static final int MAX_DIAGNOSTICS = 8;

    private final AtomicInteger diagnostics = new AtomicInteger();
    private volatile State state = State.INACTIVE;
    private volatile String detail = "not-installed";

    private AddonRuntime() {
    }

    public boolean active() {
        return state == State.ACTIVE;
    }

    public State state() {
        return state;
    }

    public String detail() {
        return detail;
    }

    public synchronized void activate() {
        if (state != State.FAILED) {
            state = State.ACTIVE;
            detail = "exact-profile";
        }
    }

    public synchronized void inactive(String reason) {
        if (state != State.FAILED) {
            state = State.INACTIVE;
            detail = normalize(reason);
            report("inactive-" + detail);
        }
    }

    public synchronized void fail(String reason) {
        state = State.FAILED;
        detail = normalize(reason);
        report("failed-" + detail);
    }

    private void report(String reason) {
        if (diagnostics.incrementAndGet() <= MAX_DIAGNOSTICS) {
            System.err.println("BlueMap Draconic Evolution add-on: " + reason + '.');
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("activation detail is null");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
        if (!normalized.matches("[a-z0-9][a-z0-9._:-]*")) {
            throw new IllegalArgumentException("activation detail is not a wire value");
        }
        return normalized;
    }

    /** Exact activation state; FAILED is terminal for one classloader. */
    public enum State {
        INACTIVE,
        ACTIVE,
        FAILED
    }
}
