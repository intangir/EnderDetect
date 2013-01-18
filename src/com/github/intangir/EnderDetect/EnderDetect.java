package com.github.intangir.EnderDetect;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import net.coreprotect.Functions;

public class EnderDetect extends JavaPlugin implements Listener
{
    public Logger log;
    public PluginDescriptionFile pdfFile;
    
	public void onEnable()
	{
		log = this.getLogger();
		pdfFile = this.getDescription();

		Bukkit.getPluginManager().registerEvents(this, this);
		
		log.info("v" + pdfFile.getVersion() + " enabled!");
	}
	
	public void onDisable()
	{
		log.info("v" + pdfFile.getVersion() + " disabled.");
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(e.getMaterial() != Material.EYE_OF_ENDER) {
			return;
		}
		e.setCancelled(true);
		
		final Block block;
		
		if(e.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			block = e.getClickedBlock().getRelative(e.getBlockFace());
		}
		else if(e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			block = e.getClickedBlock();
		}
		else
		{
			return;
		}
		
		List<Integer> interacts = Arrays.asList(new Integer[] { 23, 54, 61, 62, 64, 69, 77, 96, 107, 117 });

		// decrement the ender eye
		final Player player = e.getPlayer();
		final ItemStack hand = player.getItemInHand();
		hand.setAmount(hand.getAmount() - 1);
		
		player.setItemInHand(hand);

		// magic
		block.getWorld().playEffect(block.getLocation(), Effect.ENDER_SIGNAL, 0);
		player.sendMessage("--------");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "[EnderDetect] Searching at X:" + block.getX() + " Y:" + block.getY() + " Z:" + block.getZ());
		
				
		Runnable runnable;
		
		if(block.getType() == Material.AIR)
		{
			runnable = new Runnable()
			{
				public void run()
				{
					try
					{
						SendToPlayer(Functions.who_removed(block, 0, 0, null, 0, false), player);
					}
					catch (Exception e)
					{
						System.err.println("Got an exception when checking block data! pi");
						e.printStackTrace();
					}
				}
			};
		}
		else if(interacts.contains(block.getTypeId()))
		{
            final Location cloc;
            final int type = block.getTypeId(); 
            
            if (block.getType() == Material.CHEST)
            {
            	Chest chest = (Chest)block.getState();
            	InventoryHolder i = chest.getInventory().getHolder();
            	if ((i instanceof DoubleChest)) {
            		cloc = ((DoubleChest)i).getLocation();
            	}
            	else {
            		cloc = chest.getLocation();
            	}
            }
            else
            {
            	cloc = block.getLocation(); 
            }
			
			runnable = new Runnable()
			{
				public void run()
				{
					try
					{
						SendToPlayer(Functions.chest_transactions(cloc, null, type, 1), player);
					}
					catch (Exception e)
					{
						System.err.println("Got an exception when checking block data! pi");
						e.printStackTrace();
					}
				}
			};
		}
		else // solid non interactive block
		{
			runnable = new Runnable()
			{
				public void run()
				{
					try
					{
						SendToPlayer(Functions.who_placed(block, 0, 0, null, 0, 1), player);
					}
					catch (Exception e)
					{
						System.err.println("Got an exception when checking block data! pi");
						e.printStackTrace();
					}
				}
			};
		}

		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	public void SendToPlayer(String blockdata, Player player)
	{
		if (blockdata.indexOf("\n") > -1) {
			for (String b : blockdata.split("\n")) {
				FilterLineToPlayer(b, player);
			}
		}
		else if (blockdata.length() > 0)
			FilterLineToPlayer(blockdata, player);
	}
	
	public void FilterLineToPlayer(String line, Player player)
	{
		if(line.contains("View older data"))
			return;
		if(line.contains("---------"))
			return;

		line = line.replace("CoreProtect", "EnderDetect");
	
		player.sendMessage(line);
	}
}

