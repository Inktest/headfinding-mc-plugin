package heads.inktest;


import java.util.*;

import heads.inktest.util.SkinUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;


public class Main extends JavaPlugin implements Listener {

	
	private boolean round = false;
	private final Map<UUID,Integer>   count      = new HashMap<>();
	private final Set<UUID>           headsFound = new HashSet<>();
	private final Set<UUID>           headsPlaced = new HashSet<>();
	private final Map<Location, UUID> headsLoc   = new HashMap<>();
	
	private ItemStack headItem;

	private final String loreFlag = ChatColor.GRAY + "Hide your head as best you can from other players!";
	
	@Override
	public void onEnable() {
		headItem = new ItemStack(Material.PLAYER_HEAD);
		ItemMeta     meta = headItem.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add(loreFlag);
		meta.setLore(lore);
		headItem.setItemMeta(meta);

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		clearHeads();
	}

	public void clearHeads() {
		headsLoc.keySet().forEach(loc -> loc.getBlock().setType(Material.AIR));
		Bukkit.getOnlinePlayers().forEach(player -> clearHeads(player));
	}

	public void clearHeads(Player player) {
		PlayerInventory inv = player.getInventory();
		for (int slot = 0; slot < inv.getSize(); slot++) {
			ItemStack item = inv.getItem(slot);
			if (item != null && item.getType() == Material.PLAYER_HEAD) {
				ItemMeta meta = item.getItemMeta();
				if (meta != null && meta.hasLore() && meta.getLore().contains(loreFlag)) {
					inv.setItem(slot, null);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event) {
		if (!round) return;

		Player player = event.getPlayer();
		ItemStack item = event.getItemInHand();
		ItemMeta meta = item.getItemMeta();

		if (item.getType() == Material.PLAYER_HEAD && meta != null && meta.hasLore() && meta.getLore().contains(loreFlag)) {
			if (headsPlaced.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "You've already hidden your head this round!");
				return;
			}

			OfflinePlayer owningPlayer = ((SkullMeta) meta).getOwningPlayer();

			if (owningPlayer == null || !owningPlayer.getUniqueId().equals(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "You've may only place your own head!");
				return;
			}

			headsLoc.put(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
			headsPlaced.add(player.getUniqueId());

			player.sendMessage(ChatColor.YELLOW + "Your head has been placed, soon the sides will be swapped and you need to collect as many heads as you can!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent event) {
		if (!(round)) return;

		Block block = event.getBlock();
		if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;

		Location blockLoc = block.getLocation();

		UUID headUUID = headsLoc.get(blockLoc);
		if (headUUID == null) return;

		Player breaker = event.getPlayer();
		UUID breakerUUID = breaker.getUniqueId();
		if (breakerUUID.equals(headUUID)) {
			breaker.sendMessage(ChatColor.RED + "You cannot break your own head!");
			event.setCancelled(true);
			return;
		}
		
		count.put(breakerUUID, count.getOrDefault(breakerUUID, 0) + 1);
		headsFound.add(headUUID);
		headsLoc.remove(blockLoc);

		// Make sure head dropped has special lore line so we can remove heads later
		event.setDropItems(false);
		ItemStack headToGive = headItem.clone();
		SkullMeta meta = (SkullMeta) headToGive.getItemMeta();
		OfflinePlayer offlinePlayer = Bukkit.getPlayer(headUUID);
		if (offlinePlayer == null) offlinePlayer = Bukkit.getOfflinePlayer(headUUID);
		meta.setOwningPlayer(offlinePlayer);
		headToGive.setItemMeta(meta);
		breaker.getInventory().addItem(headToGive);

		Bukkit.broadcastMessage(ChatColor.YELLOW + event.getPlayer().getName() + ChatColor.GRAY + " has found " + ChatColor.YELLOW + SkinUtil.getName(headUUID) + "'s " + ChatColor.GRAY + "head!");
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		clearHeads(event.getPlayer());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(cmd.getName().equalsIgnoreCase("headfinding"))) return false;
		if (!(sender.hasPermission("heads.commands"))) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [giveheads/start/stop/count]");
			return true;
		}
		
		switch(args[0].toLowerCase()) {
		case "giveheads":
			
			for (Player player : Bukkit.getOnlinePlayers()) {
			    ItemStack head = headItem.clone();
			    SkullMeta meta = (SkullMeta) head.getItemMeta();
			    meta.setOwningPlayer(player);
			    head.setItemMeta(meta);

				if (!player.getInventory().contains(head)) {
					player.getInventory().addItem(head);
				}
			}
			sender.sendMessage(ChatColor.YELLOW + "Heads given to all players!");
			break;
		case "start":
			if (round) {
				sender.sendMessage(ChatColor.RED + "The round has already started!");
				return true;
			}

			round = true;
			count.clear();
			headsFound.clear();
			headsLoc.clear();
			headsPlaced.clear();
			sender.sendMessage(ChatColor.YELLOW + "Round started!");
			break;
		case "stop":
			if (!round) {
				sender.sendMessage(ChatColor.RED + "The round hasnt started yet!");
				return true;
			}

			round = false;
			clearHeads();
			headsLoc.clear();
			headsPlaced.clear();
			sender.sendMessage(ChatColor.YELLOW + "Stopped round!");
			break;
		case "count":
			Map<UUID,Integer> parsed = new HashMap<>();
			for (Map.Entry<UUID, Integer> set : count.entrySet()) {
				 parsed.put(set.getKey(), set.getValue()+(headsFound.contains(set.getKey()) ? 0 : 1));
			}

			List<Map.Entry<UUID, Integer>> sorted = new LinkedList<>(parsed.entrySet());
			Comparator<Map.Entry<UUID, Integer>> valueComparator = Map.Entry.comparingByValue();
			sorted.sort(valueComparator);

			sender.sendMessage(ChatColor.RED + ""+ ChatColor.BOLD + "## Points ##");
			sender.sendMessage("");
			for (Map.Entry<UUID, Integer> set : sorted) {
				sender.sendMessage(ChatColor.RED + SkinUtil.getName(set.getKey()) + " - " + count.get(set.getKey())
						+ (!headsFound.contains(set.getKey()) ? ChatColor.GREEN + " (+1)" : ChatColor.RED + " (+0)")
				);
			}
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [giveheads/start/stop/count]");
			break;
		}
		return true;
	}

}
