package ubb.cscluj.financialforecasting.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedAddCompanyTaskState
import ubb.cscluj.financialforecasting.admin.viewmodel.exposed_states.AdminExposedUpdateCompanyTaskState
import ubb.cscluj.financialforecasting.model.Company

class AdminSharedCompanyViewModel (application: Application) : AndroidViewModel(application) {
    private val _adminExposedAddCompanyTaskState: MutableLiveData<AdminExposedAddCompanyTaskState> = MutableLiveData()
    private val _adminExposedUpdateCompanyTaskState: MutableLiveData<AdminExposedUpdateCompanyTaskState> = MutableLiveData()

    val adminExposedAddCompanyTaskState: LiveData<AdminExposedAddCompanyTaskState> = _adminExposedAddCompanyTaskState
    val adminExposedUpdateCompanyTaskState: LiveData<AdminExposedUpdateCompanyTaskState> = _adminExposedUpdateCompanyTaskState


    fun addNewAddTask(company: Company) {
        _adminExposedAddCompanyTaskState.value =
            AdminExposedAddCompanyTaskState(
                isInitialization = false,
                isCancelled = false,
                company = company
            )
    }

    fun addCancelledAddTask() {
        _adminExposedAddCompanyTaskState.value =
            AdminExposedAddCompanyTaskState(
                isInitialization = false,
                isCancelled = true,
                company = null
            )
    }

    fun revertUninitializedAddTask() {
        _adminExposedAddCompanyTaskState.value =
            AdminExposedAddCompanyTaskState(
                isInitialization = true,
                isCancelled = false,
                company = null
            )
    }

    fun addNewUpdateTask(company: Company) {
        _adminExposedUpdateCompanyTaskState.value =
            AdminExposedUpdateCompanyTaskState(
                isInitialization = false,
                isCancelled = false,
                company = company
            )
    }

    fun addCancelledUpdateTask() {
        _adminExposedUpdateCompanyTaskState.value =
            AdminExposedUpdateCompanyTaskState(
                isInitialization = false,
                isCancelled = true,
                company = null
            )
    }

    fun revertUninitializedUpdateTask() {
        _adminExposedUpdateCompanyTaskState.value =
            AdminExposedUpdateCompanyTaskState(
                isInitialization = true,
                isCancelled = false,
                company = null
            )
    }
}