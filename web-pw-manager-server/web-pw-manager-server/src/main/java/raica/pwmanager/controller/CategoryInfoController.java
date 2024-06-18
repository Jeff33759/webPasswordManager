package raica.pwmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.dto.send.QueryCategoriesData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.service.category.CategoryInfoService;

/**
 * 分類相關的接口。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CategoryInfoController {

    @Autowired
    private CategoryInfoService categoryInfoService;

    @GetMapping(path = ApiConst.Path.QUERY_CATEGORY_LIST)
    public ResponseEntity<ResponseBodyTemplate<QueryCategoriesData>> queryCategories(@RequestAttribute(value = RequestAttributeFieldName.MY_REQ_CONTEXT) MyRequestContext myReqContext) {
        return categoryInfoService.queryCategories(myReqContext);
    }

}
