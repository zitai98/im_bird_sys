package org.im_bird_sys.controller;

import org.apache.commons.lang3.StringUtils;
import org.im_bird_sys.bo.UserBO;
import org.im_bird_sys.enums.OperatorFriendRequestTypeEnum;
import org.im_bird_sys.enums.SearchFriendsStatusEnum;
import org.im_bird_sys.pojo.ChatMsg;
import org.im_bird_sys.pojo.FriendsRequest;
import org.im_bird_sys.pojo.User;
import org.im_bird_sys.service.UserService;
import org.im_bird_sys.utils.FastDFSClient;
import org.im_bird_sys.utils.FileUtils;
import org.im_bird_sys.utils.IWdzlJSONResult;
import org.im_bird_sys.utils.MD5Utils;
import org.im_bird_sys.vo.FriendsRequestVO;
import org.im_bird_sys.vo.MyFriendsVO;
import org.im_bird_sys.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    FastDFSClient fastDFSClient;

    @RequestMapping("/getUser")
    public String getUserById(String id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user",user);
        return "user_list";
    }

    @RequestMapping("/registerOrLogin")
    @ResponseBody
    //用户注册登录一起绑定的方法
    public IWdzlJSONResult registerOrLogin(@RequestBody User user){
        User userResult = userService.queryUserNameIsExit(user.getUsername());
        if(userResult!=null){//登录处理
            if(!userResult.getPassword().equals(MD5Utils.getPwd(user.getPassword()))){//密码错误
                return IWdzlJSONResult.errorMsg("密码不正确");
            }
        }else{//注册处理
            user.setNickname(user.getUsername());
            user.setQrcode("");
            user.setPassword(MD5Utils.getPwd(user.getPassword()));//密码MD5加密
            user.setFaceImage("");
            user.setFaceImageBig("");
            userResult = userService.insert(user);
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userResult,userVo);
        return IWdzlJSONResult.ok(userVo);
    }

    @RequestMapping("/uploadFaceBase64")
    @ResponseBody
    //用户头像上传访问方法
    public IWdzlJSONResult uploadFaceBase64(@RequestBody UserBO userBO) throws Exception {
        //获取前端传过来的base64的字符串，然后转为文件对象在进行上传
        String base64Data = userBO.getFaceData();
        //保存在本地，在上传至服务器
        String userFacePath = "/root/photos/touxiang/"+userBO.getUserId()+"userFaceBase64.png";
        //调用FileUtils 类中的方法将base64 字符串转为文件对象
        FileUtils.base64ToFile(userFacePath, base64Data);
        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
        //获取fastDFS上传图片的路径
        String url = fastDFSClient.uploadBase64(multipartFile);
        String thump = "_150x150.";
        String[] arr = url.split("\\.");
        String thumpImgUrl = arr[0]+thump+arr[1];

        //更新用户头像
        User user = new User();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        User result = userService.updateUserInfo(user);
        return  IWdzlJSONResult.ok(result);
    }

    //修改昵称方法
    @RequestMapping("/setNickname")
    @ResponseBody
    public IWdzlJSONResult setNickName(User user){
        User userResult = userService.updateUserInfo(user);
        return IWdzlJSONResult.ok(userResult);
    }

    //搜索好友的请求方法
    @RequestMapping("/searchFriend")
    @ResponseBody
    public IWdzlJSONResult searchFriend(String myUserId, String friendUserName){
        /**
         * status == 1 返回：无此用户
         * status == 2 返回：不能添加自己
         * status == 3 返回：该用户已经是你的好友
         */
        Integer status = userService.preconditionSearchFriends(myUserId,friendUserName);

        if(status == SearchFriendsStatusEnum.SUCCESS.status){
            //查询好友账号信息
            User user = userService.queryUserNameIsExit(friendUserName);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            return IWdzlJSONResult.ok(userVo);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IWdzlJSONResult.errorMsg(msg);
        }
    }

    //发送添加好友请求的方法
    @RequestMapping("/addFriendRequest")
    @ResponseBody
    public IWdzlJSONResult addFriendRequest(String myUserId, String friendUserName){
        if(StringUtils.isBlank(friendUserName)){
            return IWdzlJSONResult.errorMsg("好友信息为空");
        }
        /**
         * status == 1 返回：无此用户
         * status == 2 返回：不能添加自己
         * status == 3 返回：该用户已经是你的好友
         */
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);
        if(status == SearchFriendsStatusEnum.SUCCESS.status){
            userService.sendFriendRequest(myUserId,friendUserName);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IWdzlJSONResult.errorMsg(msg);
        }
        return IWdzlJSONResult.ok();
    }

    //好友请求列表查询
    @RequestMapping("/queryFriendRequest")
    @ResponseBody
    public IWdzlJSONResult queryFriendRequest(String userId){
        List<FriendsRequestVO> friendRequestList = userService.queryFriendRequestList(userId);
        return IWdzlJSONResult.ok(friendRequestList);
    }

    //好友请求处理映射my_friends
    @RequestMapping("/operFriendRequest")
    @ResponseBody
    public IWdzlJSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType){
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type){//拒绝请求
            //需要对好友请求表进行删除操作
            userService.deleteFriendRequest(friendsRequest);
        }else if(operType==OperatorFriendRequestTypeEnum.PASS.type){//同意请求

            //向对方的好友表插入一条新记录
            //对好友请求表进行删除操作
            userService.passFriendRequest(sendUserId,acceptUserId);
        }
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return IWdzlJSONResult.ok(myFriends);
    }

    // 好友列表查询
    @RequestMapping("/myFriends")
    @ResponseBody
    public IWdzlJSONResult myFriends(String userId){
        if (StringUtils.isBlank(userId)){
            return IWdzlJSONResult.errorMsg("用户id为空");
        }
        //数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
        return IWdzlJSONResult.ok(myFriends);
    }

    /**
     * 用户手机端获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    @RequestMapping("/getUnReadMsgList")
    @ResponseBody
    public IWdzlJSONResult getUnReadMsgList(String acceptUserId){
        if(StringUtils.isBlank(acceptUserId)){
            return IWdzlJSONResult.errorMsg("接收者ID不能为空");
        }
        //根据接收ID查找为签收的消息列表
        List<ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return IWdzlJSONResult.ok(unReadMsgList);
    }

}
