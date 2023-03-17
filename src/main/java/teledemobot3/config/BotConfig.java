package teledemobot3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
public class BotConfig {
    @Value("${botusername}")
    private String username;

    @Value("${bottoken}")
    private String token;

    @Value("${botowner}")
    private long owner;

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public long getOwner() {
        return owner;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }
}