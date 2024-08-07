package fr.blockincraft.faylisia.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import fr.blockincraft.faylisia.Faylisia;
import fr.blockincraft.faylisia.Registry;
import fr.blockincraft.faylisia.configurable.DiscordData;
import fr.blockincraft.faylisia.configurable.Messages;
import fr.blockincraft.faylisia.displays.ScoreboardManager;
import fr.blockincraft.faylisia.entity.*;
import fr.blockincraft.faylisia.entity.interaction.HostileMobEntityType;
import fr.blockincraft.faylisia.items.CustomItemStack;
import fr.blockincraft.faylisia.items.event.DamageType;
import fr.blockincraft.faylisia.items.event.Handlers;
import fr.blockincraft.faylisia.map.Region;
import fr.blockincraft.faylisia.map.Spawn;
import fr.blockincraft.faylisia.menu.CraftingMenu;
import fr.blockincraft.faylisia.core.dto.CustomPlayerDTO;
import fr.blockincraft.faylisia.displays.Tab;
import fr.blockincraft.faylisia.menu.DisenchantmentMenu;
import fr.blockincraft.faylisia.menu.EnchantmentMenu;
import fr.blockincraft.faylisia.menu.InvseeMenu;
import fr.blockincraft.faylisia.player.permission.Ranks;
import fr.blockincraft.faylisia.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.time.Instant;
import java.util.*;

public class GameListeners implements Listener {
    private static final Registry registry = Faylisia.getInstance().getRegistry();
    private static final JDA discordBot = Faylisia.getInstance().getDiscordBot();

