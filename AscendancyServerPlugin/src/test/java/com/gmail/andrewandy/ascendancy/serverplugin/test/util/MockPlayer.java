package com.gmail.andrewandy.ascendancy.serverplugin.test.util;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MockPlayer implements Player {

    private final UUID uniqueID = UUID.randomUUID();

    @Override
    public UUID getUniqueId() {
        return uniqueID;
    }

    @Override
    public Optional<Container> getOpenInventory() {
        return Optional.empty();
    }

    @Override
    public Optional<Container> openInventory(final Inventory inventory, final Cause cause)
            throws IllegalArgumentException {
        return Optional.empty();
    }

    @Override
    public boolean closeInventory(final Cause cause) throws IllegalArgumentException {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public ChatVisibility getChatVisibility() {
        return null;
    }

    @Override
    public boolean isChatColorsEnabled() {
        return false;
    }

    @Override
    public MessageChannelEvent.Chat simulateChat(final Text message, final Cause cause) {
        return null;
    }

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return null;
    }

    @Override
    public PlayerConnection getConnection() {
        return null;
    }

    @Override
    public void sendResourcePack(final ResourcePack pack) {

    }

    @Override
    public TabList getTabList() {
        return null;
    }

    @Override
    public void kick() {

    }

    @Override
    public void kick(Text reason) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {

    }

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {

    }

    @Override
    public Inventory getEnderChestInventory() {
        return null;
    }

    @Override
    public boolean respawnPlayer() {
        return false;
    }

    @Override
    public Optional<Entity> getSpectatorTarget() {
        return Optional.empty();
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {

    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {

    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {

    }

    @Override
    public void playSound(
            SoundType sound, SoundCategory category, Vector3d position,
            double volume
    ) {

    }

    @Override
    public void playSound(
            SoundType sound, SoundCategory category, Vector3d position, double volume,
            double pitch
    ) {

    }

    @Override
    public void playSound(
            SoundType sound, SoundCategory category, Vector3d position, double volume,
            double pitch, double minVolume
    ) {

    }

    @Override
    public void sendTitle(Title title) {

    }

    @Override
    public void sendBookView(BookView bookView) {

    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {

    }

    @Override
    public void resetBlockChange(int x, int y, int z) {

    }

    @Override
    public Optional<ItemStack> getHelmet() {
        return Optional.empty();
    }

    @Override
    public void setHelmet(@Nullable ItemStack helmet) {

    }

    @Override
    public Optional<ItemStack> getChestplate() {
        return Optional.empty();
    }

    @Override
    public void setChestplate(@Nullable ItemStack chestplate) {

    }

    @Override
    public Optional<ItemStack> getLeggings() {
        return Optional.empty();
    }

    @Override
    public void setLeggings(@Nullable ItemStack leggings) {

    }

    @Override
    public Optional<ItemStack> getBoots() {
        return Optional.empty();
    }

    @Override
    public void setBoots(@Nullable ItemStack boots) {

    }

    @Override
    public Optional<ItemStack> getItemInHand(HandType handType) {
        return Optional.empty();
    }

    @Override
    public void setItemInHand(HandType hand, @Nullable ItemStack itemInHand) {

    }

    @Override
    public Vector3d getHeadRotation() {
        return null;
    }

    @Override
    public void setHeadRotation(Vector3d rotation) {

    }

    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return null;
    }

    @Override
    public Random getRandom() {
        return null;
    }

    @Override
    public boolean setLocation(Location<World> location) {
        return false;
    }

    @Override
    public Vector3d getRotation() {
        return null;
    }

    @Override
    public void setRotation(Vector3d rotation) {

    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation) {
        return false;
    }

    @Override
    public boolean setLocationAndRotation(
            Location<World> location, Vector3d rotation,
            EnumSet<RelativePositions> relativePositions
    ) {
        return false;
    }

    @Override
    public Vector3d getScale() {
        return null;
    }

    @Override
    public void setScale(Vector3d scale) {

    }

    @Override
    public Transform<World> getTransform() {
        return null;
    }

    @Override
    public boolean setTransform(Transform<World> transform) {
        return false;
    }

    @Override
    public boolean transferToWorld(World world, Vector3d position) {
        return false;
    }

    @Override
    public Optional<AABB> getBoundingBox() {
        return Optional.empty();
    }

    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    @Override
    public DataTransactionResult addPassenger(Entity entity) {
        return null;
    }

    @Override
    public DataTransactionResult removePassenger(Entity entity) {
        return null;
    }

    @Override
    public DataTransactionResult clearPassengers() {
        return null;
    }

    @Override
    public Optional<Entity> getVehicle() {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult setVehicle(@Nullable Entity entity) {
        return null;
    }

    @Override
    public Entity getBaseVehicle() {
        return null;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void remove() {

    }

    @Override
    public boolean damage(double damage, DamageSource damageSource, Cause cause) {
        return false;
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.empty();
    }

    @Override
    public void setCreator(@Nullable UUID uuid) {

    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.empty();
    }

    @Override
    public void setNotifier(@Nullable UUID uuid) {

    }

    @Override
    public EntityArchetype createArchetype() {
        return null;
    }

    @Override
    public GameProfile getProfile() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.empty();
    }

    @Override
    public boolean validateRawData(DataView container) {
        return false;
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {

    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return null;
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        return false;
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        return null;
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value, Cause cause) {
        return null;
    }

    @Override
    public DataTransactionResult offer(
            DataManipulator<?, ?> valueContainer,
            MergeFunction function
    ) {
        return null;
    }

    @Override
    public DataTransactionResult offer(
            DataManipulator<?, ?> valueContainer, MergeFunction function,
            Cause cause
    ) {
        return null;
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        return null;
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        return null;
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        return null;
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return null;
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return null;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public DataHolder copy() {
        return null;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return null;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return null;
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        return false;
    }

    @Override
    public boolean canEquip(EquipmentType type, @Nullable ItemStack equipment) {
        return false;
    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        return Optional.empty();
    }

    @Override
    public boolean equip(EquipmentType type, @Nullable ItemStack equipment) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return Optional.empty();
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(
            Class<T> projectileClass,
            Vector3d velocity
    ) {
        return Optional.empty();
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return null;
    }

    @Override
    public Text getTeamRepresentation() {
        return null;
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.empty();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return null;
    }

    @Override
    public SubjectData getSubjectData() {
        return null;
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return null;
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return null;
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return false;
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return null;
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return Optional.empty();
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public Set<Context> getActiveContexts() {
        return null;
    }

    @Override
    public void sendMessage(ChatType type, Text message) {

    }

    @Override
    public void sendMessage(Text message) {

    }

    @Override
    public MessageChannel getMessageChannel() {
        return null;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {

    }

    @Override
    public Translation getTranslation() {
        return null;
    }

    @Override
    public Location<World> getLocation() {
        return null;
    }

}
