package org.swyp.dessertbee.community.mate.service;

import org.springframework.data.domain.Pageable;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;

import java.util.UUID;

public interface SavedMateService {

    /** 디저트메이트 저장 */
    void saveMate(UUID mateUuid);

    /** 디저트메이트 삭제 */
    void deleteSavedMate(UUID mateUuid);

    /** 저장된 디저트메이트 조회 (userId에 해당하는 Mate만 가져오기) */
    MatesPageResponse getSavedMates(Pageable pageable);

}
