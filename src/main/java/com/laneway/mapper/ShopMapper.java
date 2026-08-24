package com.laneway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laneway.entity.Shop;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface ShopMapper extends BaseMapper<Shop> {

  Long countByShopId(@Param("shopId") Long shopId);
}
