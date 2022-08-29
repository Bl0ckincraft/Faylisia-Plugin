package fr.blockincraft.faylisia.player.permission;

public class Permission {
    public static final String[] allPerms = new String[]{
            "bukkit.command.version",
            "bukkit.command.plugins",
            "bukkit.command.help",
            "bukkit.command.reload",
            "bukkit.command.timings",
            "bukkit.command.tps",
            "minecraft.command.advancement",
            "minecraft.command.ban",
            "minecraft.command.ban-ip",
            "minecraft.command.banlist",
            "minecraft.command.clear",
            "minecraft.command.debug",
            "minecraft.command.defaultgamemode",
            "minecraft.command.deop",
            "minecraft.command.difficulty",
            "minecraft.command.effect",
            "minecraft.command.enchant",
            "minecraft.command.gamemode",
            "minecraft.command.gamerule",
            "minecraft.command.give",
            "minecraft.command.help",
            "minecraft.command.kick",
            "minecraft.command.kill",
            "minecraft.command.list",
            "minecraft.command.me",
            "minecraft.command.op",
            "minecraft.command.pardon",
            "minecraft.command.pardon-ip",
            "minecraft.command.playsound",
            "minecraft.command.save-all",
            "minecraft.command.save-off",
            "minecraft.command.save-on",
            "minecraft.command.say",
            "minecraft.command.scoreboard",
            "minecraft.command.seed",
            "minecraft.command.setblock",
            "minecraft.command.fill",
            "minecraft.command.setidletimeout",
            "minecraft.command.setworldspawn",
            "minecraft.command.spawnpoint",
            "minecraft.command.spreadplayers",
            "minecraft.command.stop",
            "minecraft.command.summon",
            "minecraft.command.msg",
            "minecraft.command.tellraw",
            "minecraft.command.testfor",
            "minecraft.command.testforblock",
            "minecraft.command.time",
            "minecraft.command.toggledownfall",
            "minecraft.command.teleport",
            "minecraft.command.weather",
            "minecraft.command.whitelist",
            "minecraft.command.xp",
            "minecraft.command.data",
            "faylisia.command.break",
            "faylisia.command.ranks",
            "faylisia.command.class",
            "faylisia.command.menu",
            "faylisia.command.items",
            "faylisia.command.spawn",
            "faylisia.chat_color",
            "faylisia.chat_gradient_color",
            "faylisia.chat_hex_color",
            "faylisia.break",
            "faylisia.ranks.edit",
            "faylisia.ranks.get",
            "faylisia.class",
            "faylisia.spawn.teleport_others",
            "faylisia.spawn.teleport",
            "faylisia.menu.open",
            "faylisia.items.give",
            "faylisia.items.menu",
            "faylisia.items.recipe"
    };
    public static final String[] otherPerms = new String[]{
            "worldedit.help",
            "worldedit.reload",
            "worldedit.report",
            "worldedit.history.undo",
            "worldedit.history.undo.self",
            "worldedit.history.redo",
            "worldedit.history.redo.self",
            "worldedit.history.clear",
            "worldedit.limit",
            "worldedit.timeout",
            "worldedit.fast",
            "worldedit.perf",
            "worldedit.reorder",
            "worldedit.drawsel",
            "worldedit.world",
            "worldedit.watchdog",
            "worldedit.global-mask",
            "worldedit.searchitem",
            "worldedit.navigation.unstuck",
            "worldedit.navigation.ascend",
            "worldedit.navigation.descend",
            "worldedit.navigation.ceiling",
            "worldedit.navigation.thru.command",
            "worldedit.navigation.jumpto.command",
            "worldedit.navigation.up",
            "worldedit.selection.pos",
            "worldedit.selection.hpos",
            "worldedit.selection.chunk",
            "worldedit.wand",
            "worldedit.wand.toggle",
            "worldedit.selection.contract",
            "worldedit.selection.shift",
            "worldedit.selection.outset",
            "worldedit.selection.inset",
            "worldedit.selection.size",
            "worldedit.analysis.count",
            "worldedit.analysis.distr",
            "worldedit.selection.expand",
            "worldedit.region.set",
            "worldedit.region.line",
            "worldedit.region.curve",
            "worldedit.region.replace",
            "worldedit.region.overlay",
            "worldedit.region.center",
            "worldedit.region.naturalize",
            "worldedit.region.walls",
            "worldedit.region.faces",
            "worldedit.region.smooth",
            "worldedit.region.move",
            "worldedit.region.stack",
            "worldedit.regen",
            "worldedit.region.deform",
            "worldedit.region.hollow",
            "worldedit.region.forest",
            "worldedit.region.flora",
            "worldedit.generation.cylinder",
            "worldedit.generation.sphere",
            "worldedit.generation.forest",
            "worldedit.generation.pumpkins",
            "worldedit.generation.pyramid",
            "worldedit.generation.shape",
            "worldedit.generation.shape.biome",
            "worldedit.schematic.delete",
            "worldedit.schematic.list",
            "worldedit.clipboard.load",
            "worldedit.schematic.save",
            "worldedit.schematic.formats",
            "worldedit.schematic.load",
            "worldedit.clipboard.save",
            "worldedit.clipboard.copy",
            "worldedit.clipboard.cut",
            "worldedit.clipboard.paste",
            "worldedit.clipboard.rotate",
            "worldedit.clipboard.flip",
            "worldedit.clipboard.clear",
            "worldedit.tool.data-cycler",
            "worldedit.tool.deltree",
            "worldedit.tool.farwand",
            "worldedit.tool.flood-fill",
            "worldedit.tool.info",
            "worldedit.tool.lrbuild",
            "worldedit.setwand",
            "worldedit.tool.replacer",
            "worldedit.setwand",
            "worldedit.tool.stack",
            "worldedit.tool.tree",
            "worldedit.superpickaxe",
            "worldedit.brush.options.mask",
            "worldedit.brush.options.material",
            "worldedit.brush.options.range",
            "worldedit.brush.options.size",
            "worldedit.brush.options.tracemask",
            "worldedit.superpickaxe.area",
            "worldedit.superpickaxe.recursive",
            "worldedit.brush.apply",
            "worldedit.brush.item",
            "worldedit.brush.biome",
            "worldedit.brush.butcher",
            "worldedit.brush.clipboard",
            "worldedit.brush.cylinder",
            "worldedit.brush.deform",
            "worldedit.brush.ex",
            "worldedit.brush.forest",
            "worldedit.brush.gravity",
            "worldedit.brush.heightmap",
            "worldedit.brush.lower",
            "worldedit.brush.paint",
            "worldedit.brush.raise",
            "worldedit.brush.set",
            "worldedit.brush.smooth",
            "worldedit.brush.snow",
            "worldedit.brush.sphere",
            "worldedit.biome.list",
            "worldedit.biome.info",
            "worldedit.biome.set",
            "worldedit.chunkinfo",
            "worldedit.listchunks",
            "worldedit.delchunks",
            "worldedit.snapshots.restore",
            "worldedit.snapshots.list",
            "worldedit.fill",
            "worldedit.fill.recursive",
            "worldedit.drain",
            "worldedit.fixlava",
            "worldedit.fixwater",
            "worldedit.removeabove",
            "worldedit.removebelow",
            "worldedit.removenear",
            "worldedit.replacenear",
            "worldedit.snow",
            "worldedit.thaw",
            "worldedit.green",
            "worldedit.extinguish",
            "worldedit.butcher",
            "worldedit.remove",
            "worldedit.calc",
            "worldedit.help",
            "worldedit.navigation.jumpto.tool",
            "worldedit.navigation.thru.tool",
            "worldedit.anyblock",
            "worldedit.limit.unrestricted",
            "worldedit.timeout.unrestricted",
            "worldedit.inventory.unrestricted",
            "worldedit.override.bedrock",
            "worldedit.override.data-cycler",
            "worldedit.setnbt",
            "worldedit.report.pastebin"
    };

    private final String perm;
    private final PermissionState state;

    public Permission(String perm, PermissionState state) {
        this.perm = perm;
        this.state = state;
    }

    public String getPerm() {
        return perm;
    }

    public PermissionState getState() {
        return state;
    }
}