package raica.pwmanager.service.tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raica.pwmanager.consts.MyExceptionMsgTemplate;
import raica.pwmanager.dao.extension.impl.TagMappingPasswordService;
import raica.pwmanager.dao.extension.impl.TagService;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.entities.dto.receive.AddTagReqBody;
import raica.pwmanager.entities.dto.receive.EditTagReqBody;
import raica.pwmanager.entities.dto.send.AddTagData;
import raica.pwmanager.entities.dto.send.EditTagData;
import raica.pwmanager.entities.dto.send.QueryTagsData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.entities.po.Tag;
import raica.pwmanager.entities.po.TagMappingPassword;
import raica.pwmanager.exception.MyUnexpectedException;
import raica.pwmanager.util.ResponseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO 單元測試待補
 */
@Service
public class TagInfoService {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagMappingPasswordService tagMappingPasswordService;

    @Autowired
    private TagInfoConverter tagInfoConverter;

    @Autowired
    private ResponseUtil responseUtil;


    public ResponseEntity<ResponseBodyTemplate<QueryTagsData>> queryTags(MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2. 查詢該用戶的所有標籤
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("u_id", myUserDetails.getId());

        List<Tag> tagPoList = tagService.list(tagQueryWrapper);

        // 3.轉換資料
        ArrayList<QueryTagsData.TagDto> tagDtoList = new ArrayList<>();

        tagPoList.forEach(tagPo -> {
            tagDtoList.add(tagInfoConverter.tagPoToTagDto(tagPo));
        });

        // 4. 返回
        return ResponseEntity.
                ok(
                        responseUtil.generateResponseBodyTemplate(
                                new QueryTagsData(tagDtoList),
                                ""
                        )
                );
    }


    public ResponseEntity<ResponseBodyTemplate<AddTagData>> addTag(AddTagReqBody addTagReqBody, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2.新增標籤
        Tag tagToInsert = new Tag()
                .setUserId(myUserDetails.getId())
                .setName(addTagReqBody.getTagName());

        tagService.save(tagToInsert);

        // 4.返回
        return ResponseEntity
                .ok()
                .body(
                        responseUtil.generateResponseBodyTemplate(
                                tagInfoConverter.tagPoToAddTagDataDto(tagToInsert),
                                ""
                        )
                );
    }


    public ResponseEntity<ResponseBodyTemplate<EditTagData>> editTag(int tagId, EditTagReqBody editTagReqBody, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2. 組織更新語句
        UpdateWrapper<Tag> tagUpdateWrapper = new UpdateWrapper<>();
        tagUpdateWrapper.eq("tag_id", tagId);
        tagUpdateWrapper.eq("u_id", myUserDetails.getId()); //防止用戶修改別人的標籤
        tagUpdateWrapper.set("tag_name", editTagReqBody.getTagName());

        // 3. 執行更新
        boolean isOperationSuccessful = tagService.update(tagUpdateWrapper);

        if (!isOperationSuccessful) { //若DB操作失敗或者ID沒有命中任何一條紀錄
            throw new MyUnexpectedException(String.format("Tag id: %d and User id: %d does not exist in DB but it should not occurred.", tagId, myUserDetails.getId()));
        }

        // 4. 返回
        return ResponseEntity
                .ok()
                .body(
                        responseUtil.generateResponseBodyTemplate(
                                new EditTagData(tagId, editTagReqBody.getTagName()),
                                ""
                        )
                );
    }

    @Transactional
    public ResponseEntity<ResponseBodyTemplate<JsonNode>> deleteTag(int tagId, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2.刪除mapping表
        QueryWrapper<TagMappingPassword> tagMappingPasswordQueryWrapper = new QueryWrapper<>();
        tagMappingPasswordQueryWrapper.eq("tag_id", tagId);

        tagMappingPasswordService.remove(tagMappingPasswordQueryWrapper);

        // 3.刪除標籤
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("tag_id", tagId);
        tagQueryWrapper.eq("u_id", myUserDetails.getId()); //防止用戶刪除別人的標籤

        boolean isOperationSuccessful = tagService.remove(tagQueryWrapper);

        if (!isOperationSuccessful) { //若DB操作失敗或者ID沒有命中任何一條紀錄
            throw new MyUnexpectedException(String.format("Tag id: %d and User id: %d does not exist in DB but it should not occurred.", tagId, myUserDetails.getId()));
        }


        // 4.返回
        return ResponseEntity
                .noContent()
                .build();
    }


    @Mapper(componentModel = "Spring")
    interface TagInfoConverter {

        @Mappings({
                @Mapping(target = "id", source = "tagPo.id"),
                @Mapping(target = "name", source = "tagPo.name")
        })
        QueryTagsData.TagDto tagPoToTagDto(Tag tagPo);

        @Mappings({
                @Mapping(target = "tagId", source = "tagPo.id"),
                @Mapping(target = "tagName", source = "tagPo.name")
        })
        AddTagData tagPoToAddTagDataDto(Tag tagPo);
    }

}
