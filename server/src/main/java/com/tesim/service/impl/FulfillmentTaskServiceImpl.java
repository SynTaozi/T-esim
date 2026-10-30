package com.tesim.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tesim.entity.FulfillmentTask;
import com.tesim.mapper.FulfillmentTaskMapper;
import com.tesim.service.FulfillmentTaskService;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentTaskServiceImpl extends ServiceImpl<FulfillmentTaskMapper, FulfillmentTask> implements FulfillmentTaskService {
}
