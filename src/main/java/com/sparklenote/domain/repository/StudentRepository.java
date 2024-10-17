package com.sparklenote.domain.repository;

import com.sparklenote.domain.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // 클래스 코드, 이름, 핀번호로 학생을 찾는 메소드
    Optional<Student> findByClassCodeAndNameAndPinNumber(int classCode, String name, int pinNumber);
}
