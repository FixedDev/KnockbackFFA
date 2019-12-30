package net.minebukket.knockbackpvp.listeners;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
@AllArgsConstructor
@Getter
public class WorldListener implements Listener {

    private final KnockbackPvpPlugin plugin;


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().isDead() || event.getPlayer().getHealth() == 0) {
            return;
        }

        if (event.getTo().getY() <= plugin.getMapManager().getCurrentMap().getLowLimit()) {
            event.getPlayer().setHealth(0);

            EntityDamageEvent damageEvent = new EntityDamageEvent(event.getPlayer(), EntityDamageEvent.DamageCause.VOID, ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, 999D), ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, (o1) -> o1));

            event.getPlayer().setLastDamageCause(damageEvent);

            Bukkit.getPluginManager().callEvent(damageEvent);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getY() >= plugin.getMapManager().getCurrentMap().getHighLimit() || event.getBlockPlaced().getY() <= plugin.getMapManager().getCurrentMap().getLowLimit()) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        if (event.getDamager().getLocation().getY() >= plugin.getMapManager().getCurrentMap().getHighLimit() || event.getEntity().getLocation().getY() >= plugin.getMapManager().getCurrentMap().getHighLimit()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player entity = (Player) event.getEntity();
            entity.setFoodLevel(20);
        } else {
            event.setFoodLevel(20);
        }
        event.setCancelled(true);
    }
}
