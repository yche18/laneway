package com.laneway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laneway.entity.SeckillPublication;
import com.laneway.entity.SeckillVoucher;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper for flash-sale vouchers, which have a one-to-one relationship to vouchers.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

  List<Map<String, Object>> selectPublicationStates();

  int markPublished(@Param("voucherId") Long voucherId);

  SeckillPublication selectPublicationDetails(@Param("voucherId") Long voucherId);

  Integer selectPublishedFlag(@Param("voucherId") Long voucherId);

  List<Map<String, Object>> selectInventory(@Param("voucherId") Long voucherId);

  List<Long> selectActivityVoucherIds();

  int insertActivity(@Param("voucherId") Long voucherId, @Param("totalStock") Integer totalStock);
}
