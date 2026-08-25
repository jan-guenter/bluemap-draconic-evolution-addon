/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.draconicevolution.profile;

import java.util.Objects;
import java.util.regex.Pattern;

/** Complete installed-byte identity required by one exact profile. */
public record ArtifactPin(
        String key,
        String modId,
        String version,
        String fileName,
        long size,
        String sha256
) {

    private static final Pattern KEY = Pattern.compile("[a-z][A-Za-z0-9]*");
    private static final Pattern MOD_ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern FILE_NAME =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._+-]*\\.jar");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    public ArtifactPin {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(sha256, "sha256");
        if (!KEY.matcher(key).matches()
                || !MOD_ID.matcher(modId).matches()
                || version.isBlank()
                || !FILE_NAME.matcher(fileName).matches()
                || size <= 0
                || !SHA256.matcher(sha256).matches()) {
            throw new IllegalArgumentException("invalid exact artifact pin");
        }
    }
}
