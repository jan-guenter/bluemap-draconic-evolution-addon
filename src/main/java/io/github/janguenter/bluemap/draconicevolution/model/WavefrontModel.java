/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import java.util.List;

/** Immutable textured triangles read from an operator-installed OBJ resource. */
public record WavefrontModel(List<Triangle> triangles) {

    public WavefrontModel {
        triangles = List.copyOf(triangles);
    }

    /** One triangle and the OBJ object or group that owns it. */
    public record Triangle(Vertex first, Vertex second, Vertex third, String group) {
    }

    /** Model-space position and normalized texture coordinates. */
    public record Vertex(float x, float y, float z, float u, float v) {
    }
}
