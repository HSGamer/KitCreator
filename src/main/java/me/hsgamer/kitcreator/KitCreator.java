package me.hsgamer.kitcreator;

import Commands.Kit;
import ServerControl.Loader;
import Utils.XMaterial;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitCreator extends JavaPlugin {
  private File kitFile;

  @Override
  public void onEnable() {
    getServer().getConsoleSender()
        .sendMessage(Loader.getInstance.getDataFolder().getAbsolutePath() + "/Kits.yml");
    this.kitFile = new File(Loader.getInstance.getDataFolder().getAbsolutePath() + "/Kits.yml");
    getServer().getPluginCommand("kitcreator").setExecutor(this);
    ServerControl.API.hookAddon(this);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  private List<ItemStack> getItems(Player player) {
    List<ItemStack> list = new ArrayList<>();
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null && !item.getType().equals(Material.AIR)) {
        list.add(item);
      }
    }
    return list;
  }

  private void createKit(String name, List<ItemStack> items) throws IOException {
    FileConfiguration kitConfig = Loader.kit;
    for (ItemStack item : items) {
      String material =
          XMaterial.matchXMaterial(item) != null ? XMaterial.matchXMaterial(item).name()
              : item.getType().name();
      kitConfig.set("Kits." + name + ".Items." + material + ".Amount", item.getAmount());
      if (item.hasItemMeta()) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
          kitConfig
              .set("Kits." + name + ".Items." + material + ".CustomName", meta.getDisplayName());
        }
        if (meta.hasEnchants()) {
          List<String> parsedEnchants = new ArrayList<>();
          meta.getEnchants().forEach((enchantment, integer) -> {
            parsedEnchants.add(enchantment.getName() + ":" + integer);
          });
          kitConfig.set("Kits." + name + ".Items." + material + ".Enchantments", parsedEnchants);
        }
        if (meta.hasLore()) {
          kitConfig.set("Kits." + name + ".Items." + material + ".Lore", meta.getLore());
        }
      }
    }
    kitConfig.save(kitFile);
  }

  private void deleteKit(String name) throws IOException {
    FileConfiguration kitConfig = Loader.kit;
    kitConfig.set("Kits." + name, null);
    kitConfig.save(kitFile);
  }

  private void setCooldown(String name, int value) throws IOException {
    FileConfiguration kitConfig = Loader.kit;
    kitConfig.set("Kits." + name + ".Cooldown", value);
    kitConfig.save(kitFile);
  }

  private void setPrice(String name, int value) throws IOException {
    FileConfiguration kitConfig = Loader.kit;
    kitConfig.set("Kits." + name + ".Price", value);
    kitConfig.save(kitFile);
  }

  private boolean isAvailable(String name) {
    return Kit.Kits().contains(name);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player) {
      if (args.length == 0) {
        sender.sendMessage(ChatColor.AQUA + "/kitcreator create <name>");
        sender.sendMessage(ChatColor.AQUA + "/kitcreator delete <name>");
        sender.sendMessage(ChatColor.AQUA + "/kitcreator cooldown <name> <time>");
        sender.sendMessage(ChatColor.AQUA + "/kitcreator price <name> <value>");
      } else {
        if (args[0].equalsIgnoreCase("create")) {
          if (args.length == 2) {
            if (isAvailable(args[1])) {
              sender.sendMessage(ChatColor.RED + "That kit is already available");
            } else {
              try {
                List<ItemStack> items = getItems((Player) sender);
                if (!items.isEmpty()) {
                  createKit(args[1], items);
                  setCooldown(args[1], 0);
                  sender.sendMessage(ChatColor.GREEN + "Success!");
                } else {
                  sender.sendMessage(ChatColor.RED + "You need items in your inventory to do this");
                }
              } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed! Check the console");
                getLogger().log(Level.WARNING, "Error when creating kit", e);
              }
            }
          } else {
            sender.sendMessage(ChatColor.AQUA + "/kitcreator create <name>");
          }
        } else if (args[0].equalsIgnoreCase("delete")) {
          if (args.length == 2) {
            if (!isAvailable(args[1])) {
              sender.sendMessage(ChatColor.RED + "That kit is not available");
            } else {
              try {
                deleteKit(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Success!");
              } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed! Check the console");
                getLogger().log(Level.WARNING, "Error when deleting kit", e);
              }
            }
          } else {
            sender.sendMessage(ChatColor.AQUA + "/kitcreator delete <name>");
          }
        } else if (args[0].equalsIgnoreCase("cooldown")) {
          if (args.length == 3) {
            if (!isAvailable(args[1])) {
              sender.sendMessage(ChatColor.RED + "That kit is not available");
            } else {
              try {
                setCooldown(args[1], Integer.parseInt(args[2]));
                sender.sendMessage(ChatColor.GREEN + "Success!");
              } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed! Check the console");
                getLogger().log(Level.WARNING, "Error when setting kit", e);
              } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That should be a number");
              }
            }
          } else {
            sender.sendMessage(ChatColor.AQUA + "/kitcreator cooldown <name>");
          }
        } else if (args[0].equalsIgnoreCase("price")) {
          if (args.length == 3) {
            if (!isAvailable(args[1])) {
              sender.sendMessage(ChatColor.RED + "That kit is not available");
            } else {
              try {
                setPrice(args[1], Integer.parseInt(args[2]));
                sender.sendMessage(ChatColor.GREEN + "Success!");
              } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed! Check the console");
                getLogger().log(Level.WARNING, "Error when setting kit", e);
              } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That should be a number");
              }
            }
          } else {
            sender.sendMessage(ChatColor.AQUA + "/kitcreator price <name>");
          }
        } else {
          sender.sendMessage(ChatColor.AQUA + "/kitcreator create <name>");
          sender.sendMessage(ChatColor.AQUA + "/kitcreator delete <name>");
          sender.sendMessage(ChatColor.AQUA + "/kitcreator cooldown <name> <time>");
          sender.sendMessage(ChatColor.AQUA + "/kitcreator price <name> <value>");
        }
      }
    }
    return true;
  }
}
