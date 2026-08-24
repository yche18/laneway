package com.laneway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laneway.dto.Result;
import com.laneway.entity.ShopType;
import com.laneway.mapper.ShopTypeMapper;
import com.laneway.service.IShopTypeService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service implementation.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType>
    implements IShopTypeService {

  @Override
  public Result queryTypeList() {
    List<ShopType> typeList = query().orderByAsc("sort").list();
    return Result.ok(typeList);
  }
}
