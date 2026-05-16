package co.fintra.financiero.models.repositories;

import co.fintra.financiero.models.entity.EmpresaCuentaBancariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IEmpresaCuentaBancariaRepository extends JpaRepository<EmpresaCuentaBancariaEntity, Long> {

  List<EmpresaCuentaBancariaEntity> findAllByEmpresaIdAndDeletedAtIsNullOrderByIdAsc(Long empresaId);

  Optional<EmpresaCuentaBancariaEntity> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

  boolean existsByEmpresaIdAndBancoCodigoAndNumeroCuentaAndDeletedAtIsNull(
      Long empresaId, String bancoCodigo, String numeroCuenta);

  boolean existsByEmpresaIdAndBancoCodigoAndNumeroCuentaAndIdNotAndDeletedAtIsNull(
      Long empresaId, String bancoCodigo, String numeroCuenta, Long id);

  List<EmpresaCuentaBancariaEntity> findAllByDeletedAtIsNullOrderByEmpresaRazonSocialAscIdAsc();
}
