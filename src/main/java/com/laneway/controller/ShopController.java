package com.laneway.controller;

import com.laneway.dto.Result;
import com.laneway.dto.ShopSearchDTO;
import com.laneway.entity.Shop;
import com.laneway.service.IShopService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller.
 *
 * @author Yi Chen
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

  @Resource
  private IShopService shopService;
  /**
   * Find shop details by ID.
   *
   * @param id shop ID
   * @return shop details
   */
  @GetMapping("/{id}")
  public Result queryShopById(@PathVariable("id") Long id) {
    return shopService.queryShopDetail(id);
  }

  /** Read-only combined search; scores use the database's integer scale of ten. */
  @GetMapping("/search")
  public Result searchShops(ShopSearchDTO criteria) {
    return shopService.searchShops(criteria);
  }

  /** Cross-category shop listing for the frontend; the Agent search retains its strict filters. */
  @GetMapping("/catalog")
  public Result catalog(
      @RequestParam(value = "keyword", defaultValue = "") String keyword,
      @RequestParam(value = "typeId", required = false) Long typeId,
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "12") Integer size,
      @RequestParam(value = "sort", defaultValue = "score") String sort) {
    return shopService.catalog(keyword, typeId, page, size, sort);
  }

  /** Batch-check current candidate shops before the external Agent combines two stops. */
  @GetMapping("/batch")
  public Result queryShopsByIds(@RequestParam("ids") List<Long> ids) {
    return shopService.queryShopsByIds(ids);
  }

  /** Match actual business districts in the user's text without guessing place names. */
  @GetMapping("/areas")
  public Result listAreas() {
    return shopService.listAreas();
  }

  /**
   * Create a shop.
   *
   * @param shop shop data
   * @return shop ID
   */
  @PostMapping
  public Result saveShop(@RequestBody Shop shop) {
    return shopService.saveShop(shop);
  }

  /**
   * Update a shop.
   *
   * @param shop shop data
   * @return empty result
   */
  @PutMapping
  public Result updateShop(@RequestBody Shop shop) {
    // Persist to the database.
    return shopService.update(shop);
  }

  /**
   * Find shops by type with pagination.
   *
   * @param typeId shop type
   * @param current page number
   * @return shop list
   */
  @GetMapping("/of/type")
  public Result queryShopByType(
      @RequestParam("typeId") Integer typeId,
      @RequestParam(value = "current", defaultValue = "1") Integer current,
      @RequestParam(value = "x", required = false) Double x,
      @RequestParam(value = "y", required = false) Double y) {
    return shopService.queryShopByType(typeId, current, x, y);
  }

  /**
   * Find shops by name keyword with pagination.
   *
   * @param name shop name keyword
   * @param current page number
   * @return shop list
   */
  @GetMapping("/of/name")
  public Result queryShopByName(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "current", defaultValue = "1") Integer current) {
    return shopService.queryShopByName(name, current);
  }
}
