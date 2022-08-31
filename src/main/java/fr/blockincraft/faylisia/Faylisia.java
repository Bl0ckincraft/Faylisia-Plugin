package fr.blockincraft.faylisia;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import fr.blockincraft.faylisia.api.RequestHandler;
import fr.blockincraft.faylisia.commands.*;
import fr.blockincraft.faylisia.configurable.Messages;
import fr.blockincraft.faylisia.configurable.DiscordData;
import fr.blockincraft.faylisia.core.dto.CustomPlayerDTO;
import fr.blockincraft.faylisia.core.entity.CustomPlayer;
import fr.blockincraft.faylisia.core.entity.DiscordTicket;
import fr.blockincraft.faylisia.entity.CustomEntity;
import fr.blockincraft.faylisia.entity.Entities;
import fr.blockincraft.faylisia.items.Items;
import fr.blockincraft.faylisia.listeners.ChatListeners;
import fr.blockincraft.faylisia.listeners.DiscordListeners;
import fr.blockincraft.faylisia.listeners.GameListeners;
import fr.blockincraft.faylisia.listeners.MenuListener;
import fr.blockincraft.faylisia.map.Regions;
import fr.blockincraft.faylisia.menu.ChestMenu;
import fr.blockincraft.faylisia.player.Classes;
import fr.blockincraft.faylisia.displays.ScoreboardManager;
import fr.blockincraft.faylisia.task.*;
import fr.blockincraft.faylisia.utils.ColorsUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Server;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This is the main {@link Plugin} class
 */
public final class Faylisia extends JavaPlugin {
    // Set server state
    public static final boolean development = true;

    // Store plugin instance and state
    private static Faylisia instance;
    private static boolean initialized = false;

    // Store non in-game class instances
    private SessionFactory sessionFactory;
    private Server apiServer;
    private JDA discordBot;

    // Store in-game class instances
    private Registry registry;
    private ScoreboardManager scoreBoardManager;
    private ProtocolManager protocolManager;

    /**
     * @return {@link Faylisia} instance
     */
    public static Faylisia getInstance() {
        return instance;
    }

    /**
     * @return if {@link Faylisia} initialization finished
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * @return {@link Hibernate} {@link SessionFactory} instance (database session factory)
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @return {@link JDA} instance (discord bot)
     */
    public JDA getDiscordBot() {
        return discordBot;
    }

    /**
     * @return {@link Registry} instance
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * @return {@link ScoreboardManager} instance
     */
    public ScoreboardManager getScoreBoardManager() {
        return scoreBoardManager;
    }

