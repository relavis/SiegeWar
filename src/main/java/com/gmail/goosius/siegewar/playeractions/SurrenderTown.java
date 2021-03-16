package com.gmail.goosius.siegewar.playeractions;

import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.gmail.goosius.siegewar.enums.SiegeStatus;
import com.gmail.goosius.siegewar.objects.Siege;
import com.gmail.goosius.siegewar.utils.SiegeWarMoneyUtil;
import com.gmail.goosius.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.entity.Player;

/**
 * This class is responsible for processing requests to surrender towns
 *
 * @author Goosius
 */
public class SurrenderTown {

    public static void surrenderTown(Siege siege) {

		long timeUntilSurrenderConfirmation = siege.getTimeUntilSurrenderConfirmationMillis();

		if(timeUntilSurrenderConfirmation > 0) {
			//Pending surrender
			siege.setStatus(SiegeStatus.PENDING_DEFENDER_SURRENDER);
			SiegeController.saveSiege(siege);
			Messaging.sendGlobalMessage(getSurrenderMessage(siege, timeUntilSurrenderConfirmation));
		} else {
			//Immediate surrender
			SiegeWarMoneyUtil.giveWarChestToAttackingNation(siege);
			SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_SURRENDER);
			Messaging.sendGlobalMessage(getSurrenderMessage(siege, 0));
		}
    }

	public static void processSurrenderTownRequest(Player player, Siege siege) throws TownyException {
		if (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, siege.getSiegeType().getPermissionNodeToAbandonAttack().getNode()))
			throw new TownyException(Translation.of("msg_err_command_disable"));

		surrenderTown(siege);
	}


	private static String getSurrenderMessage(Siege siege, long timeUntilAbandonConfirmation) {
		String key = String.format("msg_%s_siege_town_surrender", siege.getSiegeType().toString().toLowerCase());
		String message = "";
		switch(siege.getSiegeType()) {
			case CONQUEST:
			case SUPPRESSION:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getAttacker().getFormattedName());
				break;
			case LIBERATION:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getDefender().getFormattedName(),
						siege.getAttacker().getFormattedName());
				break;
			case REVOLT:
				message = Translation.of(key,
						siege.getTown().getFormattedName(),
						siege.getDefender().getFormattedName());
				break;
		}

		if(timeUntilAbandonConfirmation == 0) {
			message += Translation.of("msg_immediate_defender_victory");
		} else {
			message += Translation.of("msg_pending_defender_victory", timeUntilAbandonConfirmation);
		}

		return message;
	}
}
