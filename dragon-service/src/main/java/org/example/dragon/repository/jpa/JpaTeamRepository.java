package org.example.dragon.repository.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.dragon.entity.Team;
import org.example.dragon.repository.TeamRepository;

@ApplicationScoped
public class JpaTeamRepository implements TeamRepository {

    @PersistenceContext(unitName = "DragonPU")
    private EntityManager em;

    @Override
    public boolean existsById(long id) {
        return em.find(Team.class, id) != null;
    }

    @Override
    public Team save(Team team) {
        em.persist(team);
        return team;
    }
}
