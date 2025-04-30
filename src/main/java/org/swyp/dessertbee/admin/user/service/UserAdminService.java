package org.swyp.dessertbee.admin.user.service;

import java.util.UUID;

public interface UserAdminService {

    void suspendUserForOneMonthByUuid(UUID userUuid);
}
