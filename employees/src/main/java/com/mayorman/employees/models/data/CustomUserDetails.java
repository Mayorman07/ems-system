package com.mayorman.employees.models.data;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
@Getter
public class CustomUserDetails extends User {

    private final String employeeId;
    private final String department;

    public CustomUserDetails(String username, String password, boolean enabled,
                             boolean accountNonExpired, boolean credentialsNonExpired,
                             boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities,
                             String employeeId, String department) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.employeeId = employeeId;
        this.department = department;
    }

}
