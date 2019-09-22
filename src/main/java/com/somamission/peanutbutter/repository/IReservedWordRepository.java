package com.somamission.peanutbutter.repository;

import com.somamission.peanutbutter.domain.ReservedWord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IReservedWordRepository extends CrudRepository<ReservedWord, Integer> {
}
