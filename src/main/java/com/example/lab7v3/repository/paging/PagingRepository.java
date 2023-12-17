package com.example.lab7v3.repository.paging;

import com.example.lab7v3.domain.Entity;
import com.example.lab7v3.repository.Repository;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAllOnPage(Pageable pageable);
}
