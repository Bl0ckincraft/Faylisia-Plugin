package fr.blockincraft.faylisia.displays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.blockincraft.faylisia.Faylisia;
import fr.blockincraft.faylisia.Registry;
import fr.blockincraft.faylisia.core.dto.CustomPlayerDTO;
import fr.blockincraft.faylisia.player.Stats;
import fr.blockincraft.faylisia.utils.ColorsUtils;
import fr.blockincraft.faylisia.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionBar {
    private static final ProtocolManager protocolManager = Faylisia.getInstance().getProtocolManager();
    private static final Registry registry = Faylisia.getInstance().getRegistry();

    private static final String format = "&grad(%health%/%max_health% #F934A1 #E23746) &f%health_icon%      &grad(%defense% #5eff00 #00e038) &f%defense_icon%      &grad(%magical_reserve%/%max_magical_reserve% #B812F7 #9A1BFB) &f%magical_reserve_icon%";

    /**
     * Display action bar to a player
     * @param player targeted player
     */
    public static void display(@NotNull Player player) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SET_ACTION_BAR_TEXT);

        CustomPlayerDTO customPlayer = registry.getOrRegisterPlayer(player.getUniqueId());

        WrappedChatComponent chat =  WrappedChatComponent.fromLegacyText(ColorsUtils.translateAll(format
                .replace("%health_color%", Stats.HEALTH.color)
                .replace("%health%", TextUtils.valueWithCommas(customPlayer.getHealth()))
                .replace("%max_health%", TextUtils.valueWithCommas((long) Math.floor(customPlayer.getStat(Stats.HEALTH))))
                .replace("%health_icon%", String.valueOf(Stats.HEALTH.bigIcon))
                .replace("%defense_color%", Stats.DEFENSE.color)
                .replace("%defense%", customPlayer.getStat(Stats.DEFENSE) < 10.0 ? "\\_" + TextUtils.valueWithCommas((long) customPlayer.getStat(Stats.DEFENSE)) : TextUtils.valueWithCommas((long) customPlayer.getStat(Stats.DEFENSE)))
                .replace("%defense_icon%", String.valueOf(Stats.DEFENSE.bigIcon))
                .replace("%magical_reserve_color%", Stats.MAGICAL_RESERVE.color)
                .replace("%magical_reserve%", TextUtils.valueWithCommas(customPlayer.getMagicalReserve()))
                .replace("%max_magical_reserve%", TextUtils.valueWithCommas((long) Math.floor(customPlayer.getStat(Stats.MAGICAL_RESERVE))))
                .replace("%magical_reserve_icon%", String.valueOf(Stats.MAGICAL_RESERVE.bigIcon))
        ));

        packet.getChatComponents().write(0, chat);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
