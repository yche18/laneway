package com.laneway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laneway.entity.Follow;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface FollowMapper extends BaseMapper<Follow> {

  int insertEdge(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

  int deleteEdge(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

  Long countEdge(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

  List<Long> selectCommonFollowIds(
      @Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

  List<Map<String, Object>> selectFanBatch(
      @Param("authorId") Long authorId,
      @Param("cursorId") Long cursorId,
      @Param("publishedAt") Timestamp publishedAt);
}
