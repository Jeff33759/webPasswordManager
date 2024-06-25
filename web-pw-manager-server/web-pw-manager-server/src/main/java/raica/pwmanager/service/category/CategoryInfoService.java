package raica.pwmanager.service.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raica.pwmanager.dao.extension.impl.CategoryService;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyResponseWrapper;
import raica.pwmanager.entities.dto.send.QueryCategoriesData;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.entities.po.Category;
import raica.pwmanager.enums.MyHttpStatus;
import raica.pwmanager.util.ResponseUtil;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 分類相關的邏輯。
 * TODO 單元測試待補
 */
@Service
public class CategoryInfoService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private CategoryInfoConverter categoryInfoConverter;

    private List<QueryCategoriesData.CategoryDto> cacheCategoriesList;

    /**
     * Spring Bean完成註冊後，初始化一些元件。
     */
    @PostConstruct
    void initializeAfterStartUp() {
        this.refreshCacheCategoriesListFromDB();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::refreshCacheCategoriesListFromDB, 3, 3, TimeUnit.MINUTES);
    }

    public MyResponseWrapper queryCategories(MyRequestContext myRequestContext) {
        ResponseBodyTemplate<QueryCategoriesData> body = responseUtil.generateResponseBodyTemplate(
                new QueryCategoriesData(this.cacheCategoriesList),
                ""
        );

        return new MyResponseWrapper(MyHttpStatus.SUCCESS, body);
    }


    void refreshCacheCategoriesListFromDB() {
        List<Category> categoryPoList = categoryService.list();
        List<QueryCategoriesData.CategoryDto> categoryDtoList = new ArrayList<>();

        categoryPoList.forEach(category -> {
            categoryDtoList.add(categoryInfoConverter.categoryPoToCategoryDto(category));
        });

        this.cacheCategoriesList = categoryDtoList;
    }


    @Mapper(componentModel = "Spring")
    interface CategoryInfoConverter {

        @Mappings({
                @Mapping(target = "id", source = "categoryPo.id"),
                @Mapping(target = "name", source = "categoryPo.name")
        })
        QueryCategoriesData.CategoryDto categoryPoToCategoryDto(Category categoryPo);

    }

}
