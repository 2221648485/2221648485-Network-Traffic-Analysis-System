package com.hdu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistStore {
    @Getter
    private static final Set<String> iocServerIps = new HashSet<>(Arrays.asList("1.1.1.1", "2.2.2.2"));
    @Getter
    private static final Set<String> iocHostnames = new HashSet<>(Arrays.asList("vpn.example.com", "bad.domain.org"));

}
