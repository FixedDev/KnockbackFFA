package net.minebukket.knockbackpvp.commands;

import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public class VoteCommands {


    @Command(names = {"vote", "voteformap", "vfm", "votar", "votarpormapa", "vpm"}, onlyPlayer = true, usage = "Usage: /<command> <number>", max = 1, min = 1)
    public static boolean voteCommand(Player sender, CommandContext context) {
        if (!KnockbackPvpPlugin.getPlugin().getVoteMapManager().isMapVotating()) {
            sender.sendMessage(ChatColor.RED + "Ahora mismo no hay votacion de mapa, tendras que esperar hasta que sea activada!");
            return true;
        }

        int mapNumber = context.getObject(0, Integer.class);

        try {
            KnockbackPvpPlugin.getPlugin().getVoteMapManager().voteForMap(sender, mapNumber);
            sender.sendMessage(ChatColor.YELLOW + "Has votado satisfactoriamente por el mapa " + ChatColor.translateAlternateColorCodes('&', KnockbackPvpPlugin.getPlugin().getVoteMapManager().getAvailableMapsForVote().get(mapNumber - 1).getMapDisplayName()));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "No hay ningun mapa con el numero " + mapNumber + ", debes elegir un numero del 1 al 4!");
        } catch (IllegalStateException e){
            sender.sendMessage(ChatColor.RED + "Ya has votado por un mapa anteriormente!");
        }

        return true;
    }

    @Command(names = {"startvotation", "iniciarvotacion"}, onlyPlayer = true, usage = "Usage: /<command> ", max = 0, permission = "knockback.command.startvotation")
    public static boolean startVotationCommand(Player sender, CommandContext context) {
        if (KnockbackPvpPlugin.getPlugin().getVoteMapManager().isMapVotating()) {
            sender.sendMessage(ChatColor.RED + "Ya hay una votacion de mapas activa!");
            return true;
        }

        KnockbackPvpPlugin.getPlugin().getVoteMapManager().setMapVotating(true);
        org.bukkit.command.Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Votacion de mapas iniciada.");

        return true;
    }


}
