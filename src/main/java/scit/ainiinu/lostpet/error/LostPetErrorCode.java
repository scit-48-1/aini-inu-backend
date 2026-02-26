package scit.ainiinu.lostpet.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import scit.ainiinu.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum LostPetErrorCode implements ErrorCode {
    L001_IMAGE_REQUIRED("L001", HttpStatus.BAD_REQUEST, "분석 이미지가 필요합니다."),
    L002_INVALID_IMAGE("L002", HttpStatus.BAD_REQUEST, "분석 이미지가 유효하지 않습니다."),
    L404_NOT_FOUND("L404", HttpStatus.NOT_FOUND, "요청한 대상을 찾을 수 없습니다."),
    L403_FORBIDDEN("L403", HttpStatus.FORBIDDEN, "권한이 없습니다."),
    L409_DUPLICATE_ACTIVE_REPORT("L409", HttpStatus.CONFLICT, "미해결 중복 실종 신고가 존재합니다."),
    L409_MATCH_CONFLICT("L409", HttpStatus.CONFLICT, "현재 매치 상태에서는 승인할 수 없습니다."),
    L410_REPORT_RESOLVED("L410", HttpStatus.GONE, "해결된 실종 신고입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