    /**
     * When player login, disallow it with a message depending on if server is in {@link Faylisia#development}
     */
    @EventHandler
    public void handleLogin(PlayerLoginEvent e) {
        if (!Bukkit.getWhitelistedPlayers().contains(e.getPlayer()) && Bukkit.hasWhitelist()) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Faylisia.development ? Messages.NO_JOIN_IN_DEV.get() : Messages.NO_JOIN_IN_MAINTENANCE.get());
        } else if (!Faylisia.isInitialized()) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Messages.NO_JOIN_DURING_STARTING.get());
        }
    }

    /**
     * Initialize permissions, {@link CustomPlayerDTO}, {@link ScoreboardManager}, {@link Tab} and resource pack for player which joined <br/>
     * Also edit join message and update other players {@link Tab}
     */
    @EventHandler
    public void handleJoin(PlayerJoinEvent e) {
        // Apply resource pack
        try {
            e.getPlayer().setResourcePack(
                    "http://faylisia.fr:11342/resource_pack", FileUtils.calcSHA1(FileUtils.getResourcePack()), true
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Change join message
        Map<String, String> parameters = new HashMap<>();

        parameters.put("%player_name%", e.getPlayer().getName());
        parameters.put("%player_display_name%", e.getPlayer().getDisplayName());

        e.setJoinMessage(Messages.PLAYER_JOIN_MESSAGE.get(parameters));

        // Initialize custom player and permissions
        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
        customPlayer.refreshStats();
        customPlayer.setEffectiveHealth(customPlayer.getMaxEffectiveHealth());
        Ranks.applyPermissions(e.getPlayer(), customPlayer.getRank());
        customPlayer.setLastName(e.getPlayer().getName());
        customPlayer.updateLastInventory();

        if (!Faylisia.development) {
            e.getPlayer().setFlySpeed((float) 0.1);
            e.getPlayer().setAllowFlight(false);
            e.getPlayer().setGameMode(GameMode.SURVIVAL);
            customPlayer.setCanBreak(false);
        }

        // Apply a client side mining fatigue effect to mining features
        PacketContainer packet = Faylisia.getInstance().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EFFECT);

        packet.getIntegers().write(0, e.getPlayer().getEntityId());
        packet.getIntegers().write(1, 4);
        packet.getBytes().write(0, (byte) 255);
        packet.getIntegers().write(2, 32767);
        packet.getBytes().write(1, (byte) 0);

        try {
            Faylisia.getInstance().getProtocolManager().sendServerPacket(e.getPlayer(), packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Update items if they are edited since last connection
        registry.refreshItems(e.getPlayer().getInventory());
        // Initialize player tab
        if (!Faylisia.getInstance().getScoreBoardManager().hasScoreboard(e.getPlayer())) {
            Faylisia.getInstance().getScoreBoardManager().createScoreboard(e.getPlayer());
        }

        // Initialize and update tab of player
        Tab.initPlayersTabPartFor(e.getPlayer());
        Tab.initStatsPartFor(e.getPlayer());
        Tab.refreshStatsPartFor(e.getPlayer());
        Tab.initGuildPartFor(e.getPlayer());
        Tab.refreshGuildPartFor(e.getPlayer());

        // Update other players tab
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId() != e.getPlayer().getUniqueId()) {
                Tab.refreshPlayersInTabFor(player);
                Tab.refreshRealsPlayersInTabFor(player);
            }
        }

        TextChannel chatInGame = discordBot.getTextChannelById(DiscordData.chatInGameId);
        if (chatInGame == null) return;

        chatInGame.sendMessage(new MessageBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setDescription(net.md_5.bungee.api.ChatColor.stripColor(ColorsUtils.translateAll(customPlayer.getRank().chatName.replace("%player_name%", customPlayer.getNameToUse().replace(" ", "\\_")))) + " a rejoint le serveur !")
                        .setColor(0x00FF11)
                        .setFooter(customPlayer.getNameToUse(), "https://minotar.net/avatar/" + customPlayer.getPlayer().toString() + ".png")
                        .build())
                .build()).queue();
    }

    /**
     * Change quit message and update {@link Tab} for others players
     */
    @EventHandler
    public void handleQuit(PlayerQuitEvent e) {
        // Change quit message
        Map<String, String> parameters = new HashMap<>();

        parameters.put("%player_name%", e.getPlayer().getName());
        parameters.put("%player_display_name%", e.getPlayer().getDisplayName());

        e.setQuitMessage(Messages.PLAYER_LEAVE_MESSAGE.get(parameters));

        // Update others players tab
        Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId() != e.getPlayer().getUniqueId()) {
                    Tab.refreshPlayersInTabFor(player);
                    Tab.refreshRealsPlayersInTabFor(player);
                }
            }
        }, 1);

        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());

        TextChannel chatInGame = discordBot.getTextChannelById(DiscordData.chatInGameId);
        if (chatInGame == null) return;

        chatInGame.sendMessage(new MessageBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setDescription(net.md_5.bungee.api.ChatColor.stripColor(ColorsUtils.translateAll(customPlayer.getRank().chatName.replace("%player_name%", customPlayer.getNameToUse().replace(" ", "\\_")))) + " a quitté le serveur.")
                        .setColor(0xFF0000)
                        .setFooter(customPlayer.getNameToUse(), "https://minotar.net/avatar/" + customPlayer.getPlayer().toString() + ".png")
                        .build())
                .build()).queue();
    }

    /**
     * Prevent block breaking for player which hasn't activated {@link CustomPlayerDTO#getCanBreak()} <br/>
     * Even if {@link GameListeners#handleInteraction(PlayerInteractEvent)} already do this
     */
    @EventHandler
    public void handleBlockBreak(BlockBreakEvent e) {
        // Check if player can break
        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
        if (customPlayer.getCanBreak()) {
            return;
        }

        // Else cancel it
        e.setCancelled(true);
    }

    /**
     * Prevent block placing for player which hasn't activated {@link CustomPlayerDTO#getCanBreak()} <br/>
     * Even if {@link GameListeners#handleInteraction(PlayerInteractEvent)} already do this
     */
    @EventHandler
    public void handleBlockPlace(BlockPlaceEvent e) {
        // Check if player can break
        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
        if (customPlayer.getCanBreak()) {
            return;
        }

        // Else cancel it
        e.setCancelled(true);
    }

    /**
     * Cancel redstone propagation
     */
    @EventHandler
    public void handleRedstoneUse(BlockRedstoneEvent e) {
        e.setNewCurrent(0);
    }

    /**
     * Cancel piston extend
     */
    @EventHandler
    public void handlePiston(BlockPistonExtendEvent e) {
        e.setCancelled(true);
    }

    /**
     * Cancel piston retract
     */
    @EventHandler
    public void handlePiston(BlockPistonRetractEvent e) {
        e.setCancelled(true);
    }

    /**
     * Cancel dispenser dispense
     */
    @EventHandler
    public void handleDispenser(BlockDispenseEvent e) {
        e.setCancelled(true);
    }

    /**
     * Actions to do when a player respawn
     */
    public static void handleRespawn(Player player) {
        CustomPlayerDTO pl = registry.getOrRegisterPlayer(player.getUniqueId());
        pl.onRespawn();
    }

    /**
     * Actions to do when a player death because player can't really dead
     */
    public static void handleDeath(Player player) {
        CustomPlayerDTO pl = registry.getOrRegisterPlayer(player.getUniqueId());
        pl.onDied();

        // Send message to player
        Map<String, String> parameters = new HashMap<>();

        parameters.put("%player_name%", player.getName());
        parameters.put("%player_displayname%", player.getDisplayName());

        player.sendMessage(Messages.YOU_ARE_DIED.get(parameters));

        // Teleport to spawn and respawn
        Spawn.teleportToSpawn(player);
        handleRespawn(player);
    }

    /**
     * When a player move, actualize custom entities which need to spawn in a radius of 75 blocks <br/>
     * This also refresh player stats in case of it have items with {@link Handlers} which depend on player region
     */
    @EventHandler
    public void handleMove(PlayerMoveEvent e) {
        // Get player location
        Location plLoc = e.getPlayer().getLocation();
        // Get all custom entities spawn locations
        for (Map.Entry<EntitySpawnLocation, CustomEntity> entry : Entities.spawnLocations.entrySet()) {
            // If entity wasn't killed too recently (to prevent farm using unloading and loading chunk)
            if (Date.from(Instant.now()).getTime() - (entry.getKey().getLastKill() + (entry.getKey().getType().getTickBeforeRespawn() < 0 ? 0 : entry.getKey().getType().getTickBeforeRespawn() * 50)) > 0) {
               // If entity isn't already spawned
                if (entry.getValue() == null || !entry.getValue().getEntity().isValid()){

                    // Check distance
                    if (AreaUtils.isInRadius(plLoc.getX(), plLoc.getY(), plLoc.getZ(), 75.0, entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ())) {
                        Entities.spawnLocations.put(entry.getKey(), entry.getKey().getType().spawn(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()));
                    }
                }
            }
        }

        if (e.getTo() == null) return;

        Set<Region> leaved = new HashSet<>();
        Set<Region> joined = new HashSet<>();

        Set<Region> from = new HashSet<>(List.of(Objects.requireNonNull(registry.getRegionsAt(e.getFrom()))));
        Set<Region> to = new HashSet<>(List.of(Objects.requireNonNull(registry.getRegionsAt(e.getTo()))));

        for (Region region : to) {
            if (!from.contains(region)) {
                joined.add(region);
            }
        }

        for (Region region : from) {
            if (!to.contains(region)) {
                leaved.add(region);
            }
        }

        // If player change region then refresh player stats
        if (leaved.size() > 0 || joined.size() > 0) {
            CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());

            customPlayer.refreshStats();
        }

        // Check if event must be cancelled
        boolean cancelled = false;

        for (Region region : joined) {
            if (!region.getEnterAction().onEnter(e.getPlayer(), from, to, region)) {
                cancelled = true;
                break;
            }
        }

        if (!cancelled) {
            for (Region region : leaved) {
                if (!region.getLeaveAction().onLeave(e.getPlayer(), from, to, region)) {
                    cancelled = true;
                    break;
                }
            }
        }

        e.setCancelled(cancelled);
    }

    /**
     * Prevent entities to target non player entities
     */
    @EventHandler
    public void handleTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) {
            e.setCancelled(true);
        }
    }

    /**
     * Cancel fall, drowning and suffocation damage and set damages to 0 to keep animation <br/>
     * Also calculate damage and show them to player to make entities and players attackable
     */
    @EventHandler
    public void handleDamage(EntityDamageEvent e) {
        // Return when damage are void because /kill command make void damage
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (e.getEntity() instanceof Player player) {
                handleDeath(player);
                e.setCancelled(true);
            }
            return;
        }

        EntityDamageEvent.DamageCause cause = e.getCause();

        // Cancel fall, drowning and suffocation damage to remove animation and damage
        switch (cause) {
            case FALL, DROWNING, SUFFOCATION -> {
                e.setCancelled(true);
                return;
            }
        }

        if (e instanceof EntityDamageByEntityEvent subE) {
            if (subE.getDamager() instanceof Player player && !(subE.getEntity() instanceof Player)) {
                // When player attack entity
                CustomEntity entity = registry.getCustomEntityByEntity(subE.getEntity());
                // Cancel if entity isn't a custom entity
                if (!(entity instanceof CustomLivingEntity livingEntity)) {
                    e.setCancelled(true);
                    return;
                }

                // Calculate damage to inflict
                CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(player.getUniqueId());
                boolean critic = customPlayer.generateCritical();
                long damage = Math.round(customPlayer.getDamage(critic));

                damage = HandlersUtils.getValueWithHandlers(customPlayer, "onDamage", damage, long.class, new HandlersUtils.Parameter[]{
                        new HandlersUtils.Parameter(player, Player.class),
                        new HandlersUtils.Parameter(entity, CustomEntity.class),
                        new HandlersUtils.Parameter(DamageType.MELEE_DAMAGE, DamageType.class)
                });

                // Spawn damage indicator and apply custom damage to entity
                PlayerUtils.spawnDamageIndicator(damage, critic, player, subE.getEntity().getLocation());
                livingEntity.takeDamage(damage, player);
            } else if (subE.getEntity() instanceof Player player && !(subE.getDamager() instanceof Player)) {
                // When entity attack player
                CustomEntity entity = registry.getCustomEntityByEntity(subE.getDamager());
                // Cancel if entity isn't a custom entity
                if (!(entity instanceof HostileCustomLivingEntity hostileEntity)) {
                    e.setCancelled(true);
                    return;
                }

                // Apply custom damage to player
                CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(player.getUniqueId());
                customPlayer.takeDamage(hostileEntity.getDamageFor(player), entity);
            }
        }

        // Set damage to 0 because we store health points ourselves
        e.setDamage(0.0);
    }

    /**
     * Set item owner of items dropped to make {@link GameListeners#handlePickup(EntityPickupItemEvent)} functional
     */
    @EventHandler
    public void handleDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Item item = e.getItemDrop();

        item.setOwner(player.getUniqueId());
    }

    /**
     * Make items only recoverable by player which drop it
     */
    @EventHandler
    public void handlePickup(EntityPickupItemEvent e) {
        if (e.getItem().getOwner() != e.getEntity().getUniqueId()) e.setCancelled(true);
    }

    /**
     * Cancel interaction, this contains breaking/placing blocks and call all player <br/>
     * {@link Handlers#onInteract(Player, Block, boolean, EquipmentSlot, boolean, boolean, CustomItemStack)} using {@link HandlersUtils}
     */
    @EventHandler
    public void handleInteraction(PlayerInteractEvent e) {
        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
        if (customPlayer.getCanBreak()) {
            return;
        }

        boolean isRightClick = e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK;

        HandlersUtils.callHandlers(customPlayer, "onInteract", new HandlersUtils.Parameter[]{
                new HandlersUtils.Parameter(e.getPlayer(), Player.class),
                new HandlersUtils.Parameter(e.getClickedBlock(), Block.class),
                new HandlersUtils.Parameter(isRightClick, boolean.class),
                new HandlersUtils.Parameter(e.getHand(), EquipmentSlot.class)
        });

        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
            new CraftingMenu().open(e.getPlayer());
        }
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.GRINDSTONE) {
            new DisenchantmentMenu().open(e.getPlayer());
        }
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ANVIL) {
            new EnchantmentMenu().open(e.getPlayer());
        }

        e.setCancelled(true);
    }

    /**
     * Cancel interact with entities to prevent using shears on sheep or bucket on cow
     */
    @EventHandler
    public void handleEntityInteraction(PlayerInteractEntityEvent e) {
        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
        if (customPlayer.getCanBreak()) {
            return;
        }

        e.setCancelled(true);
    }

    /**
     * Cancel spawner to spawn entities
     */
    @EventHandler
    public void handleSpawnerSpawning(SpawnerSpawnEvent e) {
        e.setCancelled(true);
    }

    /**
     * Cancel creature spawn naturally
     */
    @EventHandler
    public void handleCreatureSpawn(CreatureSpawnEvent e) {
        if (cancellableReason.contains(e.getSpawnReason())) {
            e.setCancelled(true);
        }
    }

    // All spawn reason which will be cancelled
    List<CreatureSpawnEvent.SpawnReason> cancellableReason = Arrays.asList(
            CreatureSpawnEvent.SpawnReason.BEEHIVE,
            CreatureSpawnEvent.SpawnReason.BREEDING,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
            CreatureSpawnEvent.SpawnReason.BUILD_WITHER,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.EGG,
            CreatureSpawnEvent.SpawnReason.ENDER_PEARL,
            CreatureSpawnEvent.SpawnReason.SLIME_SPLIT,
            CreatureSpawnEvent.SpawnReason.SPELL,
            CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE,
            CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
            CreatureSpawnEvent.SpawnReason.DROWNED,
            CreatureSpawnEvent.SpawnReason.INFECTION,
            CreatureSpawnEvent.SpawnReason.LIGHTNING,
            CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK,
            CreatureSpawnEvent.SpawnReason.SHEARED,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.TRAP,
            CreatureSpawnEvent.SpawnReason.PIGLIN_ZOMBIFIED
    );

    /**
     * Cancel totem of {@link Material#TOTEM_OF_UNDYING} to work
     */
    @EventHandler
    public void handleResurrection(EntityResurrectEvent e) {
        e.setCancelled(true);
    }

    /**
     * Cancel entity burning to prevent zombies, skeletons... to burn cause of day <br/>
     * Todo: custom time to show day but set night and remove it
     */
    @EventHandler
    public void handleEntityBurnCauseOfDay(EntityCombustEvent e) {
        e.setCancelled(true);
    }

    /**
     * Just refresh stats and update json inventory to api
     */
    @EventHandler
    public void handleInventoryChange(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
                CustomPlayerDTO player = registry.getOrRegisterPlayer(p.getUniqueId());
                player.refreshStats();
                player.updateLastInventory();

                MenuListener.menus.forEach((uuid, chestMenu) -> {
                    if (chestMenu instanceof InvseeMenu invseeMenu && invseeMenu.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                        invseeMenu.refreshMenu();
                    }
                });
            });
        }
    }

    /**
     * Just refresh stats and update json inventory to api
     */
    @EventHandler
    public void handleInventoryChange(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
                CustomPlayerDTO player = registry.getOrRegisterPlayer(p.getUniqueId());
                player.refreshStats();
                player.updateLastInventory();
                Tab.refreshStatsPartFor(p);

                MenuListener.menus.forEach((uuid, chestMenu) -> {
                    if (chestMenu instanceof InvseeMenu invseeMenu && invseeMenu.getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
                        invseeMenu.refreshMenu();
                    }
                });
            });
        }
    }

    /**
     * Just refresh stats and update json inventory to api
     */
    @EventHandler
    public void handleInventoryChange(PlayerDropItemEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
            CustomPlayerDTO player = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
            player.refreshStats();
            player.updateLastInventory();
            Tab.refreshStatsPartFor(e.getPlayer());

            MenuListener.menus.forEach((uuid, chestMenu) -> {
                if (chestMenu instanceof InvseeMenu invseeMenu && invseeMenu.getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
                    invseeMenu.refreshMenu();
                }
            });
        }, 1);
    }

    /**
     * Just refresh stats and update json inventory to api
     */
    @EventHandler
    public void handleInventoryChange(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
                CustomPlayerDTO player = registry.getOrRegisterPlayer(p.getUniqueId());
                player.refreshStats();
                player.updateLastInventory();
                Tab.refreshStatsPartFor(p);

                MenuListener.menus.forEach((uuid, chestMenu) -> {
                    if (chestMenu instanceof InvseeMenu invseeMenu && invseeMenu.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                        invseeMenu.refreshMenu();
                    }
                });
            }, 1);
        }
    }

    /**
     * Just refresh stats and update json inventory to api
     */
    @EventHandler
    public void handleInventoryChange(PlayerItemHeldEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
            CustomPlayerDTO player = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
            player.refreshStats();
            player.updateLastInventory();
            Tab.refreshStatsPartFor(e.getPlayer());

            MenuListener.menus.forEach((uuid, chestMenu) -> {
                if (chestMenu instanceof InvseeMenu invseeMenu && invseeMenu.getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
                    invseeMenu.refreshMenu();
                }
            });
        }, 1);
    }

    /**
     * Just refresh stats and update json inventory to api
     */
    @EventHandler
    public void handleInventoryChange(PlayerSwapHandItemsEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Faylisia.getInstance(), () -> {
            CustomPlayerDTO player = registry.getOrRegisterPlayer(e.getPlayer().getUniqueId());
            player.refreshStats();
            player.updateLastInventory();
            Tab.refreshStatsPartFor(e.getPlayer());

            MenuListener.menus.forEach((uuid, chestMenu) -> {
                if (chestMenu instanceof InvseeMenu invseeMenu && invseeMenu.getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
                    invseeMenu.refreshMenu();
                }
            });
        }, 1);
    }

    /**
     * Cancel food level change because we don't need to eat
     */
    @EventHandler
    public void handleHunger(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }
}
