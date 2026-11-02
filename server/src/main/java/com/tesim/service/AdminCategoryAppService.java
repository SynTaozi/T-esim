package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminCategoryRequest;
import com.tesim.dto.admin.AdminCategoryVO;
import com.tesim.dto.admin.SortOrderItem;
import com.tesim.entity.I18nProductCategory;
import com.tesim.entity.ProductCategory;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.math.BigDecimal;

public interface AdminCategoryAppService {

    List<AdminCategoryVO> listRoots();



    List<AdminCategoryVO> listEnabledRoots();



    List<AdminCategoryVO> listSubs(Long categoryId);



    List<AdminCategoryVO> listEnabledSubs(Long categoryId);



    List<AdminCategoryVO> tree();



    AdminCategoryVO get(Long id);



    AdminCategoryVO createRoot(AdminCategoryRequest req);



    AdminCategoryVO createSub(AdminCategoryRequest req);



    AdminCategoryVO update(Long id, AdminCategoryRequest req);



    void delete(Long id);



    void batchSort(List<SortOrderItem> items);


}
