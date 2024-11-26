package io.strac.gdrive.gdrive.clients;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveClientFactory {
    private final OAuth2AuthorizedClientService clientService;

    @Autowired
    public GoogleDriveClientFactory(@NonNull final OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * @implNote Investigate using singleton on client side. Needs error handling.
     * @return Instance of Drive Client
     */
    public Drive createClient() {
        OAuth2AuthenticationToken oauth2User = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(oauth2User.getAuthorizedClientRegistrationId(), oauth2User.getName());
        HttpRequestInitializer requestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + client.getAccessToken().getTokenValue());
        };
        return new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName("Google Drive Stack API")
                .build();
    }

}
