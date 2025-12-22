package org.swyp.dessertbee.common.context;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.user.entity.UserEntity;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {

    private UserEntity cachedUser;
    private boolean loaded = false;

    public UserEntity getUser() {
        return cachedUser;
    }

    public void setUser(UserEntity user) {
        this.cachedUser = user;
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
