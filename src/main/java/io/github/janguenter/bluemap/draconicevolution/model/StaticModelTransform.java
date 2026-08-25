/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel.Transform;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Vertex;

/** Applies deterministic neutral transforms to installed model triangles. */
public final class StaticModelTransform {

    private StaticModelTransform() {
    }

    public static Triangle triangle(
            Triangle input,
            Transform transform,
            boolean rotateWithFacing,
            String facing
    ) {
        int turns = rotateWithFacing ? turns(facing) : 0;
        return new Triangle(
                vertex(input.first(), transform, turns),
                vertex(input.second(), transform, turns),
                vertex(input.third(), transform, turns),
                input.group()
        );
    }

    private static Vertex vertex(Vertex input, Transform transform, int turns) {
        float x = input.x() * transform.scaleX() + transform.translateX();
        float y = input.y() * transform.scaleY() + transform.translateY();
        float z = input.z() * transform.scaleZ() + transform.translateZ();
        float rotatedX;
        float rotatedZ;
        switch (turns) {
            case 0 -> {
                rotatedX = x;
                rotatedZ = z;
            }
            case 1 -> {
                rotatedX = 1F - z;
                rotatedZ = x;
            }
            case 2 -> {
                rotatedX = 1F - x;
                rotatedZ = 1F - z;
            }
            case 3 -> {
                rotatedX = z;
                rotatedZ = 1F - x;
            }
            default -> throw new IllegalArgumentException("invalid quarter turn");
        }
        return new Vertex(rotatedX, y, rotatedZ, input.u(), input.v());
    }

    private static int turns(String facing) {
        return switch (facing) {
            case "south" -> 0;
            case "east" -> 1;
            case "north" -> 2;
            case "west" -> 3;
            default -> 2;
        };
    }
}
