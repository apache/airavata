package org.apache.airavata.api;

import java.util.List;

import org.apache.airavata.application.service.ApplicationModuleService;
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/v1/appmodule")
public class ApplicationModuleController {

    private final ApplicationModuleService applicationModuleService;

    public ApplicationModuleController(ApplicationModuleService applicationModuleService) {
        this.applicationModuleService = applicationModuleService;
    }

    @GetMapping("/{moduleId}")
    @ResponseBody
    public ApplicationModule getApplicationModule(@PathVariable String moduleId) throws Exception {
        return applicationModuleService.getApplicationModule(null, moduleId);
    }

    @GetMapping("")
    @ResponseBody
    public List<ApplicationModule> getAllApplicationModules() throws Exception {
        return applicationModuleService.getAllApplicationModules();
    }

    @PostMapping("")
    @ResponseBody
    public ApplicationModule createApplicationModule(@RequestBody ApplicationModule module) throws Exception {
        return applicationModuleService.createApplicationModule(null, module);
    }

    @PostMapping("/{moduleId}")
    @ResponseBody
    public ApplicationModule updateApplicationModule(
            @PathVariable String moduleId, @RequestBody ApplicationModule module) throws Exception {
        return applicationModuleService.updateApplicationModule(null, moduleId, module);
    }

    @DeleteMapping("/{moduleId}")
    @ResponseBody
    public void deleteApplicationModule(@PathVariable String moduleId) throws Exception {
        applicationModuleService.deleteApplicationModule(null, moduleId);
    }

}
