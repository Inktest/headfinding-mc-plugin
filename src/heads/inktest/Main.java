package heads.inktest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;


public class Main extends JavaPlugin implements Listener {

	
	public static boolean round;
	public static Map<String,Integer> count = new HashMap<String,Integer>();
	public static Map<String,Boolean> bcount = new HashMap<String,Boolean>();
	public static Map<Location, String> headsLoc = new HashMap<Location, String>();
	
	@Override
	public void onEnable( ) {
		System.out.println("Heads plugin loaded!");
		round = false;
		 Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		if (!(round)) return;
		ItemStack item = event.getItemInHand();
		if (item.getType() == Material.PLAYER_HEAD && item.getItemMeta().getLore().contains(ChatColor.GRAY + "Hide your head as best you can from other players!")) {
			headsLoc.put(event.getBlock().getLocation(), event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		if (!(round)) return;
		Block block = event.getBlock();
		Location blockLoc = block.getLocation();
		String name = headsLoc.get(blockLoc);
		System.out.println(name);
		String bname = event.getPlayer().getName();
		if (name == null) return;
		if (name == bname) {
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot break your own head!");
			event.setCancelled(true);
			return;
			}
		if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;
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
		
		headsLoc.remove(blockLoc);
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
			    List<String> lore = new ArrayList<String>();
			    lore.add(ChatColor.GRAY + "Hide your head as best you can from other players!");
			    meta.setLore(lore);
			    head.setItemMeta(meta);
			    player.getInventory().addItem(head);		    
			}
			sender.sendMessage(ChatColor.YELLOW + "Heads given to all players!");
			break;
		case "start":
			round = true;
			count.clear();
			bcount.clear();
			sender.sendMessage(ChatColor.YELLOW + "Round started!");
			break;
		case "stop":
			round = false;
			for (Map.Entry<Location, String> set : headsLoc.entrySet()) {
				set.getKey().getBlock().setType(Material.AIR);
			}
			headsLoc.clear();
			sender.sendMessage(ChatColor.YELLOW + "Stopped round!");
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