    /**
     * @return {@link ProtocolLib} {@link ProtocolManager} instance
     */
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    /**
     * {@link Plugin} load method
     */
    @Override
    public void onLoad() {
        // Initialize protocol manager
        protocolManager = ProtocolLibrary.getProtocolManager();
        // Adding packet listener to change skin
        protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Get all players
                List<PlayerInfoData> players = event.getPacket().getPlayerInfoDataLists().read(0);

                // If action isn't update latency
                if (event.getPacket().getPlayerInfoAction().read(0) != EnumWrappers.PlayerInfoAction.UPDATE_LATENCY) {
                    // For each player info data
                    for (PlayerInfoData player : players) {
                        // Check if name length is superior to 3 because fake player to do beautiful tab are 3 chars names
                        if (player.getProfile().getName().length() > 3) {
                            // Get custom player and classes from uuid
                            CustomPlayerDTO custom = registry.getOrRegisterPlayer(player.getProfile().getUUID());
                            Classes classes = custom.getClasses();

                            // Change the skin depending on the classes
                            WrappedGameProfile gameProfile = new WrappedGameProfile(player.getProfile().getUUID(), custom.getName());
                            gameProfile.getProperties().put("textures", WrappedSignedProperty.fromValues("textures", classes.skin.value, classes.skin.signature));

                            // Create the player info data
                            PlayerInfoData playerInfoData = new PlayerInfoData(gameProfile, player.getLatency(), player.getGameMode(), WrappedChatComponent.fromText(custom.getName()));

                            // Add player info data to list
                            players.remove(player);
                            players.add(playerInfoData);
                        }
                    }
                }

                // Rewrite packet player info data list
                event.getPacket().getPlayerInfoDataLists().write(0, players);
            }
        });
        // Adding packet listener to change server player info and MOTD colors
        protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Read packet
                WrappedServerPing serverPing = event.getPacket().getServerPings().read(0);
                List<WrappedGameProfile> profiles = new ArrayList<>();

                // Get players amount and max players
                int players = Bukkit.getOnlinePlayers().size();
                int maxPlayers = Bukkit.getMaxPlayers();

                // Calculate amount of bar depending on players amount and max players
                int bars = 44 - (int) ((String.valueOf(players).length() + 2 + String.valueOf(maxPlayers).length()) * (2.0 + 1 / 3));
                int per = (int) Math.floor(((double) bars) / maxPlayers * players);

                // If at least a player but 0 green bar, set 1 green bar
                if (players > 0 && per == 0) per = 1;

                // Build bar
                StringBuilder sb = new StringBuilder("&a&l" + "|".repeat(bars));
                sb.insert(per + 4, "&8&l");

                // Add fake profile to make beautiful list
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("&8&l&m-------------------")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("&d       Faylisia Engine")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("&d    Capacité du Serveur")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll(sb + " &d" + players + "&8/&d" + maxPlayers)));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("&dSite: faylis.xyz")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("&dDiscord: discord.faylis.xyz")));
                profiles.add(new WrappedGameProfile(UUID.randomUUID(), ColorsUtils.translateAll("&8&l&m-------------------")));

                // Replace profiles
                serverPing.setPlayers(profiles);

                // Update MOTD colors
                serverPing.setMotD(WrappedChatComponent.fromLegacyText(ColorsUtils.translateAll(Bukkit.getMotd())));
                // Rewrite packet
                event.getPacket().getServerPings().write(0, serverPing);
            }
        });

        // Set instance
        instance = this;

        // Try to create Hibernate session
        if (!initHibernateSession()) {
            // In case of error, stop the plugin
            this.getLogger().log(Level.SEVERE, "Cannot create an Hibernate Session factory, stopping.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Create of registry
        registry = new Registry();
        registry.init();

        // Try to make the discord bot ready
        if (!initDiscordBot()) {
            // In case of error, stop the plugin

            this.getLogger().log(Level.SEVERE, "Cannot start Discord Bot server, stopping.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }

    /**
     * {@link Plugin} start method
     */
    @Override
    public void onEnable() {
        // Initialize api web server
        initServer();
        // Try to start api web server
        if (!startServer()) {
            // In case of error, stop the plugin
            this.getLogger().log(Level.SEVERE, "Cannot start API server, stopping.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize gamerules in all worlds
        initGameRules();
        // Remove all vanilla recipes
        Bukkit.clearRecipes();
        // Initialization of scoreboard manager
        scoreBoardManager = new ScoreboardManager();
        // To register all items, regions... we need to call the class
        new Items();
        new Regions();
        new Entities();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GameListeners(), instance);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), instance);
        Bukkit.getPluginManager().registerEvents(new ChatListeners(), instance);

        // Bind command completer/executors
        PluginCommand itemCommand = Bukkit.getPluginCommand("items");
        if (itemCommand != null) {
            itemCommand.setExecutor(new ItemsExecutor());
            itemCommand.setTabCompleter(new ItemsCompleter());
        }
        PluginCommand spawnCommand = Bukkit.getPluginCommand("spawn");
        if (spawnCommand != null) {
            spawnCommand.setExecutor(new SpawnExecutor());
            spawnCommand.setTabCompleter(new SpawnCompleter());
        }
        PluginCommand breakCommand = Bukkit.getPluginCommand("break");
        if (breakCommand != null) {
            breakCommand.setExecutor(new BreakExecutor());
        }
        PluginCommand classCommand = Bukkit.getPluginCommand("class");
        if (classCommand != null) {
            classCommand.setExecutor(new ClassExecutor());
        }
        PluginCommand ranksCommand = Bukkit.getPluginCommand("ranks");
        if (ranksCommand != null) {
            ranksCommand.setExecutor(new RanksExecutor());
            ranksCommand.setTabCompleter(new RanksCompleter());
        }
        PluginCommand discordCommand = Bukkit.getPluginCommand("discord");
        if (discordCommand != null) {
            discordCommand.setExecutor(new DiscordExecutor());
            discordCommand.setTabCompleter(new DiscordCompleter());
        }
        PluginCommand linkCommand = Bukkit.getPluginCommand("link");
        if (linkCommand != null) {
            linkCommand.setExecutor(new LinkExecutor());
        }

        // Start tasks
        ScoreboardRefreshTask.startTask();
        StatsRegenTask.startTask();
        ActionBarTask.startTask();
        TabHeaderFooterTask.startTask();
        EntityQuitRegionTask.startTask();
        EntityTargetTask.startTask();
        EntityRemoveTask.startTask();

        // Make plugin initialized to true
        initialized = true;
    }

    /**
     * {@link Plugin} stop method
     */
    @Override
    public void onDisable() {
        // Close all inventories to prevent dupe
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChestMenu menu = MenuListener.menus.get(player.getUniqueId());
            if (menu != null) {
                menu.getMenuCloseHandler().onClose(player);
            }
            player.closeInventory();

            player.kickPlayer(Messages.KICK_ON_DISABLE.get());
        }

        // Cancel tasks because it can do error when instance is null and session factory closed
        Bukkit.getScheduler().cancelTasks(instance);

        // Close the session factory to prevent database bad request
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        sessionFactory = null;

        // Stop the discord bot
        try {
            discordBot.shutdown();
            discordBot = null;
        } catch (Throwable ignored) {

        }

        // Stop api web server
        try {
            apiServer.stop();
        } catch (Exception ignored) {

        }

        // Remove all custom entities
        for (CustomEntity entity : registry.getEntities()) {
            entity.remove();
        }

        // Unregister instance and vars
        registry = null;
        instance = null;
    }

    /**
     * Initialize {@link Hibernate} {@link SessionFactory}
     * @return if {@link Hibernate} {@link SessionFactory} was initialized
     */
    public boolean initHibernateSession() {
        try {
            // Create configuration with file 'hibernate.cfg.xml'
            Configuration configuration = new Configuration();
            configuration.configure();

            // Add entities
            configuration.addAnnotatedClass(CustomPlayer.class);
            configuration.addAnnotatedClass(DiscordTicket.class);

            // Set instance
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Initialize {@link Server}
     */
    public void initServer() {
        // Create a new server
        apiServer = new Server(11342);
        apiServer.setStopAtShutdown(true);
        // Add handler
        apiServer.setHandler(new RequestHandler());
    }

    /**
     * Start {@link Server}
     * @return if {@link Server} was started
     */
    public boolean startServer() {
        try {
            //Start api web server
            apiServer.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initialize {@link JDA}
     * @return if {@link JDA} was initialized
     */
    public boolean initDiscordBot() {
        try {
            // Connect to bot
            discordBot = JDABuilder.createDefault(DiscordData.token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES)
                    .setActivity(Activity.playing("Faylisia"))
                    .build();

            // Add listeners
            discordBot.addEventListener(new DiscordListeners());

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initialize game rules
     */
    public void initGameRules() {
        //Apply gamerules on all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            world.setGameRule(GameRule.DO_INSOMNIA, false);
            world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
            world.setGameRule(GameRule.DISABLE_RAIDS, true);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.DO_TILE_DROPS, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
            world.setGameRule(GameRule.SPAWN_RADIUS, 0);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            world.setGameRule(GameRule.NATURAL_REGENERATION, false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.DROWNING_DAMAGE, false);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
        }
    }
}
