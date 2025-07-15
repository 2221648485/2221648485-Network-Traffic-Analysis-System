package com.hdu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data

@AllArgsConstructor
public class BlacklistStore {
    private static final Set<String> iocServerIps = new HashSet<>(Arrays.asList("1.1.1.1", "2.2.2.2"));
    private static final Set<String> iocHostnames = new HashSet<>(Arrays.asList("vpn.example.com", "bad.domain.org"));

    public static Set<String> getIocServerIps() {
        return iocServerIps;
    }

    public static Set<String> getIocHostnames() {
        return iocHostnames;
    }

    public static void updateIps(List<String> ipList) {
        iocServerIps.clear();
        iocServerIps.addAll(ipList);
    }

    public static void updateHostnames(List<String> domainList) {
        iocHostnames.clear();
        iocHostnames.addAll(domainList);
    }
}
