package com.java.date.example.javadateexample.repositories;

import com.java.date.example.javadateexample.entities.DateExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DateExampleRepository extends JpaRepository<DateExample, Long> {

}
