package org.apache.airavata.api;

import java.util.List;

import org.apache.airavata.application.service.ApplicationModuleService;
import org.apache.airavata.models.appcatalog.appdeployment.ApplicationModule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.service.annotation.DeleteExchange;

@Controller
public class ApplicationController {

    private final ApplicationModuleService applicationModuleService;

    public ApplicationController(ApplicationModuleService applicationModuleService) {
        this.applicationModuleService = applicationModuleService;
    }

    @GetMapping("/application-module/{moduleId}")
    @ResponseBody
    public ApplicationModule getApplicationModule(@PathVariable String moduleId) throws Exception {
        return applicationModuleService.getApplicationModule(null, moduleId);
    }

    @GetMapping("/application-module")
    @ResponseBody
    public List<ApplicationModule> getAllApplicationModules() throws Exception {
        return applicationModuleService.getAllApplicationModules();
    }

    @PostMapping("/application-module")
    @ResponseBody
    public ApplicationModule createApplicationModule(@RequestBody ApplicationModule module) throws Exception {
        return applicationModuleService.createApplicationModule(null, module);
    }

    @PostMapping("/application-module/{moduleId}")
    @ResponseBody
    public ApplicationModule updateApplicationModule(
            @PathVariable String moduleId, @RequestBody ApplicationModule module) throws Exception {
        return applicationModuleService.updateApplicationModule(null, moduleId, module);
    }

    @DeleteExchange("/application-module/{moduleId}")
    @ResponseBody
    public void deleteApplicationModule(@PathVariable String moduleId) throws Exception {
        applicationModuleService.deleteApplicationModule(null, moduleId);
    }

}
