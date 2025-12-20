package com.study.ticket.domain.repository;

import com.study.ticket.domain.Entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    “이 데이터를 가져오는 동안,
//    다른 애가 동시에 건드리지 못하게 잠가(lock)라”
    @Query("select u from User u where u.id = :id")
//    SQL: select * from users where user_id = ?
//    JPQL: select u from User u where u.id = :id
    Optional<User> findByIdForUpdate(@Param("id") Long id);

//    @Param("id")
//    메서드 파라미터 id 값을
//    JPQL의 :id 자리에 꽂아 넣어라
}
