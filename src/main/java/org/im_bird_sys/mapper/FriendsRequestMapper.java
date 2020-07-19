package org.im_bird_sys.mapper;

import org.im_bird_sys.pojo.FriendsRequest;

public interface FriendsRequestMapper {
    int deleteByPrimaryKey(String id);

    int insert(FriendsRequest record);

    int insertSelective(FriendsRequest record);

    FriendsRequest selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FriendsRequest record);

    int updateByPrimaryKey(FriendsRequest record);

    void deleteByFriendRequest(FriendsRequest friendsRequest);
}