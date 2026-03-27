package com.bium.youngssoo.core.data

object ApiConstants {

    const val BASE_URL = "https://api.bium-place.com"

    object KEY {
        const val KEY_AUTH_TOKEN = "X-AUTH-TOKEN"
        const val KEY_IS_DEVIL = "isDevil"
        const val KEY_TARGET_USER_ID = "targetUserId"
        const val KEY_SIZE = "size"

        const val KEY_BOARD_ID = "boardId"
        const val KEY_COMMENT_ID = "commentId"


        const val KEY_USER_ID = "userId"
        const val KEY_TOKEN = "token"

        const val KEY_REFRESH_TOKEN = "refreshToken"

        const val KEY_PAGE = "page"

        const val KEY_PARENT_ID = "parentId"
        const val KEY_CATEGORY_ID = "categoryId"

        const val KEY_CATEGORY_NAME = "categoryName"
    }

    object Endpoints {
        const val COMMENT = "/comment"
        const val BOARD = "/board"

        const val USER_SORTED = "/user-sorted"
        const val BOARDS = "/boards"
        const val FIREBASE_TOKEN = "/firebase-token"
        const val REFRESH_ACCESS_TOKEN = "/refreshAccessToken"
        const val MY_BOARD_INFO = "/my-board-info"
        const val MY_COMMENT_LIST = "/my-comment-list"
        const val POINT = "/point"
        const val TOP_100_COMMENTS = "/top-100/comments"
        const val COMMENTS = "/comments"
        const val SOCIAL_SIGNUP = "/signup"
        const val WITHDRAW = "/withdraw"
        const val SOCIAL_SIGNIN = "/signin"
        const val CATEGORY_LIST = "category-list"
        const val LIKE_BOARD = "/like-board"
        const val LIKE_REPLY = "/like-comment"

        const val REPORT = "/report"

        const val MY_NOTICE = "/my-notice"
        const val ALARM = "/alarm"
        const val NOTICE = "/notice"
    }
}