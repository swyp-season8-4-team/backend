package org.swyp.dessertbee.admin.user.service;

import org.swyp.dessertbee.email.service.WarningMailService;

import java.util.UUID;

public interface UserAdminService {
    //계정 정지
    void suspendUserForOneMonthByUuid(UUID userUuid);

    //작성 제한
    void restrictUserWritingFor7DaysByUuid(UUID userUuid);

    //사용자 경고
    void warnUserByUuid(UUID userUuid, String reason, WarningMailService warningMailService, String email);
}
