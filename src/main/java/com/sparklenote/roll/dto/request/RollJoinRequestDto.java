package com.sparklenote.roll.dto.request;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.enumType.Role;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollJoinRequestDto {

    private int classCode;
    private String name;

    @Pattern(regexp = "\\d{4}", message = "PIN 번호는 4자리 숫자여야 합니다.")
    private int pinNumber;

    public Student toStudent(Roll roll) {
        return Student.builder()
                .classCode(classCode)
                .name(name)
                .pinNumber(pinNumber)
                .roll(roll) // Roll 객체 설정 (롤링페이퍼)
                .role(Role.STUDENT) // 역할 설정 (학생 역할)
                .build();
    }
}
