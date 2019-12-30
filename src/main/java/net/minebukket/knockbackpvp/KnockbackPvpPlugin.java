package net.minebukket.knockbackpvp;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.Getter;
import net.minebukket.knockbackpvp.commands.BlocksCommand;
import net.minebukket.knockbackpvp.commands.VoteCommands;
import net.minebukket.knockbackpvp.handlers.abstractmanager.DataHandler;
import net.minebukket.knockbackpvp.handlers.mongo.MongoDataHandler;
import net.minebukket.knockbackpvp.listeners.*;
import net.minebukket.knockbackpvp.maps.GameMapLoader;
import net.minebukket.knockbackpvp.maps.GameMapManager;
import net.minebukket.knockbackpvp.maps.VoteMapManager;
import net.minebukket.knockbackpvp.objects.UserData;
import net.minebukket.knockbackpvp.objects.UserSettings;
import net.minebukket.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.DefaultCreator;
import us.sparknetwork.cm.CommandHandler;
import us.sparknetwork.cm.handlers.CommandMapHandler;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class KnockbackPvpPlugin extends JavaPlugin {

    @Getter
    private static KnockbackPvpPlugin plugin;

    private ListeningExecutorService executorService;

    private MongoClient mongoClient;
    @Getter
    private Morphia morphia;
    @Getter
    private Datastore datastore;

    @Getter
    private DataHandler<UUID, UserSettings> userSettingsHandler;
    @Getter
    private DataHandler<UUID, UserData> userDataHandler;

    @Getter
    private GameMapManager mapManager;
    @Getter
    private GameMapLoader mapLoader;
    @Getter
    private VoteMapManager voteMapManager;

    @Getter
    private ItemsListener itemsListener;

    @Override
    public void onLoad() {
        plugin = this;

        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

        try {
            this.setupMongo();
        } catch (RuntimeException e) {
            getLogger().severe("Failed to open connection to mongoDB, disabling plugin!");
            this.setEnabled(false);
        }
    }

    @Override
    public void onEnable() {

        mapLoader = new GameMapLoader(this);

        CommandHandler handler = new CommandMapHandler(this);

        if (mapLoader.getAllGameMaps().size() > 1) {
            this.getLogger().info("There is more than 1 map, enabling map rotating");
            mapManager = new GameMapManager(this, mapLoader, TimeUnit.MINUTES.toSeconds(10), TimeUnit.MINUTES.toSeconds(5));
            voteMapManager = new VoteMapManager(this, mapManager);
            handler.registerCommandClass(VoteCommands.class);

            this.getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        } else {
            this.getLogger().info("There is 1 or 0 maps, disabling map rotating");
        }

        handler.registerCommandClass(BlocksCommand.class);

        this.getServer().getPluginManager().registerEvents(new MapsListener(this), this);

        itemsListener = new ItemsListener(this);
        this.getServer().getPluginManager().registerEvents(itemsListener, this);

        this.getServer().getPluginManager().registerEvents(new JoinMessageListener(), this);
        this.getServer().getPluginManager().registerEvents(new UserListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BlockListeners(this, 25), this);
        this.getServer().getPluginManager().registerEvents(new JumpPlateListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BowListener(this), this);
        this.getServer().getPluginManager().registerEvents(new EnderPearlListener(this), this);
        this.getServer().getPluginManager().registerEvents(new WeatherListener(), this);
        this.getServer().getPluginManager().registerEvents(new StatsListener(userDataHandler), this);


        Bukkit.getOnlinePlayers().forEach(player -> {
            player.teleport(mapManager.getCurrentMap().getMapSpawnLocation());
            plugin.getItemsListener().loadInventory(player);
            player.sendMessage(mapManager.getCurrentMap().getMapInformation());
        });

    }

    @Override
    public void onDisable() {
        this.userDataHandler = null;
        this.userSettingsHandler = null;

        this.datastore = null;
        this.morphia = null;
        this.mongoClient = null;

        plugin = null;

        executorService.shutdown();
        executorService = null;

    }


    public void setupMongo() {
        Config dbConfiguration = new Config(this, "database");

        ServerAddress serverAddress = new ServerAddress(dbConfiguration.getString("mongo.host", "localhost"), dbConfiguration.getInt("mongo.port", 27017));

        if (dbConfiguration.getConfigurationSection("mongo.auth") != null) {
            MongoCredential credential = MongoCredential.createCredential(dbConfiguration.getString("mongo.auth.user", ""), dbConfiguration.getString("mongo.database", "knockbackpvp"), dbConfiguration.getString("mongo.auth.password").toCharArray());
            mongoClient = new MongoClient(serverAddress, Arrays.asList(credential));
        } else {
            mongoClient = new MongoClient(serverAddress);
        }

        morphia = new Morphia();

        morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return KnockbackPvpPlugin.plugin.getClassLoader();
            }
        });

        morphia.map(UserData.class);
        morphia.map(UserSettings.class);

        datastore = morphia.createDatastore(mongoClient, dbConfiguration.getString("mongo.database", "knockbackpvp"));

        userSettingsHandler = new MongoDataHandler<UUID, UserSettings>(this, executorService, datastore, UserSettings.class) {
            @Override
            protected UserSettings createNewObjectInstance(UUID key) {
                return new UserSettings(key);
            }
        };
        userDataHandler = new MongoDataHandler<UUID, UserData>(this, executorService, datastore, UserData.class) {

            @Override
            protected UserData createNewObjectInstance(UUID key) {
                return new UserData(key);
            }
        };
    }
}
