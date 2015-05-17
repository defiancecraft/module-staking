package com.defiancecraft.modules.staking;

import java.io.IOException;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.command.CommandRegistry;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.menu.MenuListener;
import com.defiancecraft.core.modules.Module;
import com.defiancecraft.core.util.FileUtils;
import com.defiancecraft.core.util.GsonUtils;
import com.defiancecraft.core.util.Lang;
import com.defiancecraft.modules.staking.commands.AdminCommands;
import com.defiancecraft.modules.staking.commands.StakingCommands;
import com.defiancecraft.modules.staking.config.MainConfig;
import com.defiancecraft.modules.staking.listeners.CancellationListener;
import com.defiancecraft.modules.staking.listeners.StakingListener;
import com.defiancecraft.modules.staking.stakes.ArenaManager;
import com.defiancecraft.modules.staking.stakes.StakeManager;
import com.google.gson.Gson;

public class Staking extends JavaPlugin implements Module {

	public static final String NAME = "Staking";
	public static final String LANG_NO_WORLDEDIT;
	public static final String LANG_NO_SELECTION;
	public static final String LANG_SAVE_SUCCESS;
	public static final String LANG_SAVE_FAILURE;
	public static final String LANG_BAD_USAGE;
	public static final String LANG_PLAYER_NOT_FOUND;
	public static final String LANG_NOT_CONFIGURED;
	public static final String LANG_NOT_IN_AREA;
	public static final String LANG_ACCEPT_STAKE;
	public static final String LANG_ALREADY_STAKING;
	public static final String LANG_GONE_OFFLINE;
	public static final String LANG_LEFT_AREA_CANCEL;
	public static final String LANG_SENT_REQUEST;
	public static final String LANG_NO_REQUEST;
	public static final String LANG_ACCEPTED_STAKE;
	public static final String LANG_STAKE_YOURSELF;
	public static final String LANG_SHUTDOWN;
	public static final String LANG_QUEUED;
	public static final String LANG_GENERIC_CANCEL;
	public static final String LANG_INVENTORY_FULL;
	public static final String LANG_NOT_ENOUGH_MONEY;
	public static final String LANG_CLICK_TO_ACCEPT;
	public static final String LANG_CLICK_TO_UNACCEPT;
	public static final String LANG_REMOTE_ACCEPTED;
	public static final String LANG_REMOTE_NOT_ACCEPTED;
	public static final String LANG_CLICK_TO_CHOOSE;
	public static final String LANG_WAIT_FOR_REMOTE;
	public static final String LANG_REMOTE_CHOSEN;
	public static final String LANG_ACCEPTED_KIT;
	public static final String LANG_TITLE_COUNTDOWN;
	public static final String LANG_TITLE_GO;
	public static final String LANG_WINRAR;
	public static final String LANG_LOSER;
	public static final String LANG_RANDOM_FAILURE;
	public static final String LANG_NOT_ENOUGH_SPACE;
	public static final String LANG_LEFT_AREA;
	public static final String LANG_ENTERED_AREA;
	
	private static MainConfig config;
	
	private StakeManager manager;
	
    public void onEnable() {

    	// Load config
        Staking.config = getConfig(MainConfig.class);
        
        // Create managers
        this.manager = new StakeManager(this);
        ArenaManager.init(config.arenas, this);
        
        // Register events
        MenuListener.register(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CancellationListener(manager), this);
        pm.registerEvents(new StakingListener(manager), this);
        
        // Register admin commands
        CommandRegistry.registerPlayerCommand(this, "staking", "defiancecraft.staking.help", AdminCommands::help);
        CommandRegistry.registerPlayerSubCommand("staking", "setarea", "defiancecraft.staking.setarea", AdminCommands::setArea);
        CommandRegistry.registerPlayerSubCommand("staking", "arena", "defiancecraft.staking.arena", AdminCommands::arena);
        CommandRegistry.registerPlayerSubCommand("staking", "respawn", "defiancecraft.staking.respawn", AdminCommands::respawn);
        CommandRegistry.registerPlayerSubCommand("staking", "addkit", "defiancecraft.staking.addkit", AdminCommands::addKit);
        
        // Register staking commands
        StakingCommands cmds = new StakingCommands(manager);
        CommandRegistry.registerPlayerCommand(this, "stake", "defiancecraft.staking.stake", cmds::stake);
        CommandRegistry.registerPlayerCommand(this, "accept", "defiancecraft.staking.accept", cmds::accept);

    }
    
    public void onDisable() {
    	
    	manager.shutdown();
    	
    }
    
    @Override
    public String getCanonicalName() {
        return Staking.NAME;
    }

    @Override
    public Collection[] getCollections() {
        return new Collection[] {};
    }

    public static MainConfig getConfiguration() {
    	return config;
    }
    
