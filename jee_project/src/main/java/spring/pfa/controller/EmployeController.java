package spring.pfa.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import spring.pfa.model.Conge;
import spring.pfa.model.Employe;
import spring.pfa.model.Etat;
import spring.pfa.repository.CongeRepository;
import spring.pfa.repository.EmployeRepository;

@Controller
@RequestMapping(value = "/employe")
public class EmployeController {

	private final CongeRepository congeRepos;
	private final EmployeRepository employeRepos;

	@Autowired
	public EmployeController(CongeRepository congeRepos, EmployeRepository employeRepos) {
		this.congeRepos = congeRepos;
		this.employeRepos = employeRepos;
	}
	
	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logout(SessionStatus status, HttpSession session) {
	    status.setComplete();
	    session.invalidate();
	    return "redirect:/auth/login";
	}

	@RequestMapping(value = "/index")
	public String index(Model model,
	                    @RequestParam(name = "page", defaultValue = "0") int p,
	                    @RequestParam(name = "year", required = false) Integer year,
	                    @RequestParam(name = "etatRechercher", required = false) Etat etatRechercher,
	                    HttpSession session
	) {
	    Long employeId = (Long) session.getAttribute("connectedId");
	    String connectedType = (String) session.getAttribute("connectedType");
	    
	    if (employeId == null  || !"employee".equalsIgnoreCase(connectedType)) {
	        return "redirect:/auth/login";
	    }
	    
	    Employe employee = employeRepos.getById(employeId);

	    Page<Conge> conges;
	    if (year != null && etatRechercher != null) {
	        conges = congeRepos.findByEmployeeIdAndYearAndEtat(
	                employeId,
	                year,
	                etatRechercher,
	                PageRequest.of(p, 10)
	        );
	    } else if (year != null) {
	        conges = congeRepos.findByEmployeeIdAndYear(
	                employeId,
	                year,
	                PageRequest.of(p, 10)
	        );
	    } else if (etatRechercher != null) {
	        conges = congeRepos.findByEmployeeIdAndEtat(
	                employeId,
	                etatRechercher,
	                PageRequest.of(p, 10)
	        );
	    } else {
	        conges = congeRepos.findByEmployeeId(
	                employeId,
	                PageRequest.of(p, 10)
	        );
	    }

	    model.addAttribute("connectedId", employeId);
	    model.addAttribute("employee", employee);
	    model.addAttribute("conges", conges);
	    model.addAttribute("year", year);
	    model.addAttribute("etatRechercher", etatRechercher);
	    model.addAttribute("pageCourant", p);
	    return "employeeDashboard";
	}
	
	@RequestMapping(value = "/addConge")
	public String addConge(Model model,
	                    HttpSession session
	) {
		Long employeId = (Long) session.getAttribute("connectedId");
	    String connectedType = (String) session.getAttribute("connectedType");
	    
	    if (employeId == null  || !"employee".equalsIgnoreCase(connectedType)) {
	        return "redirect:/auth/login";
	    }
	    
	    Employe employee = employeRepos.getById(employeId);


	    model.addAttribute("connectedId", employeId);
	    model.addAttribute("employee", employee);
	    return "addConge";
	}
	
	
	@PostMapping("/createConge")
	public String createConge(@Valid Conge conge, HttpSession session, RedirectAttributes redirectAttributes) {
		Long employeId = (Long) session.getAttribute("connectedId");
	    String connectedType = (String) session.getAttribute("connectedType");
	    
	    if (employeId == null  || !"employee".equalsIgnoreCase(connectedType)) {
	        return "redirect:/auth/login";
	    }

	    Employe employe = employeRepos.findById(employeId).orElse(null);
	    if (employe == null) {
	        return "redirect:/error";
	    }
	    int congeRestant = employe.getCongeRestant();

	    long numberOfDaysRequested = ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;
	    if (numberOfDaysRequested > congeRestant) {
	        redirectAttributes.addFlashAttribute("alertMessage", "Vous n'avez pas assez de jours de congé restants pour demander ce congé.");
	        return "redirect:/employe/index";
	    }

	    conge.setEtat(Etat.SOLLICITE);
	    conge.setEmploye(employe);

	    congeRepos.save(conge);

	    return "redirect:/employe/index";
	}

    @GetMapping("/deleteConge")
    public String deleteConge(@RequestParam Long id, HttpSession session) {
    	Long employeId = (Long) session.getAttribute("connectedId");
	    String connectedType = (String) session.getAttribute("connectedType");
	    
	    if (employeId == null  || !"employee".equalsIgnoreCase(connectedType)) {
	        return "redirect:/auth/login";
	    }

        Optional<Conge> optionalConge = congeRepos.findById(id);
        if (optionalConge.isPresent()) {
            Conge conge = optionalConge.get();
            if (conge.getEmploye().getId().equals(employeId)) {
            	conge.setEtat(Etat.ANNULE);
            	congeRepos.save(conge);
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }

        return "redirect:/employe/index";
    }


//	@PostMapping(value = "/Valide")
//	public String validateConge(Model model, @Valid Conge conge, @RequestParam(name = "id") Long id,
//			BindingResult bindingResult) {
//		if (bindingResult.hasErrors())
//			return "formConge";
//		if (id == null || conge == null || conge.getDateDebut() == null || conge.getDateFin() == null) {
//			model.addAttribute("error", "paramètres invalides");
//			return "NoValidate";
//		}
//
//		Employe employe = employeRepos.findById(id).orElse(null);
//		long daysSinceEmployment = ChronoUnit.DAYS.between(employe.getDateEmbauchement(), LocalDate.now());
//		if (daysSinceEmployment < 1) {
//			model.addAttribute("error", "Vous n'etes pas employé depuis assez longtemps");
//			return "NoValidate";
//		}
//		
//		List<Conge> conges = congeRepos.findByEmploye(employe);
//		int totalCongeDays = totalConge(conges);
//
//		int requestedCongeDays = (int) ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin());
//		int totalRequestedDays = totalCongeDays + requestedCongeDays;
//
//		if (totalRequestedDays < 30) {
//			//admin lezem yamel el validation 
//			conge.setEtat(Etat.SOLLICITE);
//			model.addAttribute("conge", conge);
//			congeRepos.save(conge);
//			return "En Cours";
//		} else {
//			model.addAttribute("error", "Le nombre total de jours demandés dépasse la limite autorisée 30 jours ");
//			return "NoValidate";
//		}
//
//	}
//	@GetMapping(value="/NombreJourRestant")
//	public String NombreJourRestant(Model model,@RequestParam(name = "id") Long id){
//		Employe emp = employeRepos.getById(id);
//		List<Conge> conges = congeRepos.findByEmploye(emp);
//		int totalCongeDays = 30-totalConge(conges);
//		model.addAttribute("nombrejourrestant", totalCongeDays);
//		return "NombreJours";
//		
//	}
//	@RequestMapping(value = "/delete", method = RequestMethod.GET)
//	public String delete(Long id, int page, String motCle) {
//		Conge c=congeRepos.getById(id);
//		if(c.getEtat().equals("SOLLICITE")) {
//			congeRepos.deleteById(id);
//		}
//		return "redirect:index?page=" + page + "&motCle=" + motCle;
//	}
//	int totalConge(List<Conge> conges) {
//		return conges.stream().mapToInt(c -> {
//			LocalDate endDate = (c.getDateRepture() != null) ? c.getDateRepture() : c.getDateFin();
//			return (int) ChronoUnit.DAYS.between(c.getDateDebut(), endDate);
//		}).sum();
//		
//	}
}
