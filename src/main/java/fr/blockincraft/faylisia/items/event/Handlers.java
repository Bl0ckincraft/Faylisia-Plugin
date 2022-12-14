package fr.blockincraft.faylisia.items.event;

import fr.blockincraft.faylisia.blocks.BlockType;
import fr.blockincraft.faylisia.blocks.CustomBlock;
import fr.blockincraft.faylisia.core.entity.CustomPlayer;
import fr.blockincraft.faylisia.entity.CustomEntity;
import fr.blockincraft.faylisia.entity.CustomEntityType;
import fr.blockincraft.faylisia.entity.CustomLivingEntity;
import fr.blockincraft.faylisia.entity.interaction.MobEntityType;
import fr.blockincraft.faylisia.entity.loot.Loot;
import fr.blockincraft.faylisia.items.CustomItem;
import fr.blockincraft.faylisia.items.CustomItemStack;
import fr.blockincraft.faylisia.items.weapons.DamageItemModel;
import fr.blockincraft.faylisia.player.Stats;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * In order, the handlers are executed from the main hand items, armor sets, armor slots items then others items. <br/>
 * inHand equals if the item which has the handler is in main hand
 */
public interface Handlers {
    /**
     * Called when a handler was called, for example this is used in {@link EnchantmentHandlers} to prevent using model instance by throwing a
     * {@link RuntimeException}
     */
    default void onHandlerCall() {

    }

    /**
     * Event called when probability to drop a {@link Loot} <br/>
     * This event was called after applying the <b>luck</b> {@link Stats}
     * @param player player which will get the loot
     * @param item item generated
     * @param probability current probability
     * @param baseRolls amount of rolls before applying any modifiers
     * @param isRare if base probability <= 5%
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return probability of the loot
     */
    default int getLootProbability(@NotNull Player player, @NotNull CustomItemStack item, int on, int baseRolls, boolean isRare, Loot.LootType lootType, int probability, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return probability;
    }

    /**
     * Event called when amount of a {@link Loot}
     * @param player player which will get the loot
     * @param item item generated
     * @param probability probability to get the loot
     * @param rolls amount of rolls
     * @param isRare if base probability <= 5%
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return rolls of the loot
     */
    default int getLootRolls(@NotNull Player player, @NotNull CustomItemStack item, int probability, int on, boolean isRare, Loot.LootType lootType, int rolls, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return rolls;
    }

    /**
     * Event called when amount of a {@link Loot} <br/>
     * This event was called after applying the {@link Loot.AmountFunction} of the loot
     * @param player player which will get the loot
     * @param item item generated
     * @param probability probability to get the loot
     * @param amount current amount (so the {@link Loot.AmountFunction} result)
     * @param rolls amount of rolls
     * @param isRare if base probability <= 5%
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return amount of the loot
     */
    default int getLootAmount(@NotNull Player player, @NotNull CustomItemStack item, int probability, int on, int rolls, boolean isRare, Loot.LootType lootType, int amount, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return amount;
    }

    /**
     * This event was called when a {@link Player} interact <br/>
     * It can be util to create abilities...
     * @param player player which interact
     * @param clickedBlock block interacted
     * @param inHand if the item witch has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @param isRightClick if the click is a right click
     * @param hand the hand of the interaction
     */
    default void onInteract(@NotNull Player player, @Nullable Block clickedBlock, boolean isRightClick, @NotNull EquipmentSlot hand, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
    }

    /**
     * This event was called when a {@link Player} damage a {@link CustomEntity}
     * @param player player which do damages
     * @param customEntity entity which will receive damages
     * @param damageType type of damage inflicted
     * @param damage damages to do
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return damages to deal
     */
    default long onDamage(@NotNull Player player, @NotNull CustomEntity customEntity, @NotNull DamageType damageType, long damage, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return damage;
    }

    /**
     * This event was called when a {@link Player} take damage from a {@link CustomEntity}
     * @param player player which receive the damages
     * @param customEntity entity which do damages
     * @param damageType type of damage taken
     * @param damageTaken damages to take
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return damages to receive
     */
    default long onTakeDamage(@NotNull Player player, @NotNull CustomEntity customEntity, @NotNull DamageType damageType, long damageTaken, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return damageTaken;
    }

    /**
     * This event was called during stats calculation of a {@link CustomPlayer}, it was called when we get {@code defaultValue} of a {@link Stats}
     * @param player player which we calculate the stats
     * @param stat {@link Stats} which we get the {@code defaultValue}
     * @param value current value of the stat
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return value of the stat
     */
    default double getDefaultStat(@NotNull Player player, @NotNull Stats stat, double value, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return value;
    }

    /**
     * This event was called when a player regen his health
     * @param player player which regen
     * @param regen effective health which will be gain
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return effective health to gain
     */
    default long onRegenHealth(@NotNull Player player, long regen, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return regen;
    }

    /**
     * This event was called when a player regen his magical power
     * @param player player which regen
     * @param regen magical power which will be gain
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return magical power to gain
     */
    default long onRegenMagicalPower(@NotNull Player player, long regen, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return regen;
    }

    /**
     * This event was called when we get the stat of a {@link CustomItem}
     * @param player player which has the item
     * @param customItem item which we get the stat
     * @param stat {@link Stats} of the item which we want to get
     * @param value value of this stat
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return new value of the stat
     */
    default double calculateItemStat(@NotNull Player player, @NotNull CustomItemStack customItemStack, @NotNull Stats stat, double value, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return value;
    }

    /**
     * This event was called when we get the stat of a {@link CustomPlayer}
     * @param player player which we get the stat
     * @param stat {@link Stats} of the player which we want to get
     * @param value value of the stat
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return value to return
     */
    default double getStat(@NotNull Player player, @NotNull Stats stat, double value, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return value;
    }

    /**
     * This event was called when we get damages of a {@link CustomPlayer} <br/>
     * {@link Stats} like strength, critic damages (if critic) are already applied
     * @param player player which we get the damages
     * @param value damages of the player
     * @param inHand if the item which has the handler is in hand
     * @param inArmorSlot if the item which has the handler is in an armor slot
     * @return damages to return
     */
    default double getDamage(@NotNull Player player, double value, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return value;
    }

    default boolean onDeath(@NotNull Player player, boolean cancel, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
        return cancel;
    }

    default void breakBlock(@NotNull Player player, @NotNull CustomBlock block, @NotNull BlockType blockType, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
    }

    default void killMob(@NotNull Player player, @NotNull CustomLivingEntity entity, @NotNull MobEntityType entityType, boolean inHand, boolean inArmorSlot, @Nullable CustomItemStack thisItemStack) {
        onHandlerCall();
    }
}
