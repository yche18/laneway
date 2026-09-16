package com.laneway;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.laneway.dto.Result;
import com.laneway.dto.ShopSearchDTO;
import com.laneway.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;

class ShopSearchBoundaryTest {
  private final ShopServiceImpl service = new ShopServiceImpl();

  private ShopSearchDTO valid() {
    ShopSearchDTO criteria = new ShopSearchDTO();
    criteria.setTypeId(1L);
    criteria.setPage(1);
    criteria.setSize(10);
    return criteria;
  }

  @Test
  void rejectsInvalidSearchBeforeDatabaseAccess() {
    ShopSearchDTO criteria = valid();
    criteria.setSize(21);
    assertFalse(service.searchShops(criteria).getSuccess());

    criteria = valid();
    criteria.setMinScore(51);
    assertFalse(service.searchShops(criteria).getSuccess());

    criteria = valid();
    criteria.setX(120.0);
    assertFalse(service.searchShops(criteria).getSuccess());

    criteria = valid();
    criteria.setSort("distance");
    Result result = service.searchShops(criteria);
    assertFalse(result.getSuccess());
    assertTrue(result.getErrorMsg().contains("x 和 y"));
  }
}
