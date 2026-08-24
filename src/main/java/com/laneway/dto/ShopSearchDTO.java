package com.laneway.dto;

import lombok.Data;

@Data
public class ShopSearchDTO {
  private Long typeId;
  private String keyword;
  private String area;
  private Long maxPrice;
  /** Database score scale: pass 45 for a rating of 4.5. */
  private Integer minScore;
  /** Return only shops with currently available vouchers. */
  private Boolean hasVoucher;

  private Double x;
  private Double y;
  private Integer page = 1;
  private Integer size = 10;
  /** score, price, or distance. */
  private String sort = "score";
}
