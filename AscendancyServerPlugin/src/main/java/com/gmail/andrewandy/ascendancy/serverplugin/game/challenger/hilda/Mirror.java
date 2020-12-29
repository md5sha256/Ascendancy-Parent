package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda;

import com.flowpowered.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

import javax.swing.text.View;

public class Mirror {

    private ParticleEffect particleEffect;
    private World world;
    private Vector3d primary, secondary;
    private AABB boundingBox;

    public Mirror(@NotNull ParticleEffect particleEffect, @NotNull World world, @NotNull Vector3d primary, double size) {
        this.particleEffect = particleEffect;
        this.world = world;
        this.primary = primary;
        final double sizeToAdd = Math.sqrt(size * size * 3) / 3D;
        this.secondary = primary.clone().add(sizeToAdd, sizeToAdd, sizeToAdd);
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public @NotNull ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    public void setParticleEffect(@NotNull ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    public @NotNull Vector3d getPrimary() {
        return primary;
    }

    public void setPrimary(@NotNull Vector3d primary) {
        if (!this.primary.equals(primary)) {
            this.boundingBox = new AABB(primary, this.secondary);
        }
        this.primary = primary;
    }

    public @NotNull Vector3d getSecondary() {
        return secondary;
    }

    public void setSecondary(@NotNull Vector3d secondary) {
        if (!this.secondary.equals(secondary)) {
            this.boundingBox = new AABB(this.primary, secondary);
        }
        this.secondary = secondary;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public void setWorld(@NotNull World world) {
        this.world = world;
    }

    public void render() {
        render(world);
    }

    public void render(final Viewer viewer) {
        double x1 = Math.min(primary.getX(), secondary.getX());
        double x2 = x1 == primary.getX() ? secondary.getX() : primary.getX();
        double y1 = Math.min(primary.getY(), secondary.getY());
        double y2 = y1 == primary.getY() ? secondary.getY() : primary.getY();
        double z1 = Math.min(primary.getZ(), secondary.getZ());
        double z2 = z1 == primary.getZ() ? secondary.getZ() : primary.getZ();

        final Vector3d bottomLeftCorner = new Vector3d(x1, y1, z1);
        final Vector3d bottomRightCorner = new Vector3d(x2, y1, z2);
        final Vector3d topLeftCorner = new Vector3d(x1, y2, z1);
        final Vector3d topRightCorner = new Vector3d(x2, y1, z2);

        drawEdge(viewer, bottomLeftCorner, bottomRightCorner);
        drawEdge(viewer, topLeftCorner, topRightCorner);
        drawEdge(viewer, bottomLeftCorner, topLeftCorner);
        drawEdge(viewer, bottomRightCorner, topRightCorner);
    }

    private void drawEdge(final Viewer viewer, final Vector3d primary, final Vector3d secondary) {
        final Vector3d direction = secondary.sub(primary);
        for (double t = primary.getX(); t < secondary.getX(); t++) {
            Vector3d position = primary.add(direction.mul(t));
            viewer.spawnParticles(particleEffect, position);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mirror mirror = (Mirror) o;

        if (!particleEffect.equals(mirror.particleEffect)) return false;
        if (!world.equals(mirror.world)) return false;
        if (!primary.equals(mirror.primary)) return false;
        return secondary.equals(mirror.secondary);
    }


    @Override
    public int hashCode() {
        int result = particleEffect.hashCode();
        result = 31 * result + world.hashCode();
        result = 31 * result + primary.hashCode();
        result = 31 * result + secondary.hashCode();
        return result;
    }

}
