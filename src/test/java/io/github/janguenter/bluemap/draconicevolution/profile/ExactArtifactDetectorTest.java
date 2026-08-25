/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.draconicevolution.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExactArtifactDetectorTest {

    @TempDir
    Path temporary;

    @Test
    void acceptsOneExactDeclaringJar() throws IOException {
        Path jar = createJar("candidate.jar", "targetmod", "payload");
        ArtifactPin pin = pin(jar, "targetmod");

        assertTrue(ExactArtifactDetector.matchesAll(List.of(jar), List.of(pin)));
    }

    @Test
    void rejectsDuplicatesWrongBytesAndWrongDeclarations() throws IOException {
        Path first = createJar("first.jar", "targetmod", "payload");
        Path duplicate = temporary.resolve("duplicate.jar");
        Files.copy(first, duplicate);
        ArtifactPin pin = pin(first, "targetmod");
        Path wrongMod = createJar("wrong-mod.jar", "anothermod", "payload");
        Path wrongBytes = createJar("wrong-bytes.jar", "targetmod", "changed");

        assertFalse(ExactArtifactDetector.matchesAll(
                List.of(first, duplicate),
                List.of(pin)
        ));
        assertFalse(ExactArtifactDetector.matchesAll(List.of(wrongMod), List.of(pin)));
        assertFalse(ExactArtifactDetector.matchesAll(List.of(wrongBytes), List.of(pin)));
    }

    private Path createJar(String name, String modId, String payload) throws IOException {
        Path jar = temporary.resolve(name);
        try (OutputStream output = Files.newOutputStream(jar);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry("META-INF/neoforge.mods.toml"));
            zip.write((
                    "modLoader=\"javafml\"\n"
                            + "# comments and quoted # values must not confuse admission\n"
                            + "[[mods]]\n"
                            + "modId=\"" + modId + "\"\n"
                            + "displayName=\"Example # literal\"\n"
            ).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("payload.txt"));
            zip.write(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return jar;
    }

    private static ArtifactPin pin(Path jar, String modId) throws IOException {
        return new ArtifactPin(
                "target",
                modId,
                "1.0.0",
                jar.getFileName().toString(),
                Files.size(jar),
                digest(jar)
        );
    }

    private static String digest(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
