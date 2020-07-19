package org.im_bird_sys.pojo;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;

    private String username;

    private String password;

    private String faceImage;

    private String faceImageBig;

    private String nickname;

    private String qrcode;

    private String cid;

}