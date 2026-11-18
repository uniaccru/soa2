package org.example.dragon.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.dragon.entity.Dragon;
import org.example.dragon.exception.ResourceNotFoundException;
import org.example.dragon.query.DragonPage;
import org.example.dragon.query.DragonQuery;
import org.example.dragon.repository.DragonRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
@Transactional
public class DragonServiceImpl implements DragonService {

    @Inject
    private DragonRepository repository;

    @Override
    public Dragon create(Dragon dragon) {
        dragon.setId(null);
        dragon.setCreationDate(LocalDate.now());
        return repository.save(dragon);
    }

    @Override
    public DragonPage find(DragonQuery query) {
        return repository.find(query);
    }

    @Override
    public Dragon get(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dragon with id " + id + " not found"));
    }

    @Override
    public Dragon update(long id, Dragon data) {
        Dragon existing = get(id);
        existing.applyFrom(data);
        return existing;
    }

    @Override
    public void delete(long id) {
        repository.delete(get(id));
    }

    @Override
    public void deleteByWeight(int weight) {
        // удаляем по одному, чтобы сработало каскадное удаление пещер
        repository.findByWeight(weight).forEach(repository::delete);
    }

    @Override
    public Double averageAge() {
        return repository.averageAge();
    }

    @Override
    public List<String> uniqueDescriptions() {
        return repository.distinctDescriptions();
    }
}
