package org.example.dragon.repository.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.dragon.entity.Cave;
import org.example.dragon.repository.CaveRepository;

@ApplicationScoped
public class JpaCaveRepository implements CaveRepository {

    @PersistenceContext(unitName = "DragonPU")
    private EntityManager em;

    @Override
    public boolean existsById(long id) {
        return em.find(Cave.class, id) != null;
    }
}
