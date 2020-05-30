package com.gmail.andrewandy.ascendency.serverplugin.util;

import am2.api.extensions.IEntityExtension;
import am2.extensions.EntityExtension;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import net.minecraft.entity.EntityLivingBase;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Common {

    private static String prefix = "";
    private static ExecutorService executorService;

    public static void setup() {
        executorService =
            Sponge.getScheduler().createSyncExecutor(AscendencyServerPlugin.getInstance());
    }

    public static void setPrefix(final String prefix) {
        Common.prefix = prefix;
    }

    public static ExecutorService getSyncExecutor() {
        return executorService;
    }

    public static void tell(final MessageReceiver receiver, final String... messages) {
        Objects.requireNonNull(receiver).sendMessage(Text.of((Object[]) messages));
    }

    public static void log(final Level level, final String... messages) {
        final Logger logger = AscendencyServerPlugin.getInstance().getLogger();
        for (String message : messages) {
            message = colorise(message.concat(prefix + " " + message));
            if (level == Level.INFO) {
                logger.info(message);
            } else if (level == Level.WARNING) {
                logger.warn(message);
            } else if (level == Level.SEVERE) {
                logger.error(message);
            } else {
                logger.debug(message);
            }
        }
    }

    public static long toTicks(final long time, final TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(time, timeUnit) * 5; //one tick = 5ms
    }

    public static long fromTicks(final long ticks, final TimeUnit to) {
        return to.convert(ticks / 20, TimeUnit.SECONDS);
    }

    public static void addHealth(final Player player, final double health) {
        addHealth(player, health, false);
    }

    public static void addHealth(final Player player, final double health, final boolean overheal) {
        final HealthData data = player.getHealthData();
        data.set(data.health().transform((val) -> {
            double ret = health + val;
            if (val + health > data.maxHealth().get()) {
                ret = overheal ? ret : data.maxHealth().get();
            }
            return ret;
        }));
        player.offer(data);
    }

    public static String colorise(final String string) {
        return colorise(Text.of(string));
    }

    public static String colorise(final Text text) {
        return TextSerializers.formattingCode('&').serialize(text);
    }

    public static String stripColor(final String str) {
        return TextSerializers.formattingCode('&').stripCodes(str);
    }

    public static String stripColor(final Text text) {
        return stripColor(Objects.requireNonNull(text).toString());
    }

    public static IEntityExtension getExtensionFor(final Player player) {
        return EntityExtension.For((EntityLivingBase) player);
    }

    public static int getAbilityPower(final Player player) {
        return getExtensionFor(player).getCurrentLevel();
    }

    public static void setAbilityPower(final Player player, final int level) {
        getExtensionFor(player).setCurrentLevel(level);
    }

    public static float getMana(final Player player) {
        return getExtensionFor(player).getCurrentMana();
    }

    public static void addMana(final Player player, final float mana) {
        final IEntityExtension extension = getExtensionFor(player);
        extension.setCurrentMana(extension.getCurrentMana() + mana);
    }

    public static void removeMana(final Player player, final float mana) {
        getExtensionFor(player).deductMana(mana);
    }

    /**
     * Get all entities in a given extent which are of a particular class type and based off a predicate.
     *
     * @param <T>       The type of entity
     * @param type      The type of the entity - needed due to type erasure.
     * @param location  The extent which to loop through
     * @param predicate The predicate to test, can be null.
     */
    public static <T extends Entity> Collection<T> getEntities(final Class<T> type, final Extent location,
        final Predicate<T> predicate) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(location);
        Stream<T> stream = location.getEntities().stream().filter(type::isInstance).map(type::cast);
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        return stream.collect(Collectors.toSet());
    }

    public static <T extends Entity> List<T> getSortedEntities(final Class<T> type, final Extent location,
        final Predicate<T> predicate, final Comparator<T> sorter) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(location);
        Stream<T> stream = location.getEntities().stream().filter(type::isInstance).map(type::cast);
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        if (sorter != null) {
            stream = stream.sorted(sorter);
        }
        return stream.collect(Collectors.toList());
    }


}
