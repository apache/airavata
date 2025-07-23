package org.apache.airavata.admin_api_server.service;

import org.apache.airavata.admin_api_server.entity.Model;
import org.apache.airavata.admin_api_server.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;

    public List<Model> getAllModels() {
        return modelRepository.findAll();
    }

    public Optional<Model> getModelById(Long id) {
        return modelRepository.findById(id);
    }

    public List<Model> getModelsByCategory(String category) {
        return modelRepository.findByCategory(category);
    }

    public List<Model> getModelsByTag(String tag) {
        return modelRepository.findByTag(tag);
    }

    public List<Model> searchModelsByKeyword(String keyword) {
        return modelRepository.findByKeyword(keyword);
    }

    public Model createModel(Model model) {
        return modelRepository.save(model);
    }

    public Optional<Model> updateModel(Long id, Model modelDetails) {
        return modelRepository.findById(id).map(model -> {
            model.setTitle(modelDetails.getTitle());
            model.setDescription(modelDetails.getDescription());
            model.setTags(modelDetails.getTags());
            model.setAuthors(modelDetails.getAuthors());
            model.setCategory(modelDetails.getCategory());
            return modelRepository.save(model);
        });
    }

    public boolean deleteModel(Long id) {
        if (modelRepository.existsById(id)) {
            modelRepository.deleteById(id);
            return true;
        }
        return false;
    }

}