package com.ztesoft.config.compare.repository;

import com.ztesoft.config.compare.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    void deleteByProjectId(Long projectId);
}