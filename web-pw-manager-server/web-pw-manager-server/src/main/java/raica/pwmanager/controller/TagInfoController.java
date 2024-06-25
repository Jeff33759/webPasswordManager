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
import raica.pwmanager.entities.dto.receive.AddTagReqBody;
import raica.pwmanager.entities.dto.receive.EditTagReqBody;
import raica.pwmanager.entities.dto.send.AddTagData;
import raica.pwmanager.entities.dto.send.EditTagData;
import raica.pwmanager.entities.dto.send.QueryTagsData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.service.tag.TagInfoService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TagInfoController {

    @Autowired
    private TagInfoService tagInfoService;

    @GetMapping(path = ApiConst.Path.QUERY_TAG_LIST)
    public MyResponseWrapper queryTags(@RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return tagInfoService.queryTags(myReqContext);
    }

    @PostMapping(path = ApiConst.Path.ADD_TAG)
    public MyResponseWrapper addTag(@Valid @RequestBody AddTagReqBody addTagReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return tagInfoService.addTag(addTagReqBody, myReqContext);
    }

    @PutMapping(path = ApiConst.Path.EDIT_TAG)
    public MyResponseWrapper editTag(@PathVariable("id") Integer tagId, @Valid @RequestBody EditTagReqBody editTagReqBody, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return tagInfoService.editTag(tagId, editTagReqBody, myReqContext);
    }

    @DeleteMapping(path = ApiConst.Path.DELETE_TAG)
    public MyResponseWrapper deleteTag(@PathVariable("id") Integer tagId, @RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return tagInfoService.deleteTag(tagId, myReqContext);
    }

}
