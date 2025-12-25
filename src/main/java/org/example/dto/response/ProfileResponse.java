package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.UserInfo;

@Data
public class ProfileResponse {
    private Long id;
    private String bio;
    private String avatarUrl;
    private String contactInfo;
    private UserInfo user;
}
