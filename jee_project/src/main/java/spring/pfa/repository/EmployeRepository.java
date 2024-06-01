package spring.pfa.repository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import spring.pfa.model.Administrateur;
import spring.pfa.model.Conge;
import spring.pfa.model.Employe;

public interface EmployeRepository extends JpaRepository<Employe,Long> {
	Employe findByEmail(String email);
	List<Employe> findAll();

	@Query("SELECT e FROM Employe e WHERE e.nom LIKE %:nom%")
	Page<Employe> findByEmployeOrDateOrEtat(@Param("nom") String nom, Pageable pageable);
	Employe findByEmailAndPassword(String email, String password);
}