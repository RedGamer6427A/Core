package dev.redgamer6427a.core.minecraft.paper.util;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PaperParameters(@NotNull Permission verbosePermission, @Nullable String AMPKey, boolean allowAMPShutDown, boolean funnyAMPVersion) {

}
