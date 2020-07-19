package org.im_bird_sys.service;


import org.im_bird_sys.netty.ChatMsg;
import org.im_bird_sys.pojo.FriendsRequest;
import org.im_bird_sys.pojo.User;
import org.im_bird_sys.vo.FriendsRequestVO;
import org.im_bird_sys.vo.MyFriendsVO;

import java.util.List;


public interface UserService {
    //根据用户名查找指定用户对象
    User getUserById(String id);

    //根据username查询该用户是否存在
    User queryUserNameIsExit(String username);

    //保存用户信息
    User insert(User user);

    //修改用户
    User updateUserInfo(User user);

    //搜索好友的前置条件接口
    Integer preconditionSearchFriends(String myUserId, String friendUserName);

    //发送好友请求
    void sendFriendRequest(String myUserId, String friendUserName);

    //好友请求列表查询
    List<FriendsRequestVO> queryFriendRequestList(String userId);

    //对好友请求表进行删除
    void deleteFriendRequest(FriendsRequest friendsRequest);

    //处理好友请求
    void passFriendRequest(String sendUserId, String acceptUserId);

    //获取好友列表
    List<MyFriendsVO> queryMyFriends(String acceptUserId);

    //保存用户聊天消息
    String saveMsg(ChatMsg chatMsg);

    void updateMsgSigned(List<String> msgIdList);

    List<org.im_bird_sys.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
