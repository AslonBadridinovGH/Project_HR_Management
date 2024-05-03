package uz.pdp.rest_api_jwt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.rest_api_jwt.entity.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EmployeeTaskRepository extends JpaRepository<Task, UUID> {


    List<Task>findAllByEmployee_RolesId(Integer employee_roles_id);

    Optional<Task> findByIdAndEmployeeRolesId(UUID id, Integer employee_roles_id);

    List<Task> findByEmployeeIdAndStatusIn(UUID employee_id, Set<Integer> status);

    List<Task>findAllByCompletedAtBetweenAndEmployeeIdAndStatus
    (LocalDate completedAt, LocalDate completedAt2, UUID employee_id,Integer status);


  // List<Task> findByEmployeeIdAndStatusOrStatus(UUID employee_id, Integer status, Integer status2);
  //  List<Task> findByEmployeeIdAndStatus
  //  (UUID employee_id, Integer status, Integer status2, Integer status3);

}
