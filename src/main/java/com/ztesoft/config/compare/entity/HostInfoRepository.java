package com.ztesoft.config.compare.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostInfoRepository extends JpaRepository<HostInfo, Long> {
    List<HostInfo> findByProjectId(Long projectId);
}