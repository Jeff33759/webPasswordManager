package raica.pwmanager.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.dto.receive.AddPasswordReqBody;
import raica.pwmanager.entities.dto.receive.EditPasswordReqBody;
import raica.pwmanager.entities.dto.send.*;
import raica.pwmanager.service.passwordmanagement.PasswordInfoService;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
@Validated
public class PasswordInfoController {

    @Autowired
    private PasswordInfoService passwordInfoService;

    @GetMapping(path = ApiConst.Path.QUERY_PASSWORD_LIST)
    public ResponseEntity<ResponseBodyTemplate<QueryPasswordsListData>> queryPasswordsList(@RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return passwordInfoService.queryPasswordsList(myReqContext);
    }

    @GetMapping(path = ApiConst.Path.QUERY_PASSWORD_LIST_BY_TAG)
    public ResponseEntity<ResponseBodyTemplate<QueryPasswordsListData>> queryPasswordsListByTag(@PathVariable("tagId") Integer tagId, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return passwordInfoService.queryPasswordsListByTag(tagId, myReqContext);
    }

    @GetMapping(path = ApiConst.Path.QUERY_PASSWORD)
    public ResponseEntity<ResponseBodyTemplate<QueryPasswordData>> queryPassword(@PathVariable("id") Integer passwordId , @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return passwordInfoService.queryPassword(passwordId, myReqContext);
    }

    @PostMapping(path = ApiConst.Path.ADD_PASSWORD)
    public ResponseEntity<ResponseBodyTemplate<AddPasswordData>> addPassword(@Valid @RequestBody AddPasswordReqBody addPasswordReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return passwordInfoService.addPassword(addPasswordReqBody, myReqContext);
    }

    @PutMapping(path = ApiConst.Path.EDIT_PASSWORD)
    public ResponseEntity<ResponseBodyTemplate<EditPasswordData>> editPassword(@PathVariable("id") Integer passwordId, @Valid @RequestBody EditPasswordReqBody editPasswordReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return passwordInfoService.editPassword(passwordId, editPasswordReqBody, myReqContext);
    }

    @DeleteMapping(path = ApiConst.Path.DELETE_PASSWORD)
    public ResponseEntity<ResponseBodyTemplate<JsonNode>> deletePassword(@PathVariable("id") Integer passwordId, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return passwordInfoService.deletePassword(passwordId, myReqContext);
    }


}
