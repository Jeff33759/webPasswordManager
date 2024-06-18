package raica.pwmanager.service.usermanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import raica.pwmanager.consts.MyExceptionMsgTemplate;
import raica.pwmanager.dao.extension.impl.UserService;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.entities.dto.receive.EditUserMainPasswordReqBody;
import raica.pwmanager.entities.dto.receive.EditUserReqBody;
import raica.pwmanager.entities.dto.receive.RenewAccessTokenReqBody;
import raica.pwmanager.entities.dto.send.EditUserData;
import raica.pwmanager.entities.dto.send.QueryUserData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.exception.MyUnexpectedException;
import raica.pwmanager.util.AESUtil;
import raica.pwmanager.util.JWTUtil;
import raica.pwmanager.util.ResponseUtil;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * 用戶控管相關的邏輯封裝。
 */
@Service
public class UserInfoService {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private AESUtil aesUtil;

    @Autowired
    private UserInfoService.UserInfoConverter userInfoConverter;

    /**
     * 空的data欄位，因為呼叫頻繁，故採用單例，節省系統開銷。
     */
    private final JsonNode EMPTY_EDIT_USER_PASSWORD_DATA = JsonNodeFactory.instance.objectNode();


    /**
     * 和{@link  raica.pwmanager.service.usermanagement.LoginService#renewAccessTokenByRefreshToken(RenewAccessTokenReqBody, MyRequestContext)}不同，此API會重新去DB獲取最新的User資訊，製成AccessToken。
     *
     * @return MyUnexpectedException 當查詢用戶失敗，通常不該發生。
     */
    public ResponseEntity<ResponseBodyTemplate<QueryUserData>> queryUser(MyRequestContext myRequestContext) {
        //1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        //2. 查詢DB中最新的User資訊
        Optional<User> latestUserFromDBOpt = userService.getOptById(myUserDetails.getId());
        User latestUserFromDB = latestUserFromDBOpt.orElseThrow(() -> new MyUnexpectedException(String.format("User id: %d does not exist in DB but it should not occurred.", myUserDetails.getId())));

        //3. 轉換物件
        MyUserDetails latestMyUserDetails = jwtUtil.generateMyUserDetailsByUserPo(latestUserFromDB);

        //4. 製作AccessToken & 返回
        return ResponseEntity
                .ok(
                        responseUtil.generateResponseBodyTemplate(
                                new QueryUserData(jwtUtil.generateAccessToken(latestMyUserDetails)),
                                ""
                        )
                );
    }

    /**
     * @return MyUnexpectedException 當更新用戶失敗，通常不該發生。
     */
    public ResponseEntity<ResponseBodyTemplate<EditUserData>> editUser(EditUserReqBody editUserReqBody, MyRequestContext myRequestContext) throws MyUnexpectedException {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2. 組織條件語句
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("u_id", myUserDetails.getId());
        userUpdateWrapper.set("u_name", editUserReqBody.getUserName());
        userUpdateWrapper.set("mfa_type", editUserReqBody.getMfaType());
        userUpdateWrapper.set("u_update_time", new Timestamp(java.time.Instant.now().toEpochMilli()));

        // 3.更新用戶
        boolean isOperationSuccessful = userService.update(userUpdateWrapper);

        if (!isOperationSuccessful) { //若DB操作失敗或者ID沒有命中任何一條紀錄
            throw new MyUnexpectedException("Edit user failed but it should not occurred.");
        }

        // 4. 製作更新後的MyUserDetails物件
        MyUserDetails editedMyUserDetails = userInfoConverter.oldMyUserDetailsAndEditUserReqBodyToEditedMyUserDetails(myUserDetails, editUserReqBody);

        // 5. 將更新後的授權物件設置進Context，因Filter chain或interceptor chain可能會用到
        myRequestContext.setMyUserDetailsOpt(Optional.of(editedMyUserDetails));

        // 6. 製作AccessToken & 返回
        return ResponseEntity
                .ok(
                        responseUtil.generateResponseBodyTemplate(
                                new EditUserData(jwtUtil.generateAccessToken(editedMyUserDetails)),
                                ""
                        )
                );
    }

    public ResponseEntity<ResponseBodyTemplate<JsonNode>> editUserMainPassword(EditUserMainPasswordReqBody editUserMainPasswordReqBody, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2. 查詢用戶原密碼
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("main_password");
        userQueryWrapper.eq("u_id", myUserDetails.getId());

        Optional<User> userOptOnlyHasPasswordFieldValue = userService.getOneOpt(userQueryWrapper);
        User userOnlyHasPasswordFieldValue = userOptOnlyHasPasswordFieldValue.orElseThrow(() -> new MyUnexpectedException(String.format("User id: %d does not exist in DB but it should not occurred.", myUserDetails.getId())));

        // 3. 解密原密碼 & 比對用戶原密碼
        String oldMainPasswordPlainText = aesUtil.decryptFromDB(userOnlyHasPasswordFieldValue.getMainPassword());

        if (!oldMainPasswordPlainText.equals(editUserMainPasswordReqBody.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            responseUtil.generateResponseBodyTemplate(
                                    EMPTY_EDIT_USER_PASSWORD_DATA,
                                    "原密碼輸入錯誤。"
                            )
                    );
        }

        // 4. 加密新密碼 & 更新用戶新密碼
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("u_id", myUserDetails.getId());
        userUpdateWrapper.set("main_password", aesUtil.encryptForDB(editUserMainPasswordReqBody.getNewPassword()));
        userUpdateWrapper.set("u_update_time", new Timestamp(java.time.Instant.now().toEpochMilli()));

        boolean isOperationSuccessful = userService.update(userUpdateWrapper);

        if (!isOperationSuccessful) { //若DB操作失敗或者ID沒有命中任何一條紀錄
            throw new MyUnexpectedException("Edit user mainPassword failed but it should not occurred.");
        }

        // 5. 返回
        return ResponseEntity
                .noContent()
                .build();
    }


    /**
     * 用戶業務相關的物件轉換器。
     */
    @Mapper(componentModel = "Spring")
    interface UserInfoConverter {

        @Mappings({
                @Mapping(target = "id", source = "oldMyUserDetails.id"),
                @Mapping(target = "name", source = "editUserReqBody.userName"), //更新
                @Mapping(target = "email", source = "oldMyUserDetails.email"),
                @Mapping(target = "activated", source = "oldMyUserDetails.activated"),
                @Mapping(target = "mfaType", source = "editUserReqBody.mfaType"), //更新
                @Mapping(target = "enabled", source = "oldMyUserDetails.enabled"),
                @Mapping(target = "accountNonExpired", source = "oldMyUserDetails.accountNonExpired"),
                @Mapping(target = "credentialsNonExpired", source = "oldMyUserDetails.credentialsNonExpired"),
                @Mapping(target = "accountNonLocked", source = "oldMyUserDetails.accountNonLocked"),
                @Mapping(target = "authorities", source = "oldMyUserDetails.authorities")
        })
        MyUserDetails oldMyUserDetailsAndEditUserReqBodyToEditedMyUserDetails(MyUserDetails oldMyUserDetails, EditUserReqBody editUserReqBody);

    }
}
