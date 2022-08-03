package heads.inktest;


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
	public static Map<String,Integer> count = new HashMap<String,Integer>();
	public static Map<String,Boolean> bcount = new HashMap<String,Boolean>();
	
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
		@SuppressWarnings("deprecation")
		String name = skull.getOwner()==null?"Steve":skull.getOwner();
		String bname = event.getPlayer().getName();
		Bukkit.broadcastMessage(ChatColor.YELLOW + bname + " has found " + name + "'s head!");
		if (count.get(bname) == null) {
			count.put(bname,0);
			bcount.put(bname, true);
		}
		if (count.get(name) == null) {
			count.put(name,0);
			bcount.put(name, true);
		}
		
		Integer current = count.get(bname);
		current += 1;
		count.put(bname, current);
		Boolean bcurrent = bcount.get(name);
		bcurrent = false;
		bcount.put(name, bcurrent);
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(cmd.getName().equalsIgnoreCase("heads"))) return false;
		if (!(sender.hasPermission("heads.commands"))) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /heads [giveheads/start/stop/count]");
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
			count.clear();
			bcount.clear();
			break;
		case "stop":
			round = false;
			break;
		case "count":
			Map<String,Integer> parsed = new HashMap<String,Integer>();
			for (Map.Entry<String, Integer> set : count.entrySet()) {			
				 parsed.put(set.getKey(), set.getValue()+(bcount.get(set.getKey())?1:0));
			 }
			Map<String,Integer> sorted = new TreeMap<>(parsed);
			sender.sendMessage(ChatColor.RED + ""+ ChatColor.BOLD + "## Points ##");
			sender.sendMessage("");
			for (Map.Entry<String, Integer> set : sorted.entrySet()) {			
				 sender.sendMessage(ChatColor.RED + set.getKey() + " - " + count.get(set.getKey()) 
				 +(bcount.get(set.getKey())?ChatColor.GREEN + " (+1)":ChatColor.RED + " (+0)")
				 );
			 }
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Usage: /heads [giveheads/start/stop/count]");
			break;
		}
		return true;
	}

}
