package com.sparklenote.roll.service;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.request.RollUpdateRequestDto;
import com.sparklenote.roll.dto.response.RollJoinResponseDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class RollService {

    @Value("${jwt.accessExpiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenExpiration;

    private final RollRepository rollRepository;
    private final UserRepository userRepository;
    private final UrlGenerator urlGenerator;
    private final StudentRepository studentRepository;
    private final JWTUtil jwtUtil;

    public RollResponseDTO createRoll(RollCreateRequestDto createRequestDto) {
        int classCode = ClassCodeGenerator.generateClassCode(); // 학급 코드 생성
        String url = urlGenerator.generateUrl(); // URL 생성

        // SecurityContextHolder에서 로그인된 사용자의 username 가져오기
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = customOAuth2User.getUsername();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Roll 엔티티 생성
        Roll roll = Roll.fromRollCreateDto(createRequestDto, classCode, url, user);

        // Roll 저장
        Roll savedRoll = rollRepository.save(roll);

        // RollResponseDTO 생성하여 반환
        return RollResponseDTO.fromRoll(savedRoll, user.getId()); // RollResponseDTO에 URL 포함
    }

    public RollResponseDTO getRollById(Long id) {
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 Roll을 찾을 수 없습니다."));
        User user = roll.getUser(); // 롤의 소유자 정보 가져오기
        return RollResponseDTO.fromRoll(roll, user.getId());
    }

    public void deleteRoll(Long id) {
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 Roll을 찾을 수 없습니다."));
        rollRepository.delete(roll);
    }

    public RollResponseDTO updateRollName(Long id, RollUpdateRequestDto updateRequestDto) {
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 Roll을 찾을 수 없습니다."));

        // Roll 이름 수정
        roll.updateName(updateRequestDto.getRollName());

        // 수정된 Roll 저장
        Roll updatedRoll = rollRepository.save(roll);

        // 수정된 Roll 정보를 DTO로 변환하여 반환
        Long userId = roll.getUser().getId();
        return RollResponseDTO.fromRoll(updatedRoll,userId);
    }

    public RollJoinResponseDto joinRoll(String url, RollJoinRequestDto joinRequestDto) {
        // Roll 조회 및 학급 코드 검증
        Roll roll = rollRepository.findByUrl(url)
                .orElseThrow(() -> new IllegalArgumentException("해당 URL의 Roll을 찾을 수 없습니다."));

        if (roll.getClassCode() != joinRequestDto.getClassCode()) {
            throw new IllegalArgumentException("학급 코드가 일치하지 않습니다.");
        }

        // 학생 조회 또는 등록
        Optional<Student> optionalStudent = studentRepository.findByClassCodeAndNameAndPinNumber(
                joinRequestDto.getClassCode(),
                joinRequestDto.getName(),
                joinRequestDto.getPinNumber()
        );

        Student student;
        if (optionalStudent.isEmpty()) {
            // 회원가입 처리
            student = studentRepository.save(joinRequestDto.toStudent(roll));
        } else {
            // 기존 학생 정보 사용
            student = optionalStudent.get();
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                student.getStudentId().toString(),
                Role.STUDENT, // 학생의 역할을 지정
                accessTokenExpiration
        );
        String refreshToken = jwtUtil.createRefreshToken(
                student.getStudentId().toString(),
                refreshTokenExpiration
        );

        // 응답 DTO 생성
        RollJoinResponseDto responseDto = RollJoinResponseDto.builder()
                .name(student.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return responseDto;
    }
}