    public static boolean saveConfiguration(MainConfig config) {
    	try {
    		Staking.config = config;
    		DefianceCore.getModuleConfig().configs.put(NAME, GsonUtils.toMap(new Gson().toJsonTree(config).getAsJsonObject()));
    		DefianceCore.getModuleConfig().save(FileUtils.getSharedConfig("modules.json"));
    		return true;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    }
    
    static {
    	
        LANG_NO_WORLDEDIT        = Lang.setDefault(NAME, "no_worldedit", "&cWorldEdit is not enabled! Please install/enable WorldEdit first.");
        LANG_NO_SELECTION        = Lang.setDefault(NAME, "no_selection", "&cPlease select a region!");
        LANG_SAVE_SUCCESS        = Lang.setDefault(NAME, "save_failure", "&aSaved successfully!");
        LANG_SAVE_FAILURE        = Lang.setDefault(NAME, "save_success", "&cFailed to save. Exception probably in console/log.");
        LANG_BAD_USAGE           = Lang.setDefault(NAME, "bad_usage", "&fUsage: %s");
        LANG_PLAYER_NOT_FOUND    = Lang.setDefault(NAME, "player_not_found", "&cPlayer %s is not online!");
        LANG_NOT_CONFIGURED      = Lang.setDefault(NAME, "no_staking_area", "&cStaking area or arenas are not defined!");
        LANG_NOT_IN_AREA         = Lang.setDefault(NAME, "not_in_area", "&c%s not in the staking area.");
        LANG_ACCEPT_STAKE        = Lang.setDefault(NAME, "accept_stake", "&6&l&nClick to accept stake from %s!");
        LANG_ALREADY_STAKING     = Lang.setDefault(NAME, "already_staking", "&cYou are already in a stake with someone! Cancel it first.");
        LANG_GONE_OFFLINE        = Lang.setDefault(NAME, "gone_offline", "&cPlayer %s has left the server; stake cancelled.");
        LANG_LEFT_AREA_CANCEL    = Lang.setDefault(NAME, "left_area", "&c%s has left the staking area; stake cancelled.");
        LANG_SENT_REQUEST        = Lang.setDefault(NAME, "sent_request", "&aSent a stake request to %s!");
        LANG_NO_REQUEST          = Lang.setDefault(NAME, "no_request", "&cYou do not have a staking request from %s!");
        LANG_ACCEPTED_STAKE      = Lang.setDefault(NAME, "accepted_stake", "&aPlayer %s accepted your stake request!");
        LANG_STAKE_YOURSELF      = Lang.setDefault(NAME, "stake_yourself", "&cYou cannot start a stake with yourself!");
        LANG_SHUTDOWN            = Lang.setDefault(NAME, "shutdown", "&cServer shutting down; staking cancelled.");
        LANG_QUEUED              = Lang.setDefault(NAME, "queued", "&7There are no free arenas. You have been put in a queue instead.");
        LANG_GENERIC_CANCEL      = Lang.setDefault(NAME, "generic_cancel", "&cPlayer %s cancelled the stake.");
        LANG_INVENTORY_FULL      = Lang.setDefault(NAME, "inventory_full", "&cYour inventory was full, so your items were dropped on the floor.");
        LANG_NOT_ENOUGH_MONEY    = Lang.setDefault(NAME, "not_enough_money", "&cYou do not have enough tokens!");
        LANG_CLICK_TO_ACCEPT     = Lang.setDefault(NAME, "click_to_accept", "&a&lClick To Accept!");
    	LANG_CLICK_TO_UNACCEPT   = Lang.setDefault(NAME, "click_to_unaccept", "&a&lAccepted! &9&oClick to cancel");
    	LANG_REMOTE_ACCEPTED     = Lang.setDefault(NAME, "remote_accepted", "&9Partner has &a&laccepted");
    	LANG_REMOTE_NOT_ACCEPTED = Lang.setDefault(NAME, "remote_not_accepted", "&9Partner has &a&lnot accepted");
    	LANG_CLICK_TO_CHOOSE     = Lang.setDefault(NAME, "click_to_choose", "&a&lChoose kit %s");
    	LANG_WAIT_FOR_REMOTE     = Lang.setDefault(NAME, "wait_for_remote", "&6&lWaiting for partner to choose");
    	LANG_REMOTE_CHOSEN       = Lang.setDefault(NAME, "remote_chosen", "&6&lClick to accept");
    	LANG_ACCEPTED_KIT        = Lang.setDefault(NAME, "accepted_kit", "&a&lAccepted kit");
    	LANG_TITLE_COUNTDOWN     = Lang.setDefault(NAME, "title_countdown", "&f&l%s");
    	LANG_TITLE_GO            = Lang.setDefault(NAME, "title_go", "&f&lFight!");
    	LANG_WINRAR              = Lang.setDefault(NAME, "winrar", "&aWinRar! You won %s tokens!");
    	LANG_LOSER               = Lang.setDefault(NAME, "loser", "&bYou lost :( The winrar was actually %s.");
    	LANG_RANDOM_FAILURE      = Lang.setDefault(NAME, "random_failure", "&cAn error occurred while giving you your tokens! Don't worry, ask an admin for them.");
    	LANG_NOT_ENOUGH_SPACE    = Lang.setDefault(NAME, "not_enough_space", "&cThe other player does not have enough inventory space for the items. Please remove some!");
    	LANG_LEFT_AREA           = Lang.setDefault(NAME, "left_area", "&bYou have left the staking area.");
    	LANG_ENTERED_AREA        = Lang.setDefault(NAME, "left_area", "&bYou have entered the staking area.");
    	
    	try {
			Lang.save();
		} catch (IOException e) {}
    	
    }
    
}
