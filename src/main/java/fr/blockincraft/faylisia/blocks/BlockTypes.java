package fr.blockincraft.faylisia.blocks;

import fr.blockincraft.faylisia.entity.loot.Loot;
import fr.blockincraft.faylisia.items.CustomItemStack;
import fr.blockincraft.faylisia.items.Items;
import fr.blockincraft.faylisia.items.tools.ToolType;
import org.bukkit.Material;

public class BlockTypes {
    public static final BlockType WOOD = new BlockType("wood", Material.DARK_OAK_WOOD)
            .setDurability(6000)
            .setTickBeforeRespawn(200)
            .setLevel(1)
            .setToolTypes(new ToolType[]{ToolType.FORAGING})
            .setLoots(new Loot[]{
                    new Loot(1, new CustomItemStack(Items.coolDiamondBlock, 1), 1, 1, () -> 1, Loot.LootType.BLOCK)
            });

    static {
        WOOD.register();
    }
}
