package ex.org.project.downloadService.auth;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AuthRasDbGapPermissionDTO {

    private String phs_id;
    private Long expiration;
    private ZonedDateTime expiration_dt;
    private String passport;
}
