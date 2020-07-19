package org.im_bird_sys.netty;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataContent implements Serializable {
    private Integer action;//动作类型
    private ChatMsg chatMsg;//用户的聊天内容
    private String extand;//扩展字段

}
