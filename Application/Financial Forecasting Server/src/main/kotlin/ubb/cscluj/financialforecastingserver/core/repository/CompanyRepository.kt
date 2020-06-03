package ubb.cscluj.financialforecastingserver.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ubb.cscluj.financialforecastingserver.core.model.Company

@Repository
interface CompanyRepository : JpaRepository<Company, Long> {
}
