package raica.pwmanager.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyResponseWrapper;
import raica.pwmanager.entities.dto.receive.EditUserMainPasswordReqBody;
import raica.pwmanager.entities.dto.receive.EditUserReqBody;
import raica.pwmanager.entities.dto.send.EditUserData;
import raica.pwmanager.entities.dto.send.QueryUserData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.service.usermanagement.UserInfoService;

import javax.validation.Valid;


/**
 * 操作用戶資訊有關的接口。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping(path = ApiConst.Path.QUERY_USER)
    public MyResponseWrapper queryUser(@RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return userInfoService.queryUser(myReqContext);
    }

    @PutMapping(path = ApiConst.Path.EDIT_USER)
    public MyResponseWrapper editUser(@Valid @RequestBody EditUserReqBody editUserReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return userInfoService.editUser(editUserReqBody, myReqContext);
    }

    @PutMapping(path = ApiConst.Path.EDIT_USER_MAIN_PASSWORD)
    public MyResponseWrapper editUserMainPassword(@Valid @RequestBody EditUserMainPasswordReqBody editUserMainPasswordReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return userInfoService.editUserMainPassword(editUserMainPasswordReqBody, myReqContext);
    }

}
