package com.thavin.email_invitations.data.remote.request_invite.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val errorMessage: String)