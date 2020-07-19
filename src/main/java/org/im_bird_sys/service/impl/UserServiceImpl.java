package org.im_bird_sys.service.impl;

import org.im_bird_sys.enums.MsgSignFlagEnum;
import org.im_bird_sys.enums.SearchFriendsStatusEnum;
import org.im_bird_sys.mapper.*;
import org.im_bird_sys.netty.ChatMsg;
import org.im_bird_sys.pojo.FriendsRequest;
import org.im_bird_sys.pojo.MyFriends;
import org.im_bird_sys.pojo.User;
import org.im_bird_sys.service.UserService;
import org.im_bird_sys.utils.FastDFSClient;
import org.im_bird_sys.utils.FileUtils;
import org.im_bird_sys.utils.QRCodeUtils;
import org.im_bird_sys.vo.FriendsRequestVO;
import org.im_bird_sys.vo.MyFriendsVO;
import org.im_bird_sys.vo.UserVo;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    UserMapper userMapper;

    @Autowired
    MyFriendsMapper myFriendsMapper;

    @Autowired
    FriendsRequestMapper friendsRequestMapper;

    @Autowired
    UserMapperCustom userMapperCustom;

    @Autowired
    ChatMsgMapper chatMsgMapper;

    @Autowired
    Sid sid;

    @Autowired
    QRCodeUtils qrCodeUtils;

    @Autowired
    FastDFSClient fastDFSClient;

    @Override
    public User getUserById(String id) {
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    @Override
    public User queryUserNameIsExit(String username) {
        User user = userMapper.queryUserNameIsExit(username);
        return user;
    }

    @Override
    public User insert(User user) {
        String userId = sid.nextShort();
        //为每个注册用户生成一个唯一的二维码
        //安装本地地址二维码
        String qrCodePath = "/root/photos/erweima/"+userId+"qrcode.png";
        //创建二维码对象信息
        qrCodeUtils.createQRCode(qrCodePath, "bird_qrcode:"+user.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeURL ="";
        try {
            qrCodeURL = fastDFSClient.uploadQRCode(qrcodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setId(userId);
        user.setQrcode(qrCodeURL);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User result = userMapper.selectByPrimaryKey(user.getId());
        return result;
    }

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        //搜索账号不存在返回1
        if(user==null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //搜索账号为自己账号返回2
        if(user.getId().equals(myUserId)){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //搜索账号为已经是你的好友，返回3
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(myUserId);
        myFriends.setMyFriendUserId(user.getId());
        MyFriends myf = myFriendsMapper.selectOneByExample(myFriends);
        if(myf != null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);

        FriendsRequest friendsRequest = new FriendsRequest();
        //设置申请ID
        String requestId = sid.nextShort();
        friendsRequest.setId(requestId);
        friendsRequest.setSendUserId(myUserId);
        friendsRequest.setAcceptUserId(user.getId());
        friendsRequest.setRequestDateTime(new Date());
        friendsRequestMapper.insert(friendsRequest);
    }

    @Override
    public List<FriendsRequestVO> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        //双向保存好友信息
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);

        //删除好友请求表
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        deleteFriendRequest(friendsRequest);
    }

    //通过好友请求并保存数据到my_friends 表中
    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();

        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String acceptUserId) {
        return userMapperCustom.queryMyFriends(acceptUserId);
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        org.im_bird_sys.pojo.ChatMsg msgDb = new org.im_bird_sys.pojo.ChatMsg();
        String msgId = sid.nextShort();

        msgDb.setId(msgId);
        msgDb.setAcceptUserId(chatMsg.getReceiverId());
        msgDb.setSendUserId(chatMsg.getSenderId());
        msgDb.setCreateTime(new Date());
        msgDb.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDb.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDb);

        return msgId;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Override
    public List<org.im_bird_sys.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        List<org.im_bird_sys.pojo.ChatMsg> result = chatMsgMapper.getUnReadMsgListByAcceptUid(acceptUserId);
        return result;
    }
}
