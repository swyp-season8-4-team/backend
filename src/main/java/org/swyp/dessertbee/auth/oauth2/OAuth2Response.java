package org.swyp.dessertbee.auth.oauth2;

public interface OAuth2Response {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getNickname();
    String getImageUrl();
}
