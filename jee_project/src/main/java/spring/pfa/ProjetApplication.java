package spring.pfa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import spring.pfa.model.Administrateur;
import spring.pfa.model.Conge;
import spring.pfa.model.Employe;
import spring.pfa.model.Etat;
import spring.pfa.repository.AdministrateurRepository;
import spring.pfa.repository.CongeRepository;
import spring.pfa.repository.EmployeRepository;

@SpringBootApplication
public class ProjetApplication {
    static AdministrateurRepository adminRepo;
    static CongeRepository congeRepo;
    static EmployeRepository employeRepo;

    public static void main(String[] args) {
        ApplicationContext contexte = SpringApplication.run(ProjetApplication.class, args);

        // repos
        adminRepo = contexte.getBean(AdministrateurRepository.class);
        congeRepo = contexte.getBean(CongeRepository.class);
        employeRepo = contexte.getBean(EmployeRepository.class);

        // Seed the database
        seedDatabase();
        
        // Update conge status
        updateCongeStatus();
    }
    
    private static void updateCongeStatus() {
        LocalDate today = LocalDate.now();

        // maj etat "VALIDE" a "EN_COURS" si dateDebut est aujourd'hui ou avant
        List<Conge> congesValides = congeRepo.findByEtatAndDateDebutLessThanEqual(Etat.VALIDE, today);
        for (Conge conge : congesValides) {
            conge.setEtat(Etat.EN_COURS);
            congeRepo.save(conge);
        }

        // maj etat "EN_COURS" a "FINI" si dateFin est dans le passé
        List<Conge> congesEnCours = congeRepo.findByEtatAndDateFinLessThanEqual(Etat.EN_COURS, today);
        for (Conge conge : congesEnCours) {
            conge.setEtat(Etat.FINI);
            congeRepo.save(conge);
        }
    }

    private static void seedDatabase() {
        // créer deux admins
        Administrateur admin1 = new Administrateur(1L, "admin1@gmail.com", "nom_1", "prenom_1", LocalDate.of(2023, 5, 31), "password");
        Administrateur admin2 = new Administrateur(2L, "admin2@gmail.com", "nom_2", "prenom_2", LocalDate.of(2023, 5, 31), "password");
        adminRepo.save(admin1);
        adminRepo.save(admin2);

        // créer deux employes
        Employe emp1 = new Employe(1L, "emp1@gmail.com", "nom_1", "prenom_1", LocalDate.of(2023, 5, 31), "password");
        Employe emp2 = new Employe(2L, "emp2@gmail.com", "nom_2", "prenom_2", LocalDate.of(2023, 5, 31), "password");
        employeRepo.save(emp1);
        employeRepo.save(emp2);

        // créer et sauvegarder des conges pour les employes
        Conge conge1 = new Conge("Maladie", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 10), null, Etat.SOLLICITE, emp1);
        Conge conge2 = new Conge("Medical", LocalDate.of(2023, 7, 15), LocalDate.of(2023, 7, 20), null, Etat.VALIDE, emp2);

        Conge conge3 = new Conge("Personnel", LocalDate.of(2023, 8, 1), LocalDate.of(2023, 8, 5), null, Etat.REFUSE, emp1);
        Conge conge4 = new Conge("Vacances", LocalDate.of(2023, 9, 10), LocalDate.of(2023, 9, 20), null, Etat.SOLLICITE, emp2);

        Conge conge5 = new Conge("Personnel", LocalDate.now(), LocalDate.now().plusDays(1), null, Etat.VALIDE, emp1);
        Conge conge6 = new Conge("Vacances", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), null, Etat.FINI, emp2);

        Conge conge7 = new Conge("Vacances", LocalDate.of(2023, 9, 10), LocalDate.of(2023, 9, 20), null, Etat.ANNULE, emp1);

        congeRepo.save(conge1);
        congeRepo.save(conge2);
        congeRepo.save(conge3);
        congeRepo.save(conge4);
        congeRepo.save(conge5);
        congeRepo.save(conge6);
        congeRepo.save(conge7);
    }
}
