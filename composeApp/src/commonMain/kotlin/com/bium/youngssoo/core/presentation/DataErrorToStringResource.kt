package com.bium.youngssoo.core.presentation

import youngsso.composeapp.generated.resources.Res
import youngsso.composeapp.generated.resources.error_board_failed
import youngsso.composeapp.generated.resources.error_comment_failed
import youngsso.composeapp.generated.resources.error_disk_full
import youngsso.composeapp.generated.resources.error_login_failed
import youngsso.composeapp.generated.resources.error_no_internet
import youngsso.composeapp.generated.resources.error_request_fail
import youngsso.composeapp.generated.resources.error_request_timeout
import youngsso.composeapp.generated.resources.error_serialization
import youngsso.composeapp.generated.resources.error_too_many_requests
import youngsso.composeapp.generated.resources.error_unknown
import com.bium.youngssoo.core.domain.DataError

fun DataError.toUiText(): UiText {
    val stringRes = when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.UNKNOWN -> Res.string.error_unknown
        DataError.Local.EMPTY_TEMP_WRITING -> Res.string.error_unknown
        DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_request_timeout
        DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
        DataError.Remote.SERVER -> Res.string.error_unknown
        DataError.Remote.SERIALIZATION -> Res.string.error_serialization
        DataError.Remote.UNKNOWN -> Res.string.error_unknown
        DataError.Remote.LOGIN_FAILED -> Res.string.error_login_failed
        DataError.Remote.FAILED_BOARD -> Res.string.error_board_failed
        DataError.Remote.FAILED_COMMENT -> Res.string.error_comment_failed
        DataError.Remote.REQUEST_ERROR -> Res.string.error_request_fail
        DataError.Remote.REFRESH_TOKEN_FAILED -> Res.string.error_request_fail
    }

    return UiText.StringResourceId(stringRes)
}
