package com.sparklenote.roll.dto.response;

import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollJoinResponseDto {

    private String name;
    private String accessToken;
    private String refreshToken;

}