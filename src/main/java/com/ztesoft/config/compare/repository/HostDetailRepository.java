package com.ztesoft.config.compare.repository;

import com.ztesoft.config.compare.entity.HostDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostDetailRepository extends JpaRepository<HostDetail, Long> {
    List<HostDetail> findByHostId(Long hostId);

    void deleteByHostId(Long hostId);

    @Query(value = "update host_detail set value=?1 where host_id = ?2 and key=?3 ;", nativeQuery = true)
    @Modifying
    void updateOne(String value, Long hostId, String key);

    HostDetail findByHostIdAndKey(Long hostId, String key);

    boolean existsByHostIdAndAndKey(Long hostId, String key);
}