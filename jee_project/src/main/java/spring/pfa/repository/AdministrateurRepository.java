package spring.pfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import spring.pfa.model.Administrateur;

public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {
	Administrateur findByEmail(String email);

	Administrateur findByEmailAndPassword(String email, String password);
}
