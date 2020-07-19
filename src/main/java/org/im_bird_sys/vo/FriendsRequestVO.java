package org.im_bird_sys.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendsRequestVO {

    private String sendUserId;
    private String sendUsername;
    private String sendFaceImage;
    private String sendNickname;

}