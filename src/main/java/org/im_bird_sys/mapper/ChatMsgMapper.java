package org.im_bird_sys.mapper;

import org.im_bird_sys.pojo.ChatMsg;

import java.util.List;

public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    int insertSelective(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChatMsg record);

    int updateByPrimaryKey(ChatMsg record);

    List<ChatMsg> getUnReadMsgListByAcceptUid(String acceptUserId);
}