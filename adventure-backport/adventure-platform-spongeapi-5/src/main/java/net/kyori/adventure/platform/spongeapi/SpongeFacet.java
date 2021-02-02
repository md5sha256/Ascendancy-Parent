/*
 * This file is part of adventure-platform, licensed under the MIT License.
 *
 * Copyright (c) 2018-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.platform.spongeapi;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.facet.Facet;
import net.kyori.adventure.platform.facet.FacetBase;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Index;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.ChatTypeMessageReceiver;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.Locatable;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.platform.facet.Knob.logUnsupported;
import static net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer.get;

class SpongeFacet<V> extends FacetBase<V> {
  protected SpongeFacet(final @Nullable Class<? extends V> viewerClass) {
    super(viewerClass);
  }

  public <K, S extends CatalogType> @Nullable S sponge(final @NonNull Class<S> spongeType, final @NonNull K value, final @NonNull Index<String, K> elements) {
    return Sponge.getRegistry()
            .getType(spongeType, elements.key(requireNonNull(value, "value")))
            .orElseGet(() -> {
              logUnsupported(this, value);
              return null;
            });
  }

  public <S extends CatalogType> @Nullable S sponge(final @NonNull Class<S> spongeType, final @NonNull Key identifier) {
    return Sponge.getRegistry()
            .getType(spongeType, requireNonNull(identifier, "Identifier must be non-null").asString())
            .orElseGet(() -> {
              logUnsupported(this, identifier);
              return null;
            });
  }

  static class Message<V> extends SpongeFacet<V> implements Facet.Message<V, Text> {
    protected Message(final @Nullable Class<? extends V> viewerClass) {
      super(viewerClass);
    }

    @Override
    public @NonNull Text createMessage(final @NonNull V viewer, final @NonNull Component message) {
      return get().serialize(message);
    }
  }

  static class Chat extends Message<MessageReceiver> implements Facet.Chat<MessageReceiver, Text> {
    protected Chat() {
      super(MessageReceiver.class);
    }

    @Override
    public void sendMessage(final @NonNull MessageReceiver viewer, final @NonNull Identity source, final @NonNull Text message, final @NonNull MessageType type) {
      viewer.sendMessage(message);
    }
  }

  static class ChatWithType extends Message<ChatTypeMessageReceiver> implements Facet.Chat<ChatTypeMessageReceiver, Text> {
    protected ChatWithType() {
      super(ChatTypeMessageReceiver.class);
    }

    private @Nullable ChatType type(final @NonNull MessageType type) {
      if(type == MessageType.CHAT) {
        return ChatTypes.CHAT;
      } else if(type == MessageType.SYSTEM) {
        return ChatTypes.SYSTEM;
      }
      logUnsupported(this, type);
      return null;
    }

    @Override
    public void sendMessage(final @NonNull ChatTypeMessageReceiver viewer, final @NonNull Identity source, final @NonNull Text message, final @NonNull MessageType type) {
      final ChatType chat = this.type(type);
      if(chat != null) {
        viewer.sendMessage(chat, message);
      }
    }
  }
  
  static class ActionBar extends Message<ChatTypeMessageReceiver> implements Facet.ActionBar<ChatTypeMessageReceiver, Text> {
    protected ActionBar() {
      super(ChatTypeMessageReceiver.class);
    }

    @Override
    public void sendMessage(final @NonNull ChatTypeMessageReceiver viewer, final @NonNull Text message) {
      viewer.sendMessage(ChatTypes.ACTION_BAR, message);
    }
  }
  
  static class Title extends Message<Viewer> implements Facet.Title<Viewer, Text, org.spongepowered.api.text.title.Title> {
    protected Title() {
      super(Viewer.class);
    }

    @Override
    public org.spongepowered.api.text.title.@NonNull Title createTitle(final @Nullable Text title, final @Nullable Text subTitle, final int inTicks, final int stayTicks, final int outTicks) {
      final org.spongepowered.api.text.title.Title.Builder builder = org.spongepowered.api.text.title.Title.builder();

      if(title != null) builder.title(title);
      if(subTitle != null) builder.subtitle(subTitle);
      if(inTicks > -1) builder.fadeIn(inTicks);
      if(stayTicks > -1) builder.stay(stayTicks);
      if(outTicks > -1) builder.fadeOut(outTicks);

      return builder.build();
    }

    @Override
    public void showTitle(final @NonNull Viewer viewer, final org.spongepowered.api.text.title.@NonNull Title title) {
      viewer.sendTitle(title);
    }

    @Override
    public void clearTitle(final @NonNull Viewer viewer) {
      viewer.clearTitle();
    }

    @Override
    public void resetTitle(final @NonNull Viewer viewer) {
      viewer.resetTitle();
    }
  }

  static class Position extends SpongeFacet<Viewer> implements Facet.Position<Viewer, Vector3d> {
    protected Position() {
      super(Viewer.class);
    }

    @Override
    public boolean isApplicable(final @NonNull Viewer viewer) {
      return super.isApplicable(viewer) && viewer instanceof Locatable;
    }

    @Nullable
    @Override
    public Vector3d createPosition(final @NonNull Viewer viewer) {
      if(viewer instanceof Locatable) {
        return ((Locatable) viewer).getLocation().getPosition();
      }
      return null;
    }

    @NonNull
    @Override
    public Vector3d createPosition(final double x, final double y, final double z) {
      return new Vector3d(x, y, z);
    }
  }

  static class Sound extends Position implements Facet.Sound<Viewer, Vector3d> {
    @Override
    public void playSound(final @NonNull Viewer viewer, final net.kyori.adventure.sound.@NonNull Sound sound, final @NonNull Vector3d vector) {
      final SoundType type = this.type(sound.name());
      final SoundCategory category = this.category(sound.source());

      if(type != null && category != null) {
        viewer.playSound(type, category, vector, sound.volume(), sound.pitch());
      } else if(type != null) {
        viewer.playSound(type, vector, sound.volume(), sound.pitch());
      }
    }

    @Override
    public void stopSound(final @NonNull Viewer viewer, final @NonNull SoundStop stop) {
      throw new UnsupportedOperationException("stopSound is not available in Sponge API 5!");
      /*
      final SoundType type = this.type(stop.sound());
      final SoundCategory category = this.category(stop.source());

      if(type != null && category != null) {
        viewer.stopSound(type, category);
      } else if(type != null) {
        viewer.stopSounds(type);
      } else if(category != null) {
        viewer.stopSounds(category);
      } else {
        viewer.stopSounds();
      }

       */
    }

    public @Nullable SoundType type(final @Nullable Key sound) {
      return sound == null ? null : this.sponge(SoundType.class, sound);
    }

    public @Nullable SoundCategory category(final net.kyori.adventure.sound.Sound.@Nullable Source source) {
      return source == null ? null : this.sponge(SoundCategory.class, source, net.kyori.adventure.sound.Sound.Source.NAMES);
    }
  }

  static class Book extends Message<Viewer> implements Facet.Book<Viewer, Text, BookView> {
    protected Book() {
      super(Viewer.class);
    }

    @NonNull
    @Override
    public BookView createBook(final @NonNull Text title, final @NonNull Text author, final @NonNull Iterable<Text> pages) {
      return BookView.builder().title(title).author(author).addPages(Lists.newArrayList(pages)).build();
    }

    @Override
    public void openBook(final @NonNull Viewer viewer, final @NonNull BookView book) {
      viewer.sendBookView(book);
    }
  }

  static class BossBarBuilder extends SpongeFacet<Player> implements Facet.BossBar.Builder<Player, SpongeFacet.BossBar> {
    protected BossBarBuilder() {
      super(Player.class);
    }

    @Override
    public SpongeFacet.@NonNull BossBar createBossBar(final @NonNull Collection<Player> viewers) {
      return new SpongeFacet.BossBar(viewers);
    }
  }

  static class BossBar extends Message<Player> implements Facet.BossBar<Player> {
    private final ServerBossBar bar;

    protected BossBar(final @NonNull Collection<Player> viewers) {
      super(Player.class);
      this.bar = ServerBossBar.builder().name(Text.of()).color(BossBarColors.PINK).overlay(BossBarOverlays.PROGRESS).visible(false).build();
      this.bar.addPlayers(viewers);
    }

    @Override
    public void bossBarInitialized(final net.kyori.adventure.bossbar.@NonNull BossBar bar) {
      Facet.BossBar.super.bossBarInitialized(bar);
      this.bar.setVisible(true);
    }

    @Override
    public void bossBarNameChanged(final net.kyori.adventure.bossbar.@NonNull BossBar bar, final @NonNull Component oldName, final @NonNull Component newName) {
      if(!this.bar.getPlayers().isEmpty()) {
        this.bar.setName(this.createMessage(this.bar.getPlayers().iterator().next(), newName));
      }
    }

    @Override
    public void bossBarProgressChanged(final net.kyori.adventure.bossbar.@NonNull BossBar bar, final float oldPercent, final float newPercent) {
      this.bar.setPercent(newPercent);
    }

    @Override
    public void bossBarColorChanged(final net.kyori.adventure.bossbar.@NonNull BossBar bar, final net.kyori.adventure.bossbar.BossBar.@NonNull Color oldColor, final net.kyori.adventure.bossbar.BossBar.@NonNull Color newColor) {
      final BossBarColor color = this.sponge(BossBarColor.class, newColor, net.kyori.adventure.bossbar.BossBar.Color.NAMES);
      if(color != null) {
        this.bar.setColor(color);
      }
    }

    @Override
    public void bossBarOverlayChanged(final net.kyori.adventure.bossbar.@NonNull BossBar bar, final net.kyori.adventure.bossbar.BossBar.@NonNull Overlay oldOverlay, final net.kyori.adventure.bossbar.BossBar.@NonNull Overlay newOverlay) {
      final BossBarOverlay overlay = this.sponge(BossBarOverlay.class, newOverlay, net.kyori.adventure.bossbar.BossBar.Overlay.NAMES);
      if(overlay != null) {
        this.bar.setOverlay(overlay);
      }
    }

    @Override
    public void bossBarFlagsChanged(final net.kyori.adventure.bossbar.@NonNull BossBar bar, final @NonNull Set<net.kyori.adventure.bossbar.BossBar.Flag> removedFlags, final @NonNull Set<net.kyori.adventure.bossbar.BossBar.Flag> addedFlags) {
      final Boolean fog = this.hasFlag(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG, removedFlags, addedFlags);
      if(fog != null) this.bar.setCreateFog(fog);
      final Boolean darkenScreen = this.hasFlag(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN, removedFlags, addedFlags);
      if(darkenScreen != null) this.bar.setDarkenSky(darkenScreen);
      final Boolean bossMusic = this.hasFlag(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC, removedFlags, addedFlags);
      if(bossMusic != null) this.bar.setPlayEndBossMusic(bossMusic);
    }

    private @Nullable Boolean hasFlag(final net.kyori.adventure.bossbar.BossBar.@NonNull Flag flag, final @NonNull Set<net.kyori.adventure.bossbar.BossBar.Flag> removedFlags, final @NonNull Set<net.kyori.adventure.bossbar.BossBar.Flag> addedFlags) {
      if(addedFlags.contains(flag)) return true;
      if(removedFlags.contains(flag)) return false;
      return null;
    }

    @Override
    public void addViewer(final @NonNull Player viewer) {
      this.bar.addPlayer(viewer);
    }

    @Override
    public void removeViewer(final @NonNull Player viewer) {
      this.bar.removePlayer(viewer);
    }

    @Override
    public boolean isEmpty() {
      return !this.bar.isVisible() || this.bar.getPlayers().isEmpty();
    }

    @Override
    public void close() {
      this.bar.removePlayers(this.bar.getPlayers());
    }
  }

  static class ViaHook implements Function<Player, UserConnection> {
    @Override
    public UserConnection apply(final @NonNull Player player) {
      return Via.getManager().getConnection(player.getUniqueId());
    }
  }

  static class TabList extends Message<Player> implements Facet.TabList<Player, Text> {

    TabList() {
      super(Player.class);
    }

    @Override
    public void send(final Player viewer, final @Nullable Text header, final @Nullable Text footer) {
      if(header != null && footer != null) {
        viewer.getTabList().setHeaderAndFooter(header, footer);
      } else if(header != null) {
        viewer.getTabList().setHeader(header);
      } else if(footer != null) {
        viewer.getTabList().setFooter(footer);
      }
    }
  }
}
