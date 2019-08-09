package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.entity.Project;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/project")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @RequestMapping(method = RequestMethod.POST)
    public Project insert(@RequestBody Project project) {
        return projectRepository.save(project);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/p")
    public Project insert1(@RequestParam String name, @RequestParam String comments) {
        Project project = new Project();
        project.setName(name);
        project.setComments(comments);
        return projectRepository.save(project);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Project update(@RequestBody Project project) {
        return projectRepository.save(project);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> find() {
        List<Project> projects = projectRepository.findAll();
        return ResponseUtil.renderTableResponse(projects);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Project findById(@PathVariable Long id) {
        return projectRepository.getOne(id);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public Map<String, Object> delete(@RequestParam("projectId") Long projectId) {
//        projectRepository.
        Map<String, Object> map = new HashMap<>();
        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            map.put("result", !projectRepository.existsById(projectId));
        } else {
            map.put("result", true);
        }
        return map;
    }
}
