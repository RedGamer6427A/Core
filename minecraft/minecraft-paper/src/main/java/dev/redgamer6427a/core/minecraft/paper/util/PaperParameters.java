package dev.redgamer6427a.admiral.paper.util;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AdmiralParameters (@NotNull Permission verbosePermission, @Nullable String AMPKey, boolean allowAMPShutDown, boolean funnyAMPVersion) {

}
