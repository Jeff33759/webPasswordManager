package raica.pwmanager.service.passwordmanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raica.pwmanager.consts.MyExceptionMsgTemplate;
import raica.pwmanager.dao.extension.impl.PasswordService;
import raica.pwmanager.dao.extension.impl.TagMappingPasswordService;
import raica.pwmanager.dao.extension.impl.TagService;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyResponseWrapper;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.entities.dto.receive.AddPasswordReqBody;
import raica.pwmanager.entities.dto.receive.EditPasswordReqBody;
import raica.pwmanager.entities.dto.send.*;
import raica.pwmanager.entities.po.Password;
import raica.pwmanager.entities.po.Tag;
import raica.pwmanager.entities.po.TagMappingPassword;
import raica.pwmanager.enums.MyHttpStatus;
import raica.pwmanager.exception.MyUnexpectedException;
import raica.pwmanager.util.AESUtil;
import raica.pwmanager.util.ResponseUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO 單元測試待補
 */
@Service
public class PasswordInfoService {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private TagMappingPasswordService tagMappingPasswordService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private AESUtil aesUtil;

    @Autowired
    private PasswordInfoService.PasswordInfoConverter passwordInfoConverter;


    public MyResponseWrapper queryPasswordsList(MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2.組織查詢語句
        QueryWrapper<Password> passwordQueryWrapper = new QueryWrapper<>();
        passwordQueryWrapper.eq("u_id", myUserDetails.getId());

        // 3.執行查詢
        List<Password> passwordPoList = passwordService.list(passwordQueryWrapper);

        // 4.轉換物件
        ArrayList<QueryPasswordsListData.PasswordDto> passwordDtoList = new ArrayList<>();
        passwordPoList.forEach(passwordPo -> {
            passwordDtoList.add(passwordInfoConverter.passwordPoToPasswordsListDtoElement(passwordPo));
        });

        // 5.返回
        ResponseBodyTemplate<QueryPasswordsListData> body = responseUtil.generateResponseBodyTemplate(
                new QueryPasswordsListData(passwordDtoList),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }

    public MyResponseWrapper queryPasswordsListByTag(int tagId, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2. 查詢映射表
        QueryWrapper<TagMappingPassword> tagMappingPasswordQueryWrapper = new QueryWrapper<>();
        tagMappingPasswordQueryWrapper.select("p_id AS passwordId");
        tagMappingPasswordQueryWrapper.eq("tag_id", tagId);

        List<TagMappingPassword> tagMappingPasswordList = tagMappingPasswordService.list(tagMappingPasswordQueryWrapper);
        List<Integer> pIdList = tagMappingPasswordList.stream().map(TagMappingPassword::getPasswordId).toList();

        if(pIdList.isEmpty()) {
            ResponseBodyTemplate<QueryPasswordsListData> body = responseUtil.generateResponseBodyTemplate(
                    new QueryPasswordsListData(new ArrayList<>()),
                    ""
            );

            return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
        }

        // 3.查詢密碼表
        QueryWrapper<Password> passwordQueryWrapper = new QueryWrapper<>();
        passwordQueryWrapper.eq("u_id", myUserDetails.getId()); //防止用戶查詢到別人的密碼
        passwordQueryWrapper.in("p_id", pIdList);

        List<Password> passwordPoList = passwordService.list(passwordQueryWrapper);

        // 4.轉換物件
        ArrayList<QueryPasswordsListData.PasswordDto> passwordDtoList = new ArrayList<>();
        passwordPoList.forEach(passwordPo -> {
            passwordDtoList.add(passwordInfoConverter.passwordPoToPasswordsListDtoElement(passwordPo));
        });

        // 5.返回
        ResponseBodyTemplate<QueryPasswordsListData> body = responseUtil.generateResponseBodyTemplate(
                new QueryPasswordsListData(passwordDtoList),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }

    @Transactional
    public MyResponseWrapper addPassword(AddPasswordReqBody addPasswordReqBody, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2. 加密 & 轉換物件
        Password passwordPo = passwordInfoConverter.addPasswordReqBodyAndMyUserDetailsToPasswordPo(addPasswordReqBody, myUserDetails, aesUtil);

        // 3. 新增密碼
        passwordService.save(passwordPo);

        // 4.標籤相關 TODO 之後可以做一個密碼匹配多個標籤
        Optional<TagMappingPassword> tagMappingPasswordPoOpt = Optional.empty();
        if (addPasswordReqBody.getTagId() != 0) {
            // 查詢會員底下的標籤ID
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.select("tag_id AS id");
            tagQueryWrapper.eq("u_id", myUserDetails.getId());

            List<Tag> tagList = tagService.list(tagQueryWrapper);
            Set<Integer> tagIdSet = tagList.stream().map(Tag::getId).collect(Collectors.toSet());

            if (!tagIdSet.contains(addPasswordReqBody.getTagId())) { //檢查標籤ID存不存在 or 是否隸屬於該用戶底下
                throw new MyUnexpectedException(String.format("tag id: %d does not belong to User id: %d but it should not occurred.", addPasswordReqBody.getTagId(), myUserDetails.getId()));
            }

            // 新增標籤映射表
            TagMappingPassword tagMappingPasswordPo = new TagMappingPassword().setPasswordId(passwordPo.getId()).setTagId(addPasswordReqBody.getTagId());

            tagMappingPasswordService.save(tagMappingPasswordPo);

            tagMappingPasswordPoOpt = Optional.of(tagMappingPasswordPo);
        }

        // 5. 返回
        ResponseBodyTemplate<AddPasswordData> body = responseUtil.generateResponseBodyTemplate(
                passwordInfoConverter.passwordPoAndTagMappingPasswordOptToAddPasswordDataDto(passwordPo, tagMappingPasswordPoOpt, aesUtil),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }

    @Transactional
    public MyResponseWrapper editPassword(int passwordId, EditPasswordReqBody editPasswordReqBody, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2.查詢密碼
        QueryWrapper<Password> passwordQueryWrapper = new QueryWrapper<>();
        passwordQueryWrapper.eq("p_id", passwordId);
        passwordQueryWrapper.eq("u_id", myUserDetails.getId()); //防止其他用戶修改別人的密碼

        Optional<Password> oldPasswordOpt = passwordService.getOneOpt(passwordQueryWrapper);
        Password passwordPo = oldPasswordOpt.orElseThrow(() -> new MyUnexpectedException(String.format("password id: %d and User id: %d does not exist in DB but it should not occurred.", passwordId, myUserDetails.getId())));

        // 3. 設置po
        passwordPo
                .setTitle(editPasswordReqBody.getTitle())
                .setWebUrl(editPasswordReqBody.getWebUrl())
                .setPassword(aesUtil.encryptForDB(editPasswordReqBody.getPassword()))
                .setDynamicEntries(editPasswordReqBody.getDynamicEntries())
                .setRemark(editPasswordReqBody.getRemark());

        // 4.更新密碼
        UpdateWrapper<Password> passwordUpdateWrapper = new UpdateWrapper<>();
        passwordUpdateWrapper
                .eq("p_id", passwordPo.getId())
                .eq("u_id", passwordPo.getUserId())
                .set("p_title", passwordPo.getTitle())
                .set("web_url", passwordPo.getWebUrl())
                .set("password", passwordPo.getPassword())
                .set("dynamic_entries", passwordPo.getDynamicEntries().toString())
                .set("remark", passwordPo.getRemark())
                .set("p_update_time", new Timestamp(Instant.now().toEpochMilli()));

        passwordService.update(passwordUpdateWrapper);

        // 5. 移除所有跟該密碼有關的標籤映射
        Optional<TagMappingPassword> tagMappingPasswordPoOpt = Optional.empty();

        QueryWrapper<TagMappingPassword> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("p_id", passwordId);
        tagMappingPasswordService.remove(objectQueryWrapper);

        // 6.如果tagId帶非0，則更新標籤映射表
        if (editPasswordReqBody.getTagId() != 0) {
            // 查詢會員底下的標籤ID
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.select("tag_id AS id");
            tagQueryWrapper.eq("u_id", myUserDetails.getId());

            List<Tag> tagList = tagService.list(tagQueryWrapper);
            Set<Integer> tagIdSet = tagList.stream().map(Tag::getId).collect(Collectors.toSet());

            if (!tagIdSet.contains(editPasswordReqBody.getTagId())) { //檢查標籤ID存不存在 or 是否隸屬於該用戶底下。TODO 之後可以做一個密碼匹配多個標籤
                throw new MyUnexpectedException(String.format("tag id: %d does not belong to User id: %d but it should not occurred.", editPasswordReqBody.getTagId(), myUserDetails.getId()));
            }

            //更新標籤映射表 TODO 之後可以做一個密碼匹配多個標籤
            TagMappingPassword tagMappingPasswordPo = new TagMappingPassword().setPasswordId(passwordId).setTagId(editPasswordReqBody.getTagId());

            tagMappingPasswordService.save(tagMappingPasswordPo);

            tagMappingPasswordPoOpt = Optional.of(tagMappingPasswordPo);
        }


        // 7.返回
        ResponseBodyTemplate<EditPasswordData> body = responseUtil.generateResponseBodyTemplate(
                passwordInfoConverter.passwordPoAndTagMappingPasswordOptToEditPasswordDataDto(passwordPo, tagMappingPasswordPoOpt, aesUtil),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }


    public MyResponseWrapper queryPassword(int passwordId, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2.查詢密碼
        QueryWrapper<Password> passwordQueryWrapper = new QueryWrapper<>();
        passwordQueryWrapper.eq("p_id", passwordId);
        passwordQueryWrapper.eq("u_id", myUserDetails.getId()); //防止用戶查詢別人的密碼

        Optional<Password> passwordOpt = passwordService.getOneOpt(passwordQueryWrapper);
        Password passwordPo = passwordOpt.orElseThrow(() -> new MyUnexpectedException(String.format("User id: %d and password id: %d does not exist in DB but it should not occurred.", myUserDetails.getId(), passwordId)));

        // 3.查詢標籤映射 TODO 之後可以做一個密碼匹配多個標籤
        QueryWrapper<TagMappingPassword> tagMappingPasswordQueryWrapper = new QueryWrapper<>();
        tagMappingPasswordQueryWrapper.eq("p_id", passwordId);

        Optional<TagMappingPassword> tagMappingPasswordOpt = tagMappingPasswordService.getOneOpt(tagMappingPasswordQueryWrapper);

        // 4.解密 & 返回
        ResponseBodyTemplate<QueryPasswordData> body = responseUtil.generateResponseBodyTemplate(
                passwordInfoConverter.passwordPoAndTagMappingPasswordOptToQueryPasswordDataDto(passwordPo, tagMappingPasswordOpt, aesUtil),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }

    @Transactional
    public MyResponseWrapper deletePassword(int passwordId, MyRequestContext myRequestContext) {
        // 1. 取出認證後的User資訊(從AccessToken解碼而來)
        MyUserDetails myUserDetails = myRequestContext.getMyUserDetailsOpt().orElseThrow(() -> new MyUnexpectedException(MyExceptionMsgTemplate.UNAUTHENTICATED_USER));

        // 2.刪除mapping表
        QueryWrapper<TagMappingPassword> tagMappingPasswordQueryWrapper = new QueryWrapper<>();
        tagMappingPasswordQueryWrapper.eq("p_id", passwordId);

        tagMappingPasswordService.remove(tagMappingPasswordQueryWrapper);

        // 3.刪除密碼
        QueryWrapper<Password> passwordQueryWrapper = new QueryWrapper<>();
        passwordQueryWrapper.eq("p_id", passwordId);
        passwordQueryWrapper.eq("u_id", myUserDetails.getId()); //防止用戶刪除別人的資料

        boolean isOperationSuccessful = passwordService.remove(passwordQueryWrapper);

        if (!isOperationSuccessful) { //若DB操作失敗或者ID沒有命中任何一條紀錄
            throw new MyUnexpectedException(String.format("Password id: %d and User id: %d does not exist in DB but it should not occurred.", passwordId, myUserDetails.getId()));
        }

        // 4.返回
        return new MyResponseWrapper(MyHttpStatus.SUCCESS_NO_CONTENT, null);
    }


    @Mapper(componentModel = "Spring")
    interface PasswordInfoConverter {

        @Mappings({
                @Mapping(target = "id", source = "passwordPo.id"),
                @Mapping(target = "title", source = "passwordPo.title"),
                @Mapping(target = "categoryId", source = "passwordPo.categoryId")
        })
        QueryPasswordsListData.PasswordDto passwordPoToPasswordsListDtoElement(Password passwordPo);


        @Mappings({
                @Mapping(target = "id", ignore = true),
                @Mapping(target = "userId", source = "myUserDetails.id"),
                @Mapping(target = "categoryId", source = "addPasswordReqBody.categoryId"),
                @Mapping(target = "title", source = "addPasswordReqBody.title"),
                @Mapping(target = "webUrl", expression = "java(addPasswordReqBody.getWebUrl() == null ? \"\" : addPasswordReqBody.getWebUrl())"),
                @Mapping(target = "password", expression = "java(aesUtil.encryptForDB(addPasswordReqBody.getPassword()))"),
                @Mapping(target = "dynamicEntries", expression = "java(addPasswordReqBody.getDynamicEntries() == null ? com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode() : addPasswordReqBody.getDynamicEntries())"),
                @Mapping(target = "remark", source = "addPasswordReqBody.remark"),
                @Mapping(target = "createTime", expression = "java(java.sql.Timestamp.from(java.time.Instant.now()))"),
                @Mapping(target = "updateTime", expression = "java(java.sql.Timestamp.from(java.time.Instant.now()))")
        })
        Password addPasswordReqBodyAndMyUserDetailsToPasswordPo(AddPasswordReqBody addPasswordReqBody, MyUserDetails myUserDetails, @Context AESUtil aesUtil);


        @Mappings({
                @Mapping(target = "id", source = "passwordPo.id"),
                @Mapping(target = "categoryId", source = "passwordPo.categoryId"),
                @Mapping(target = "title", source = "passwordPo.title"),
                @Mapping(target = "webUrl", source = "passwordPo.webUrl"),
                @Mapping(target = "password", expression = "java(aesUtil.decryptFromDB(passwordPo.getPassword()))"),
                @Mapping(target = "dynamicEntries", source = "passwordPo.dynamicEntries"),
                @Mapping(target = "remark", source = "passwordPo.remark"),
                @Mapping(target = "tagId", expression = "java(tagMappingPasswordOpt.isEmpty() == true ? 0 : tagMappingPasswordOpt.get().getTagId())")
        })
        AddPasswordData passwordPoAndTagMappingPasswordOptToAddPasswordDataDto(Password passwordPo, Optional<TagMappingPassword> tagMappingPasswordOpt, @Context AESUtil aesUtil);


        @Mappings({
                @Mapping(target = "id", source = "passwordPo.id"),
                @Mapping(target = "categoryId", source = "passwordPo.categoryId"),
                @Mapping(target = "title", source = "passwordPo.title"),
                @Mapping(target = "webUrl", source = "passwordPo.webUrl"),
                @Mapping(target = "password", expression = "java(aesUtil.decryptFromDB(passwordPo.getPassword()))"),
                @Mapping(target = "dynamicEntries", expression = "java(passwordPo.getDynamicEntries() == null ? com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode() : passwordPo.getDynamicEntries())"),
                @Mapping(target = "remark", source = "passwordPo.remark"),
                @Mapping(target = "tagId", expression = "java(tagMappingPasswordOpt.isEmpty() == true ? 0 : tagMappingPasswordOpt.get().getTagId())")
        })
        QueryPasswordData passwordPoAndTagMappingPasswordOptToQueryPasswordDataDto(Password passwordPo, Optional<TagMappingPassword> tagMappingPasswordOpt, @Context AESUtil aesUtil);


//        @Mappings({
//                @Mapping(target = "id", source = "passwordId"),
//                @Mapping(target = "userId", source = "myUserDetails.id"),
//                @Mapping(target = "title", source = "editPasswordReqBody.title"),
//                @Mapping(target = "webUrl", source = "editPasswordReqBody.webUrl"),
//                @Mapping(target = "password", expression = "java(aesUtil.encryptForDB(editPasswordReqBody.getPassword()))"),
//                @Mapping(target = "dynamicEntries", source = "editPasswordReqBody.dynamicEntries"),
//                @Mapping(target = "remark", source = "editPasswordReqBody.remark"),
//                @Mapping(target = "updateTime", expression = "java(java.sql.Timestamp.from(java.time.Instant.now()))")
//        })
//        Password editPasswordReqBodyAndMyUserDetailsToPasswordPo(int passwordId, EditPasswordReqBody editPasswordReqBody, MyUserDetails myUserDetails, @Context AESUtil aesUtil);


        @Mappings({
                @Mapping(target = "id", source = "passwordPo.id"),
                @Mapping(target = "categoryId", source = "passwordPo.categoryId"),
                @Mapping(target = "title", source = "passwordPo.title"),
                @Mapping(target = "webUrl", source = "passwordPo.webUrl"),
                @Mapping(target = "password", expression = "java(aesUtil.decryptFromDB(passwordPo.getPassword()))"),
                @Mapping(target = "dynamicEntries", source = "passwordPo.dynamicEntries"),
                @Mapping(target = "remark", source = "passwordPo.remark"),
                @Mapping(target = "tagId", expression = "java(tagMappingPasswordOpt.isEmpty() == true ? 0 : tagMappingPasswordOpt.get().getTagId())")
        })
        EditPasswordData passwordPoAndTagMappingPasswordOptToEditPasswordDataDto(Password passwordPo, Optional<TagMappingPassword> tagMappingPasswordOpt, @Context AESUtil aesUtil);
    }

}
