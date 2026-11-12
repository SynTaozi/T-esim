package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminProductRequest;
import com.tesim.dto.admin.AdminProductVO;
import com.tesim.dto.admin.AdminSkuRequest;
import com.tesim.dto.admin.AdminSkuVO;
import com.tesim.dto.admin.IdListRequest;
import com.tesim.dto.admin.SortOrderItem;
import com.tesim.entity.I18nProduct;
import com.tesim.entity.I18nProductCategory;
import com.tesim.entity.I18nProductSku;
import com.tesim.entity.Product;
import com.tesim.entity.ProductCoverage;
import com.tesim.entity.ProductSku;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

public interface AdminProductAppService {

    PageResult<AdminProductVO> page(Integer page, Integer size, String name, Integer status,
                                    Integer itemType, Integer isPhysicalCard, Long subCategoryId,
                                    Long categoryId, String keyword);

    AdminProductVO get(Long id);



    AdminProductVO create(AdminProductRequest req);



    AdminProductVO update(Long id, AdminProductRequest req);



    void delete(Long id);



    void batchDelete(IdListRequest req);



    void patchStatus(Long id, Integer status);



    void batchStatus(IdListRequest req);



    void batchSort(List<SortOrderItem> items);



    void batchUpdate(List<AdminProductRequest> items);



    List<AdminSkuVO> listSkus(Long productId);



    AdminSkuVO getSku(Long productId, Long skuId);



    AdminSkuVO createSku(Long productId, AdminSkuRequest req);



    AdminSkuVO updateSku(Long productId, Long skuId, AdminSkuRequest req);



    void deleteSku(Long productId, Long skuId);



    List<AdminSkuVO> batchSaveSkus(Long productId, List<AdminSkuRequest> reqs);


}
