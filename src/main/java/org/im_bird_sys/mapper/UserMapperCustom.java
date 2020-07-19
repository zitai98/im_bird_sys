package org.im_bird_sys.mapper;



import org.im_bird_sys.vo.FriendsRequestVO;
import org.im_bird_sys.vo.MyFriendsVO;

import java.util.List;

public interface UserMapperCustom {
    List<FriendsRequestVO> queryFriendRequestList(String acceptUserId);
    List<MyFriendsVO> queryMyFriends(String userId);
    void batchUpdateMsgSigned(List<String> msgIdList);

}
