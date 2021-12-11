package not.hub.log4jinjectionfilter;

import io.papermc.lib.PaperLib;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

public final class Plugin extends JavaPlugin implements Listener {

    // generalized extensive pattern to also match on invisible unicode stripping attack, etc.
    private static final Pattern pattern = Pattern.compile(".*[$#].*\\{.*}.*");

    private static boolean isInjection(String message) {
        if (message == null || message.isEmpty()) return false;
        return pattern.matcher(message).matches();
    }

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (isInjection(event.getMessage())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBroadcastMessage(BroadcastMessageEvent event) {
        if (isInjection(event.getMessage())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        ItemStack item = null;
        if (event.getHand() == EquipmentSlot.HAND && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.NAME_TAG) {
            item = event.getPlayer().getInventory().getItemInMainHand();
        } else if (event.getHand() == EquipmentSlot.OFF_HAND && event.getPlayer().getInventory().getItemInOffHand().getType() == Material.NAME_TAG) {
            item = event.getPlayer().getInventory().getItemInOffHand();
        }
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) return;
        if (isInjection(item.getItemMeta().getDisplayName())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem().getItemMeta() == null) return;
        if (isInjection(event.getCurrentItem().getItemMeta().getDisplayName())) {
            event.setCurrentItem(null);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        for (String line : event.getLines()) {
            if (isInjection(line)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (event.getNewBookMeta().hasTitle() && isInjection(event.getNewBookMeta().getTitle())) {
            event.setCancelled(true);
            return;
        }
        for (String line : event.getNewBookMeta().getPages()) {
            if (isInjection(line)) {
                event.setCancelled(true);
                break;
            }
        }
    }

}
