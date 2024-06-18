package raica.pwmanager.entities.bo;

import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;

/**
 * 參考{@link org.springframework.security.core.userdetails.User}，對UserDetails客製化。
 * */
@Getter
public class MyUserDetails implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer id;

    private final String name;

    private final String email;

    private final Boolean activated;

    private final Integer mfaType;

    private final boolean enabled;

    private final boolean accountNonExpired;

    private final boolean accountNonLocked;

    private final boolean credentialsNonExpired;

    private final Set<GrantedAuthority> authorities;


    public MyUserDetails(Integer id, String name, String email, Boolean activated, Integer mfaType,
                         boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
                         Set<GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mfaType = mfaType;
        this.activated = activated;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.authorities = authorities;
    }


    @Deprecated
    @Override
    public void eraseCredentials() {
//        do nothing
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }


    /**
     * 本系統的設計，不需要在Security認證時認證密碼。
     */
    @Deprecated
    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public Set<GrantedAuthority> getAuthoritiesSet() {
        return this.authorities;
    }
}
