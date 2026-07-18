package dev.jeryn.mc.rwf.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BetaWarningData extends SavedData {

    private static final String ID = "tardis_refined_rwf_beta_warning";

    private final Set<UUID> seen = new HashSet<>();

    public static boolean markSeenIfFirstJoin(ServerPlayer player) {
        BetaWarningData data = player.getServer().overworld().getDataStorage()
                .computeIfAbsent(BetaWarningData::load, BetaWarningData::new, ID);
        boolean firstTime = data.seen.add(player.getUUID());
        if (firstTime) {
            data.setDirty();
        }
        return firstTime;
    }

    public static BetaWarningData load(CompoundTag tag) {
        BetaWarningData data = new BetaWarningData();
        ListTag list = tag.getList("seen", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.seen.add(UUID.fromString(list.getString(i)));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (UUID uuid : seen) {
            list.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("seen", list);
        return tag;
    }
}
