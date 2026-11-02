package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminSupplierConfigRequest;
import com.tesim.dto.admin.AdminSupplierConfigVO;
import com.tesim.dto.admin.AdminSupplierMappingRequest;
import com.tesim.dto.admin.AdminSupplierMappingVO;
import com.tesim.entity.Product;
import com.tesim.entity.ProductSku;
import com.tesim.entity.SupplierChannel;
import com.tesim.entity.SupplierSkuMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface AdminSupplierAppService {

    List<AdminSupplierConfigVO> listConfigs();



    AdminSupplierConfigVO getConfig(Long id);



    AdminSupplierConfigVO createConfig(AdminSupplierConfigRequest req);



    AdminSupplierConfigVO updateConfig(Long id, AdminSupplierConfigRequest req);



    void deleteConfig(Long id);



    Map<String, Object> testConfig(Long id);



    AdminSupplierConfigVO switchEnvironment(Long id, String environment);



    List<AdminSupplierMappingVO> listMappings();



    AdminSupplierMappingVO createMapping(AdminSupplierMappingRequest req);



    AdminSupplierMappingVO updateMapping(Long id, AdminSupplierMappingRequest req);



    void deleteMapping(Long id);



    void batchDeleteMappings(List<Long> ids);



    Map<String, Object> listTerminalPlans(Long configId, String type);



    Map<String, Object> syncPreview(Long configId, String type);



    Map<String, Object> syncApply(Map<String, Object> body);



    Map<String, Object> planSyncPreview(Long configId, String type);



    Map<String, Object> planSyncApply(Map<String, Object> body);



    Map<String, Object> bulkPreview(Long configId, Map<String, ?> params);



    Map<String, Object> bulkApply(Map<String, Object> body);


}
