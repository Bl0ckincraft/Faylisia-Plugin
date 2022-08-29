package fr.blockincraft.faylisia;

import fr.blockincraft.faylisia.core.service.CustomPlayerService;
import fr.blockincraft.faylisia.entity.CustomEntity;
import fr.blockincraft.faylisia.entity.CustomEntityType;
import fr.blockincraft.faylisia.items.CustomItem;
import fr.blockincraft.faylisia.items.armor.ArmorSet;
import fr.blockincraft.faylisia.items.management.Categories;
import fr.blockincraft.faylisia.map.Region;
import fr.blockincraft.faylisia.map.Shape;
import fr.blockincraft.faylisia.core.dto.CustomPlayerDTO;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class Registry {
    private static final CustomPlayerService customPlayerService = new CustomPlayerService();

    private final Map<UUID, CustomPlayerDTO> players = new HashMap<>();
    private final Map<String, CustomItem> itemsById = new HashMap<>();
    private final List<CustomItem> items = new ArrayList<>();
    private final Map<String, ArmorSet> armorSetsById = new HashMap<>();
    private final List<ArmorSet> armorSets = new ArrayList<>();
    private final Map<String, CustomEntityType> entityTypesById = new HashMap<>();
    private final List<CustomEntityType> entityTypes = new ArrayList<>();
    private final Map<Entity, CustomEntity> entitiesByEntity = new HashMap<>();
    private final List<CustomEntity> entities = new ArrayList<>();
    private final Map<String, Region> regionsById = new HashMap<>();
    private final List<Region> regions = new ArrayList<>();
    private Region defaultRegion = null;

    public void init() {
        for (CustomPlayerDTO customPlayer : customPlayerService.getAllCustomPlayer()) {
            Faylisia.getInstance().getLogger().log(Level.INFO, "Load player data of " + customPlayer.getPlayer());
            players.put(customPlayer.getPlayer(), customPlayer);
        }
    }

    public Map<UUID, CustomPlayerDTO> getPlayers() {
        return new HashMap<>(players);
    }

    public CustomPlayerDTO getPlayer(UUID player) {
        return players.get(player);
    }

    public CustomPlayerDTO registerPlayer(UUID player) {
        CustomPlayerDTO dto = new CustomPlayerDTO(player);

        customPlayerService.persistCustomPlayer(dto);
        players.put(player, dto);
        return getPlayer(player);
    }

    public CustomPlayerDTO getOrRegisterPlayer(UUID player) {
        return getPlayer(player) == null ? registerPlayer(player) : getPlayer(player);
    }

    public void applyModification(CustomPlayerDTO dto) {
        customPlayerService.mergeCustomPlayer(dto);
    }

    public List<CustomItem> getItems() {
        return new ArrayList<>(items);
    }

    public boolean itemIdUsed(String id) {
        return itemsById.containsKey(id);
    }

    public void registerItem(CustomItem item) {
        itemsById.put(item.getId(), item);
        items.add(item);
        Categories category = item.getCategory();
        if (category != null) {
            category.items.add(item);
        }
    }

    public CustomItem getByItemStack(ItemStack itemStack) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;

        PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();

        if (data.has(CustomItem.idKey, PersistentDataType.STRING)) {
            String id = data.get(CustomItem.idKey, PersistentDataType.STRING);
            return itemsById.get(id);
        }

        return null;
    }

    public Map<String, CustomItem> getItemsById() {
        return new HashMap<>(itemsById);
    }

    public void refreshItems(PlayerInventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            CustomItem ci = getByItemStack(itemStack);
            if (ci != null) {
                ItemStack model = ci.getAsItemStack();

                ItemMeta isMeta = itemStack.getItemMeta();
                ItemMeta ciMeta = model.getItemMeta();

                if (ciMeta.hasCustomModelData()) isMeta.setCustomModelData(ciMeta.getCustomModelData());
                isMeta.setDisplayName(ciMeta.getDisplayName());
                isMeta.setLore(ciMeta.getLore());

                itemStack.setItemMeta(isMeta);
            }
        }
    }

    public List<ArmorSet> getArmorSets() {
        return new ArrayList<>(armorSets);
    }

    public boolean armorSetIdUsed(String id) {
        return armorSetsById.containsKey(id);
    }

    public void registerArmorSet(ArmorSet armorSet) {
        armorSetsById.put(armorSet.getId(), armorSet);
        armorSets.add(armorSet);
    }

    public List<CustomEntityType> getEntityTypes() {
        return new ArrayList<>(entityTypes);
    }

    public boolean entityTypeIdUsed(String id) {
        return entityTypesById.containsKey(id);
    }

    public void registerEntityType(CustomEntityType entityType) {
        entityTypes.add(entityType);
        entityTypesById.put(entityType.getId(), entityType);
    }

    public CustomEntity getCustomEntityByEntity(Entity entity) {
        return entitiesByEntity.get(entity);
    }

    public List<CustomEntity> getEntities() {
        return new ArrayList<>(entities);
    }

    public void addEntity(CustomEntity entity) {
        entities.add(entity);
        entitiesByEntity.put(entity.getEntity(), entity);
    }

    public void removeEntity(CustomEntity entity) {
        entities.remove(entity);
        entitiesByEntity.remove(entity.getEntity());
    }

    public Map<String, ArmorSet> getArmorSetsById() {
        return new HashMap<>(armorSetsById);
    }

    public boolean regionIdUsed(String id) {
        return regionsById.containsKey(id);
    }

    public Map<String, Region> getRegionsById() {
        return regionsById;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void registerRegion(Region region) {
        regionsById.put(region.getId(), region);
        regions.add(region);
    }

    public Region getRegionAt(int x, int y, int z, World world) {
        if (world == null) return null;

        Region region = defaultRegion;
        outer: while (region.hasSubRegion()) {
            for (Region subRegion : region.getSubRegion(false)) {
                for (Shape shape : subRegion.getAreas()) {
                    if (shape.contain(x, y, z, world)) {
                        region = subRegion;
                        continue outer;
                    }
                }
            }

            break;
        }

        return region;
    }

    public Region getRegionAt(Location location) {
        return getRegionAt(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
    }

    public boolean isInRegion(Region region, int x, int y, int z, World world, boolean strict) {
        Region regionIn = getRegionAt(x, y, z, world);

        if (regionIn == region) return true;


        if (!strict) {
            List<Region> allRegion = region.getSubRegion(true);

            return allRegion.contains(regionIn);
        }

        return false;
    }

    public boolean isInRegion(Region region, Location location, boolean strict) {
        return this.isInRegion(region, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld(), strict);
    }

    public void setDefaultRegion(Region defaultRegion) {
        this.defaultRegion = defaultRegion;
    }
}