package fr.blockincraft.faylisia.items.armor;

import fr.blockincraft.faylisia.items.CustomItem;
import fr.blockincraft.faylisia.items.StatsItem;
import fr.blockincraft.faylisia.player.Stats;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.*;

public class ArmorItem extends CustomItem implements StatsItem {
    private static final Material[] armorMaterials = new Material[]{
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS,

            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_BOOTS,

            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS,

            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS,

            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,

            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS,

            Material.TURTLE_HELMET,

            Material.PLAYER_HEAD
    };

    private ArmorSet armorSet = null;
    private final Map<Stats, Double> stats = new HashMap<>();

    public ArmorItem(Material material, String id) throws InvalidBuildException {
        super(material, id);
    }

    @Override
    public List<String> firstLore() {
        List<String> lore = new ArrayList<>();

        List<Map.Entry<Stats, Double>> sorted = stats.entrySet().stream().sorted((o1, o2) -> o1.getKey().index - o2.getKey().index).toList();

        sorted.forEach(entry -> {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7" + entry.getKey().name + " &" + entry.getKey().color + "+" + entry.getValue()));
        });

        return lore;
    }

    @Override
    protected List<String> moreLore() {
        List<String> lore = new ArrayList<>();

        for (ArmorSet.Bonus bonus : armorSet.getBonus()) {
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&d" + bonus.getMinimum() + " Pieces bonus - " + bonus.getName() + "&d:"));
            for (String descPart : bonus.getDescription()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', descPart));
            }
        }

        return lore;
    }

    public ArmorItem setArmorSet(ArmorSet armorSet) {
        if (isRegistered()) throw new ChangeRegisteredItem();
        this.armorSet = armorSet;
        return this;
    }

    public ArmorSet getArmorSet() {
        return armorSet;
    }

    @Override
    protected void registerOthers() {
        if (!Arrays.asList(armorMaterials).contains(getMaterial())) throw new InvalidBuildException("Armor item can only be an armor material");
        if (!armorSet.isRegistered()) throw new InvalidBuildException("Armor set must be registered before armor item");
    }

    @Override
    public boolean validStats(boolean inMainHand, boolean inArmorSlot) {
        return inArmorSlot;
    }

    @Override
    public double getStat(Stats stat) {
        return stats.get(stat);
    }

    @Override
    public boolean hasStat(Stats stat) {
        return stats.get(stat) != null;
    }

    public ArmorItem removeStat(Stats stat) {
        if (isRegistered()) throw new ChangeRegisteredItem();
        stats.remove(stat);
        return this;
    }

    public ArmorItem setStat(Stats stat, double value) {
        if (isRegistered()) throw new ChangeRegisteredItem();
        stats.put(stat, value);
        return this;
    }

    @Override
    public Map<Stats, Double> getStats() {
        return new HashMap<>(stats);
    }

    @Override
    protected String getType() {
        return "PIECE D'ARMURE";
    }
}