package heads.inktest;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;


public class Main extends JavaPlugin implements Listener {

	
	public static boolean round;
	
	@Override
	public void onEnable( ) {
		System.out.println("Heads plugin loaded!");
		round = false;
		 Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		if (!(round)) return;
		Block block = event.getBlock();
		if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;
		Skull skull = (Skull) block.getState();
		
		Bukkit.broadcastMessage(ChatColor.YELLOW + event.getPlayer().getName() + " has found " + skull.getOwner() + "'s head!");

		getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard players add "+event.getPlayer().getName()+" headsbroken 1");
		getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard players remove "+skull.getOwner()+" headsbroken 1");
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(cmd.getName().equalsIgnoreCase("heads"))) return false;
		if (!(sender.hasPermission("heads.commands"))) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /heads [giveheads/start/stop]");
			return true;
		}
		
		switch(args[0].toLowerCase()) {
		case "giveheads":
			
			for(Player player : Bukkit.getOnlinePlayers()){
			    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			    SkullMeta meta = (SkullMeta) head.getItemMeta();
			    meta.setOwningPlayer(player);
			    head.setItemMeta(meta);
			    player.getInventory().addItem(head);
			}
			break;
		case "start":
			round = true;
			
			getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard objectives remove headsbroken");
			getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard objectives add headsbroken dummy \"POINTS\"");
			getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard players set @a headsbroken 1");
			getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard objectives setdisplay sidebar headsbroken");
			break;
		case "stop":
			round = false;
			getServer().dispatchCommand(getServer().getConsoleSender(), "scoreboard objectives remove headsbroken");
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Usage: /heads [giveheads/start/stop]");
			break;
		}
		return true;
	}

}
