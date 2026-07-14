package dev.redgamer6427a.core.utils;

import dev.redgamer6427a.core.logging.Logger;

import java.net.InetAddress;

public class CidrRange {
    private static final Logger logger = Logger.create();

    private byte[] network;
    private final int prefixLen;
    private boolean isInvalid;

    public CidrRange(String cidr) {
        String[] parts = cidr.split("/");
        try {
            network = InetAddress.getByName(parts[0]).getAddress();
            isInvalid = false;
        } catch (Exception e) {
            logger.error("Error while creating CidrRange ({}) object: " + e, cidr);
            isInvalid = true;
        }
        prefixLen = Integer.parseInt(parts[1]);
    }

    public boolean contains(InetAddress addr) {
        if (isInvalid) return false;
        byte[] b = addr.getAddress();
        int fullBytes = prefixLen / 8;
        int remBits = prefixLen % 8;
        for (int i = 0; i < fullBytes; i++) if (b[i] != network[i]) return false;
        if (remBits == 0) return true;
        int mask = 0xFF << (8 - remBits);
        return (b[fullBytes] & mask) == (network[fullBytes] & mask);
    }

}
