package net.saturn.dimensionDisable;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DimensionListener implements Listener {

    private final DimensionDisable plugin;

    public DimensionListener(DimensionDisable plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles all player-initiated portal teleports, including:
     * - Walking into a nether/end portal
     * - Ender pearl throws that pass through portals
     * - Boats/vehicles carrying a player through a portal (fires as player event too)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (isNetherCause(cause) && plugin.isNetherDisabled()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cThe Nether is disabled on this server.");
            return;
        }

        if (isEndCause(cause) && plugin.isEndDisabled()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cThe End is disabled on this server.");
        }
    }

    /**
     * Handles non-player entities going through portals:
     * - Boats, minecarts, and other vehicles (even without passengers)
     * - Mobs
     * - Items/projectiles (ender pearls thrown into portals without a player riding)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        World.Environment targetEnv = getTargetEnvironment(event);

        if (targetEnv == World.Environment.NETHER && plugin.isNetherDisabled()) {
            event.setCancelled(true);
            return;
        }

        if (targetEnv == World.Environment.THE_END && plugin.isEndDisabled()) {
            event.setCancelled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isNetherCause(PlayerTeleportEvent.TeleportCause cause) {
        return cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;
    }

    private boolean isEndCause(PlayerTeleportEvent.TeleportCause cause) {
        return cause == PlayerTeleportEvent.TeleportCause.END_PORTAL
                || cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY;
    }

    /**
     * Determines the target environment for an EntityPortalEvent.
     * If a destination world is already set we use its environment;
     * otherwise we infer from where the entity currently is (going to the
     * opposite dimension is the vanilla behaviour).
     */
    private World.Environment getTargetEnvironment(EntityPortalEvent event) {
        if (event.getTo() != null) {
            return event.getTo().getWorld().getEnvironment();
        }

        // Fallback: infer from current world
        World.Environment current = event.getEntity().getWorld().getEnvironment();
        return switch (current) {
            case NORMAL -> World.Environment.NETHER;   // overworld → nether
            case NETHER -> World.Environment.NORMAL;   // nether → overworld (safe to allow)
            case THE_END -> World.Environment.NORMAL;  // end → overworld (safe to allow)
            default -> current;
        };
    }
}