package org.example.dragon.repository.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.dragon.entity.Cave;
import org.example.dragon.entity.Dragon;
import org.example.dragon.query.DragonField;
import org.example.dragon.query.DragonPage;
import org.example.dragon.query.DragonQuery;
import org.example.dragon.repository.DragonRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class JpaDragonRepository implements DragonRepository {

    @PersistenceContext(unitName = "DragonPU")
    private EntityManager em;

    @Override
    public Dragon save(Dragon dragon) {
        em.persist(dragon);
        return dragon;
    }

    @Override
    public Optional<Dragon> findById(long id) {
        return Optional.ofNullable(em.find(Dragon.class, id));
    }

    @Override
    public List<Dragon> findByWeight(int weight) {
        return em.createQuery("select d from Dragon d where d.weight = :weight", Dragon.class)
                .setParameter("weight", weight)
                .getResultList();
    }

    @Override
    public void delete(Dragon dragon) {
        em.remove(dragon);
    }

    @Override
    public Double averageAge() {
        return em.createQuery("select avg(d.age) from Dragon d", Double.class).getSingleResult();
    }

    @Override
    public List<String> distinctDescriptions() {
        return em.createQuery(
                "select distinct d.description from Dragon d where d.description is not null order by d.description",
                String.class).getResultList();
    }

    @Override
    public DragonPage find(DragonQuery query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        long total = count(cb, query);

        CriteriaQuery<Dragon> cq = cb.createQuery(Dragon.class);
        Root<Dragon> root = cq.from(Dragon.class);
        Join<Dragon, Cave> cave = root.join("cave", JoinType.LEFT);
        cq.select(root)
                .where(predicates(cb, root, cave, query))
                .orderBy(orders(cb, root, cave, query));

        long offset = (long) query.page() * query.size();
        List<Dragon> items = offset > Integer.MAX_VALUE
                ? Collections.emptyList()
                : em.createQuery(cq)
                        .setFirstResult((int) offset)
                        .setMaxResults(query.size())
                        .getResultList();

        return new DragonPage(items, total, query.page(), query.size());
    }

    private long count(CriteriaBuilder cb, DragonQuery query) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Dragon> root = cq.from(Dragon.class);
        Join<Dragon, Cave> cave = root.join("cave", JoinType.LEFT);
        cq.select(cb.count(root)).where(predicates(cb, root, cave, query));
        return em.createQuery(cq).getSingleResult();
    }

    private Predicate[] predicates(CriteriaBuilder cb, Root<Dragon> root, Join<Dragon, Cave> cave, DragonQuery query) {
        List<Predicate> result = new ArrayList<>();
        query.filters().forEach((field, value) -> result.add(cb.equal(path(field, root, cave), value)));
        return result.toArray(new Predicate[0]);
    }

    private List<Order> orders(CriteriaBuilder cb, Root<Dragon> root, Join<Dragon, Cave> cave, DragonQuery query) {
        DragonField sortBy = query.sortBy() == null ? DragonField.ID : query.sortBy();
        Path<?> sortPath = path(sortBy, root, cave);
        List<Order> result = new ArrayList<>();
        result.add(query.ascending() ? cb.asc(sortPath) : cb.desc(sortPath));
        if (sortBy != DragonField.ID) {
            result.add(cb.asc(root.get("id"))); // стабильный порядок между страницами
        }
        return result;
    }

    /** Единственное место, которое знает, как поле API отображается на JPA-модель. */
    private Path<?> path(DragonField field, Root<Dragon> root, Join<Dragon, Cave> cave) {
        return switch (field) {
            case ID -> root.get("id");
            case NAME -> root.get("name");
            case X -> root.get("coordinates").get("x");
            case Y -> root.get("coordinates").get("y");
            case CREATION_DATE -> root.get("creationDate");
            case AGE -> root.get("age");
            case DESCRIPTION -> root.get("description");
            case WEIGHT -> root.get("weight");
            case COLOR -> root.get("color");
            case KILLER_NAME -> root.get("killer").get("name");
            case KILLER_HEIGHT -> root.get("killer").get("height");
            case KILLER_EYE_COLOR -> root.get("killer").get("eyeColor");
            case KILLER_HAIR_COLOR -> root.get("killer").get("hairColor");
            case CAVE_DEPTH -> cave.get("depth");
        };
    }
}
