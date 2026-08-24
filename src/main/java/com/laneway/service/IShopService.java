package com.laneway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laneway.dto.Result;
import com.laneway.dto.ShopSearchDTO;
import com.laneway.entity.Shop;
import java.util.List;

/**
 * Service interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface IShopService extends IService<Shop> {

  Result queryById(Long id);

  Result update(Shop shop);

  Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

  Result searchShops(ShopSearchDTO criteria);

  Result catalog(String keyword, Long typeId, Integer page, Integer size, String sort);

  Result queryShopsByIds(List<Long> ids);

  Result listAreas();

  Result saveShop(Shop shop);

  Result queryShopByName(String name, Integer current);

  Result queryShopDetail(Long id);
}
