package com.thavin.email_invitations.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.thavin.email_invitations.data.local.fake.FakeInviteStatusRepositoryImpl
import com.thavin.email_invitations.data.remote.fake.FakeRequestInviteRepositoryImpl
import com.thavin.email_invitations.presentation.viewmodel.InvitationViewModel.InvitationUiEvent.*
import com.thavin.email_invitations.presentation.viewmodel.InvitationViewModel.UserDetailsUiEvent.*
import com.thavin.email_invitations.presentation.viewmodel.InvitationViewModel.CancelInviteUiEvent.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class InvitationViewModelTest {

    private lateinit var viewModel: InvitationViewModel
    private val dispatcher = StandardTestDispatcher()
    private val requestInviteRepositoryImpl = FakeRequestInviteRepositoryImpl()
    private val inviteStatusRepositoryImpl = FakeInviteStatusRepositoryImpl()

    @Before
    fun beforeTests() {
        viewModel = InvitationViewModel(
            requestInviteRepositoryImpl,
            inviteStatusRepositoryImpl
        )
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun afterTests() {
        Dispatchers.resetMain()
    }

    @Test
    fun `requesting an invite`() = runTest {
        viewModel.requestInviteOnClick()

        viewModel.invitationUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(RequestInviteOnClick)
        }
    }

    @Test
    fun `requesting to cancel an invite`() = runTest {
        viewModel.requestCancelInviteOnClick()

        viewModel.invitationUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(RequestCancelInviteOnClick)
        }
    }

    @Test
    fun `sending user details returns a success`() = runTest {
        requestInviteRepositoryImpl.shouldReturnError(false)

        viewModel.sendUserDetailsOnClick("testName", "testEmail@test.com", "testEmail@test.com")

        viewModel.userDetailsUiEvent.test {
            listOf(
                ValidName,
                ValidEmail,
                ValidConfirmEmail,
                InviteDetailsLoading,
                InviteDetailsSuccess
            ).forEach { uiEvent ->
                val emission = awaitItem()
                assertThat(emission).isEqualTo(uiEvent)
            }
        }
    }

    @Test
    fun `sending user details returns an error`() = runTest {
        requestInviteRepositoryImpl.shouldReturnError(true)
        viewModel.sendUserDetailsOnClick("testName", "testEmail@test.com", "testEmail@test.com")

        viewModel.userDetailsUiEvent.test {
            listOf(
                ValidName,
                ValidEmail,
                ValidConfirmEmail,
                InviteDetailsLoading,
                InviteDetailsError("error")
            ).forEach { uiEvent ->
                val emission = awaitItem()
                assertThat(emission).isEqualTo(uiEvent)
            }
        }
    }

    @Test
    fun `sending invalid user details returns validation error`() = runTest {
        requestInviteRepositoryImpl.shouldReturnError(true)
        viewModel.sendUserDetailsOnClick("sam", "testEmail", "testEmail@test.com")

        viewModel.userDetailsUiEvent.test {
            listOf(
                InvalidName,
                InvalidEmail,
                InvalidConfirmEmail
            ).forEach { uiEvent ->
                val emission = awaitItem()
                assertThat(emission).isEqualTo(uiEvent)
            }
        }
    }

    @Test
    fun `dismissing the invite details dialog`() = runTest {
        viewModel.dismissInviteDetailsDialogOnClick()

        viewModel.userDetailsUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(DismissInviteDetailsDialogOnClick)
        }
    }

    @Test
    fun `invited status displays the post invite screen`() = runTest {
        inviteStatusRepositoryImpl.setInvitedStatus(true)
        viewModel.checkInviteStatus()

        viewModel.invitationUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(ShowPostInviteScreen)
        }
    }

    @Test
    fun `not invited status returns the pre invite screen`() = runTest {
        inviteStatusRepositoryImpl.setInvitedStatus(false)
        viewModel.checkInviteStatus()

        viewModel.invitationUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(ShowPreInviteScreen)
        }
    }

    @Test
    fun `cancelling a current invite`() = runTest {
        viewModel.cancelInviteOnClick()

        viewModel.cancelInviteUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(CancelInviteSuccess)
        }
    }

    @Test
    fun `dismissing the cancel invite dialog`() = runTest {
        viewModel.dismissCancelInviteDialogOnClick()

        viewModel.cancelInviteUiEvent.test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(DismissCancelInviteDialogOnClick)
        }
    }
}