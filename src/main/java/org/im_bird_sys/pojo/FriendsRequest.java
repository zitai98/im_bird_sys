package org.im_bird_sys.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendsRequest {
    private String id;

    private String sendUserId;

    private String acceptUserId;

    private Date requestDateTime;

}