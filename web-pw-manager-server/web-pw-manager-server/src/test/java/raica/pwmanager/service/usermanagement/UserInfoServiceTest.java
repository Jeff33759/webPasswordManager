//package raica.pwmanager.service.usermanagement;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.fasterxml.jackson.databind.JsonNode;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import raica.pwmanager.consts.MyExceptionMsgTemplate;
//import raica.pwmanager.enums.MFAType;
//import raica.pwmanager.dao.extension.impl.UserService;
//import raica.pwmanager.entities.bo.MyRequestContext;
//import raica.pwmanager.entities.bo.MyUserDetails;
//import raica.pwmanager.entities.dto.receive.EditUserMainPasswordReqBody;
//import raica.pwmanager.entities.dto.receive.EditUserReqBody;
//import raica.pwmanager.entities.dto.send.EditUserData;
//import raica.pwmanager.entities.dto.send.QueryUserData;
//import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
//import raica.pwmanager.entities.po.User;
//import raica.pwmanager.exception.MyUnexpectedException;
//import raica.pwmanager.util.AESUtil;
//import raica.pwmanager.util.JWTUtil;
//import raica.pwmanager.util.ResponseUtil;
//
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class UserInfoServiceTest {
//
//    @Mock
//    private UserService mockUserService;
//
//    @Mock
//    private JWTUtil mockJWTUtil;
//
//    @Mock
//    private ResponseUtil mockResponseUtil;
//
//    @Mock
//    private AESUtil aesUtil;
//
//    @Mock
//    private UserInfoService.UserInfoConverter mockUserInfoConverter;
//
//    @InjectMocks
//    @Spy
//    private UserInfoService spyUserInfoService;
//
//    @Test
//    void GivenNormalWorkflow_WhenQueryUser_ThenExecuteExpectedProcessAndReturnStatusCodeIs200() {
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//        User stubLatestUserFromDB = new User();
//        MyUserDetails stubLatestMyUserDetails = new MyUserDetails(1, "stubNameBeModified", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//
//        Mockito.when(mockUserService.getOptById(stubMyUserDetails.getId())).thenReturn(Optional.of(stubLatestUserFromDB));
//        Mockito.when(mockJWTUtil.generateMyUserDetailsByUserPo(stubLatestUserFromDB)).thenReturn(stubLatestMyUserDetails);
//        Mockito.when(mockJWTUtil.generateAccessToken(stubLatestMyUserDetails)).thenReturn("stubNewAccessToken");
//        ArgumentCaptor<QueryUserData> queryUserDataArgumentCaptor = ArgumentCaptor.forClass(QueryUserData.class);
//
//        ResponseEntity<ResponseBodyTemplate<QueryUserData>> actual = spyUserInfoService.queryUser(inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
//        Mockito.verify(mockUserService, Mockito.times(1)).getOptById(stubMyUserDetails.getId());
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).generateMyUserDetailsByUserPo(stubLatestUserFromDB);
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).generateAccessToken(stubLatestMyUserDetails);
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(queryUserDataArgumentCaptor.capture(), Mockito.eq(""));
//        QueryUserData targetQueryUserData = queryUserDataArgumentCaptor.getValue();
//        Assertions.assertEquals("stubNewAccessToken", targetQueryUserData.getAccessToken());
//    }
//
//    @Test
//    void GivenUserHasNotBeenAuthenticatedBySecurityChain_WhenQueryUser_ThenThrowMyUnexpectedException() {
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID"); //沒有setMyUserDetails，代表沒有經過Security chain認證
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.queryUser(inputMyRequestContext);
//        });
//
//        Assertions.assertEquals(MyExceptionMsgTemplate.UNAUTHENTICATED_USER, actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(0)).getOptById(Mockito.any());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateMyUserDetailsByUserPo(Mockito.any());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//        Mockito.verify(mockResponseUtil, Mockito.times(0)).generateResponseBodyTemplate(Mockito.any(), Mockito.any());
//    }
//
//    @Test
//    void GivenUserIdOfMyUserDetailsDoesNotExistInDB_WhenQueryUser_ThenThrowMyUnexpectedException() {
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//
//        Mockito.when(mockUserService.getOptById(stubMyUserDetails.getId())).thenReturn(Optional.empty()); //從DB中查不到那個ID
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.queryUser(inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("User id: 1 does not exist in DB but it should not occurred.", actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(1)).getOptById(stubMyUserDetails.getId());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateMyUserDetailsByUserPo(Mockito.any());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//        Mockito.verify(mockResponseUtil, Mockito.times(0)).generateResponseBodyTemplate(Mockito.any(), Mockito.any());
//    }
//
//    @Test
//    void GivenNormalWorkflow_WhenEditUser_ThenExecuteExpectedProcessAndReturnStatusIs200() {
//        String stubOldUserName = "stubName";
//        String stubNewUserName = "stubNewName";
//        MFAType stubOldMFAType = MFAType.NONE;
//        MFAType stubNewMFAType = MFAType.EMAIL;
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, stubOldUserName, "stubEmail", true, stubOldMFAType.getTypeNum(), true, true, true, true, Collections.emptySet());
//        EditUserReqBody inputEditUserReqBody = new EditUserReqBody(stubNewUserName, stubNewMFAType.getTypeNum());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//        MyUserDetails stubEditedMyUserDetails = new MyUserDetails(stubMyUserDetails.getId(), inputEditUserReqBody.getUserName(), stubMyUserDetails.getEmail(), stubMyUserDetails.getActivated(), inputEditUserReqBody.getMfaType(), stubMyUserDetails.isEnabled(), stubMyUserDetails.isAccountNonExpired(), stubMyUserDetails.isCredentialsNonExpired(), stubMyUserDetails.isAccountNonLocked(), stubMyUserDetails.getAuthoritiesSet());
//
//        Mockito.when(mockUserService.update(Mockito.any(UpdateWrapper.class))).thenReturn(true);
//        Mockito.when(mockUserInfoConverter.oldMyUserDetailsAndEditUserReqBodyToEditedMyUserDetails(stubMyUserDetails, inputEditUserReqBody)).thenReturn(stubEditedMyUserDetails);
//        Mockito.when(mockJWTUtil.generateAccessToken(stubEditedMyUserDetails)).thenReturn("stubEditedAccessToken");
//        ArgumentCaptor<UpdateWrapper<User>> userUpdateWrapperArgumentCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
//
//        ResponseEntity<ResponseBodyTemplate<EditUserData>> actual = spyUserInfoService.editUser(inputEditUserReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
//        Mockito.verify(mockUserService, Mockito.times(1)).update(userUpdateWrapperArgumentCaptor.capture());
//        Mockito.verify(mockUserInfoConverter, Mockito.times(1)).oldMyUserDetailsAndEditUserReqBodyToEditedMyUserDetails(stubMyUserDetails, inputEditUserReqBody);
//        Mockito.verify(mockJWTUtil, Mockito.times(1)).generateAccessToken(stubEditedMyUserDetails);
//        Assertions.assertEquals(stubEditedMyUserDetails, inputMyRequestContext.getMyUserDetailsOpt().get());
//        //以下驗證更新的語句
//        UpdateWrapper<User> targetUpdateWrapper = userUpdateWrapperArgumentCaptor.getValue();
//        String condition = targetUpdateWrapper.getCustomSqlSegment();
//        Assertions.assertTrue(condition.contains("WHERE"));
//        Assertions.assertTrue(condition.contains("u_id ="));
//        String updateFieldsString = targetUpdateWrapper.getSqlSet();
//        //以下為此API不能修改的欄位
//        Assertions.assertFalse(updateFieldsString.contains("u_id"));
//        Assertions.assertFalse(updateFieldsString.contains("email"));
//        Assertions.assertFalse(updateFieldsString.contains("main_password"));
//        Assertions.assertFalse(updateFieldsString.contains("is_activated"));
//        Assertions.assertFalse(updateFieldsString.contains("u_create_time"));
//    }
//
//    @Test
//    void GivenUserHasNotBeenAuthenticatedBySecurityChain_WhenEditUser_ThenThrowMyUnexpectedException() {
//        String stubNewUserName = "stubNewName";
//        MFAType stubNewMFAType = MFAType.EMAIL;
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID"); //沒有setMyUserDetails，代表沒有經過Security chain認證
//        EditUserReqBody inputEditUserReqBody = new EditUserReqBody(stubNewUserName, stubNewMFAType.getTypeNum());
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.editUser(inputEditUserReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals(MyExceptionMsgTemplate.UNAUTHENTICATED_USER, actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(0)).update(Mockito.any());
//        Mockito.verify(mockUserInfoConverter, Mockito.times(0)).oldMyUserDetailsAndEditUserReqBodyToEditedMyUserDetails(Mockito.any(), Mockito.any());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//    }
//
//    @Test
//    void GivenUserIdOfMyUserDetailsDoesNotExistInDB_WhenEditUser_ThenThrowMyUnexpectedException() {
//        String stubOldUserName = "stubName";
//        String stubNewUserName = "stubNewName";
//        MFAType stubOldMFAType = MFAType.NONE;
//        MFAType stubNewMFAType = MFAType.EMAIL;
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, stubOldUserName, "stubEmail", true, stubOldMFAType.getTypeNum(), true, true, true, true, Collections.emptySet());
//        EditUserReqBody inputEditUserReqBody = new EditUserReqBody(stubNewUserName, stubNewMFAType.getTypeNum());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//
//        Mockito.when(mockUserService.update(Mockito.any(UpdateWrapper.class))).thenReturn(false); //DB中沒有匹配的ID或者其他因素執行失敗
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.editUser(inputEditUserReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("Edit user failed but it should not occurred.", actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(1)).update(Mockito.any());
//        Mockito.verify(mockUserInfoConverter, Mockito.times(0)).oldMyUserDetailsAndEditUserReqBodyToEditedMyUserDetails(Mockito.any(), Mockito.any());
//        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateAccessToken(Mockito.any());
//    }
//
//    @Test
//    void GivenNormalWorkflow_WhenEditUserMainPassword_ThenExecutedExpectedProcessAndReturnStatusIs204() {
//        String stubOldPasswordFromFrontend = "stubOldPassword";
//        String stubOldPasswordPlainTextInDB = "stubOldPassword";
//        String stubNewPasswordPlainText = "stubOldPasswordFromFrontend";
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//        EditUserMainPasswordReqBody inputEditUserMainPasswordReqBody = new EditUserMainPasswordReqBody(stubOldPasswordFromFrontend, stubNewPasswordPlainText);
//        User stubUserInDB = new User().setMainPassword("stubMainPasswordCipherTextInDB");
//
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(Optional.of(stubUserInDB));
//        Mockito.when(aesUtil.decryptFromDB(stubUserInDB.getMainPassword())).thenReturn(stubOldPasswordPlainTextInDB);
//        Mockito.when(mockUserService.update(Mockito.any(UpdateWrapper.class))).thenReturn(true);
//        ArgumentCaptor<QueryWrapper> queryWrapperArgumentCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
//        ArgumentCaptor<UpdateWrapper> updateWrapperArgumentCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
//
//        ResponseEntity<ResponseBodyTemplate<JsonNode>> actual = spyUserInfoService.editUserMainPassword(inputEditUserMainPasswordReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
//        Mockito.verify(mockUserService, Mockito.times(1)).getOneOpt(queryWrapperArgumentCaptor.capture());
//        Mockito.verify(aesUtil, Mockito.times(1)).decryptFromDB(stubUserInDB.getMainPassword());
//        Mockito.verify(mockUserService, Mockito.times(1)).update(updateWrapperArgumentCaptor.capture());
//        Mockito.verify(aesUtil, Mockito.times(1)).encryptForDB(inputEditUserMainPasswordReqBody.getNewPassword());
//        //以下驗證查詢查詢語句
//        QueryWrapper<User> targetQueryWrapper = queryWrapperArgumentCaptor.getValue();
//        String querySelectCols = targetQueryWrapper.getSqlSelect();
//        String queryCondition = targetQueryWrapper.getCustomSqlSegment();
//        Assertions.assertEquals("main_password", querySelectCols);
//        Assertions.assertTrue(queryCondition.contains("WHERE"));
//        Assertions.assertTrue(queryCondition.contains("u_id ="));
//        //以下驗證更新語句
//        UpdateWrapper<User> targetUpdateWrapper = updateWrapperArgumentCaptor.getValue();
//        String condition = targetUpdateWrapper.getCustomSqlSegment();
//        Assertions.assertTrue(condition.contains("WHERE"));
//        Assertions.assertTrue(condition.contains("u_id ="));
//        String updateFieldsString = targetUpdateWrapper.getSqlSet();
//        Assertions.assertTrue(updateFieldsString.contains("main_password"));
//        Assertions.assertTrue(updateFieldsString.contains("u_update_time"));
//    }
//
//
//    @Test
//    void GivenUserHasNotBeenAuthenticatedBySecurityChain_WhenEditUserMainPassword_ThenThrowMyUnexpectedException() {
//        String stubOldPasswordFromFrontend = "stubOldPassword";
//        String stubNewPasswordPlainText = "stubOldPasswordFromFrontend";
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID"); //沒有setMyUserDetails，代表沒有經過Security chain認證
//        EditUserMainPasswordReqBody inputEditUserMainPasswordReqBody = new EditUserMainPasswordReqBody(stubOldPasswordFromFrontend, stubNewPasswordPlainText);
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.editUserMainPassword(inputEditUserMainPasswordReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals(MyExceptionMsgTemplate.UNAUTHENTICATED_USER, actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(0)).getOneOpt(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(0)).decryptFromDB(Mockito.any());
//        Mockito.verify(mockUserService, Mockito.times(0)).update(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(0)).encryptForDB(Mockito.any());
//    }
//
//
//    @Test
//    void GivenUserIdOfMyUserDetailsDoesNotExistInDB_WhenEditUserMainPassword_ThenThrowMyUnexpectedException() {
//        String stubOldPasswordFromFrontend = "stubOldPassword";
//        String stubNewPasswordPlainText = "stubOldPasswordFromFrontend";
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//        EditUserMainPasswordReqBody inputEditUserMainPasswordReqBody = new EditUserMainPasswordReqBody(stubOldPasswordFromFrontend, stubNewPasswordPlainText);
//
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(Optional.empty()); //用戶ID不存在於DB
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.editUserMainPassword(inputEditUserMainPasswordReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("User id: 1 does not exist in DB but it should not occurred.", actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(1)).getOneOpt(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(0)).decryptFromDB(Mockito.any());
//        Mockito.verify(mockUserService, Mockito.times(0)).update(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(0)).encryptForDB(Mockito.any());
//    }
//
//    @Test
//    void GivenUserMainPasswordInDBIsDifferentFromFrontendParam_WhenEditUserMainPassword_ThenExecuteExpectedProcessAndReturnStatusIs400() {
//        String stubOldPasswordFromFrontend = "stubOldPasswordIsDifferentFromDB"; //前端傳的原密碼不同於DB的原密碼
//        String stubOldPasswordPlainTextInDB = "stubOldPassword";
//        String stubNewPasswordPlainText = "stubOldPasswordFromFrontend";
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//        EditUserMainPasswordReqBody inputEditUserMainPasswordReqBody = new EditUserMainPasswordReqBody(stubOldPasswordFromFrontend, stubNewPasswordPlainText);
//        User stubUserInDB = new User().setMainPassword("stubMainPasswordCipherTextInDB");
//
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(Optional.of(stubUserInDB));
//        Mockito.when(aesUtil.decryptFromDB(stubUserInDB.getMainPassword())).thenReturn(stubOldPasswordPlainTextInDB);
//        ArgumentCaptor<JsonNode> editUserMainPasswordDataArgumentCaptor = ArgumentCaptor.forClass(JsonNode.class);
//
//        ResponseEntity<ResponseBodyTemplate<JsonNode>> actual = spyUserInfoService.editUserMainPassword(inputEditUserMainPasswordReqBody, inputMyRequestContext);
//
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
//        Mockito.verify(mockUserService, Mockito.times(1)).getOneOpt(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(1)).decryptFromDB(stubUserInDB.getMainPassword());
//        Mockito.verify(mockResponseUtil, Mockito.times(1)).generateResponseBodyTemplate(editUserMainPasswordDataArgumentCaptor.capture(), Mockito.eq("原密碼輸入錯誤。"));
//        Mockito.verify(mockUserService, Mockito.times(0)).update(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(0)).encryptForDB(Mockito.any());
//    }
//
//    @Test
//    void GivenUpdateFailed_WhenEditUserMainPassword_ThenThrowMyUnexpectedException() {
//        String stubOldPasswordFromFrontend = "stubOldPassword";
//        String stubOldPasswordPlainTextInDB = "stubOldPassword";
//        String stubNewPasswordPlainText = "stubOldPasswordFromFrontend";
//        MyUserDetails stubMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
//        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID").setMyUserDetailsOpt(Optional.of(stubMyUserDetails));
//        EditUserMainPasswordReqBody inputEditUserMainPasswordReqBody = new EditUserMainPasswordReqBody(stubOldPasswordFromFrontend, stubNewPasswordPlainText);
//        User stubUserInDB = new User().setMainPassword("stubMainPasswordCipherTextInDB");
//
//        Mockito.when(mockUserService.getOneOpt(Mockito.any(QueryWrapper.class))).thenReturn(Optional.of(stubUserInDB));
//        Mockito.when(aesUtil.decryptFromDB(stubUserInDB.getMainPassword())).thenReturn(stubOldPasswordPlainTextInDB);
//        Mockito.when(mockUserService.update(Mockito.any(UpdateWrapper.class))).thenReturn(false); //更新失敗
//
//        MyUnexpectedException actual = assertThrows(MyUnexpectedException.class, () -> {
//            spyUserInfoService.editUserMainPassword(inputEditUserMainPasswordReqBody, inputMyRequestContext);
//        });
//
//        Assertions.assertEquals("Edit user mainPassword failed but it should not occurred.", actual.getMessage());
//        Mockito.verify(mockUserService, Mockito.times(1)).getOneOpt(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(1)).decryptFromDB(Mockito.any());
//        Mockito.verify(mockUserService, Mockito.times(1)).update(Mockito.any());
//        Mockito.verify(aesUtil, Mockito.times(1)).encryptForDB(Mockito.any());
//    }
//
//}