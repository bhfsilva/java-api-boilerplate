package br.com.boilerplate.repositories.user;

import br.com.boilerplate.dtos.user.input.FindUsersByFiltersInputDTO;
import br.com.boilerplate.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl {
    private final EntityManager entityManager;
    private final UserJpaRepository jpaRepository;

    private Optional<User> findBy(String field, Object value) {
        if (Objects.isNull(field))
            return Optional.empty();

        final var criteriaBuilder = entityManager.getCriteriaBuilder();
        final var query = criteriaBuilder.createQuery(User.class);
        final var user = query.from(User.class);

        var onlyActiveUsers = true;
        var activeUserPredicates = this.getUserStatusPredicate(user, onlyActiveUsers);

        query.where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(user.get(field), value),
                        activeUserPredicates
                )
        );

        final var results = entityManager.createQuery(query).getResultList();
        return results.stream().findFirst();
    }

    public Page<User> findByFilters(FindUsersByFiltersInputDTO filters) {
        final var criteriaBuilder = entityManager.getCriteriaBuilder();
        final var query = criteriaBuilder.createQuery(User.class);
        final var user = query.from(User.class);
        final var pageLimit = filters.getLimit();
        final var pageNumber = filters.getPage();

        query.where(this.getFilterPredicates(user, filters)).orderBy(
                criteriaBuilder.asc(user.get("name")),
                criteriaBuilder.asc(user.get("createdAt"))
        );

        final var typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(pageLimit);
        typedQuery.setFirstResult(pageNumber * pageLimit);

        final var totalElements = getTotalUsersByFilters(filters);

        final var pageable = PageRequest.of(pageNumber, pageLimit);
        return new PageImpl<>(typedQuery.getResultList(), pageable, totalElements);
    }

    private Predicate getFilterPredicates(Root<User> user, FindUsersByFiltersInputDTO filters) {
        final var criteriaBuilder = entityManager.getCriteriaBuilder();

        final var predicates = new ArrayList<Predicate>();

        Optional.ofNullable(filters.getName()).ifPresent(name -> {
                var userName = criteriaBuilder.lower(user.get("name"));
                predicates.add(criteriaBuilder.like(userName, "%"+ name.toLowerCase() +"%"));
        });

        Optional.ofNullable(filters.getEmail()).ifPresent(email ->
                predicates.add(criteriaBuilder.equal(user.get("email"), email))
        );

        Optional.ofNullable(filters.getRole()).ifPresent(role ->
                predicates.add(criteriaBuilder.equal(user.get("role"), role))
        );

        predicates.add(this.getUserStatusPredicate(user, filters.getActive()));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate getUserStatusPredicate(Root<User> user, Boolean onlyActiveUsers) {
        final var criteriaBuilder = entityManager.getCriteriaBuilder();

        if (Objects.isNull(onlyActiveUsers))
            return criteriaBuilder.conjunction();

        if (onlyActiveUsers)
            return criteriaBuilder.isNotNull(user.get("emailValidatedAt"));

        return criteriaBuilder.isNull(user.get("emailValidatedAt"));
    }

    private Long getTotalUsersByFilters(FindUsersByFiltersInputDTO filters) {
        final var criteriaBuilder = entityManager.getCriteriaBuilder();

        var query = criteriaBuilder.createQuery(Long.class);
        var users = query.from(User.class);

        var countQuery = query.select(criteriaBuilder.count(users));
        countQuery.where(this.getFilterPredicates(users, filters));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public Optional<User> findByEmail(String email) {
        return this.findBy("email", email);
    }

    public Optional<User> findByEmailIncludeInactive(String email) {
        return jpaRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return this.findBy("id", id);
    }

    public Optional<User> findByIdIncludeInactive(UUID id) {
        return jpaRepository.findById(id);
    }

    public User save(User entity) {
        return jpaRepository.save(entity);
    }

    public void delete(User entity) {
        jpaRepository.delete(entity);
    }
}
