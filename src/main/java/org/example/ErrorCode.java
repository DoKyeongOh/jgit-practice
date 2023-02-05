package org.example;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ErrorCode {

    // 리모트 저장소 불러오는 중 에러
    DIRECTORY_IS_NOT_EXIST("입력받은 경로가 존재하지 않습니다."),
    INPUT_IS_NOT_DIRECTORY("입력받은 경로가 디렉토리가 아닙니다."),
    BAD_DIRECTORY("입력받은 디렉토리가 비어있지 않으며 디렉토리에 .git이 없습니다."),
    REMOTE_CLONE_FAILURE("리모트 저장소 클론에 실패했습니다."),
    LOCAL_OPEN_FAILURE("로컬 저장소를 불러오는 것에 실패했습니다."),

    // JGIT 사용 중 발생 가능한 에러
    GIT_RESET_FAILURE("git reset에 실패했습니다."),
    DIFF_SEARCH_FAILURE("파일 변경 상태를 읽어들이는 것에 실패했습니다."),
    DIFF_FILE_STAGING_FAILURE("변경되거나 추가된 파일을 스테이징 영역에 옮기는 것에 실패했습니다."),
    REMOVED_FILE_STAGING_FAILURE("삭제된 파일을 스테이징 영역에 옮기는 것에 실패했습니다."),
    GIT_COMMIT_FAILURE("커밋 생성에 실패했습니다.");


    private String detail;
}
