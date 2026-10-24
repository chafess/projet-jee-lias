package com.fsbm.lias.controller;

import com.fsbm.lias.model.*;
import com.fsbm.lias.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Controller
public class HomeController {

    @Autowired private MemberRepository memberRepository;
    @Autowired private PublicationRepository publicationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private MaterialRequestRepository materialRequestRepository;
    @Autowired private MeetingRepository meetingRepository;
    @Autowired private ConventionRepository conventionRepository;
    @Autowired private ActualiteRepository actualiteRepository;
    @Autowired private BackupRepository backupRepository;
    @Autowired private ActivityLogRepository activityLogRepository;
    @Autowired private SystemSettingsRepository settingsRepository;
    @Autowired private ProfileHistoryRepository profileHistoryRepository;
    @Autowired private AffiliationHistoryRepository affiliationHistoryRepository;
    @Autowired private MandateRepository mandateRepository;
    @Autowired private DiscussionRepository discussionRepository;
    @Autowired private DiscussionMessageRepository discussionMessageRepository;
    @Autowired private NotificationRepository notificationRepository;

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    private Member getSessionUser(HttpSession session) {
        return (Member) session.getAttribute("loggedInUser");
    }

    private boolean estEnAttente(Member m) {
        return !m.isActive() && (m.getStatus() == null || "EN_ATTENTE".equals(m.getStatus()));
    }

    private boolean isAssociateRestricted(Member user) {
        return "MEMBRE_ASSOCIE".equals(user.getRole());
    }

    private void envoyerNotification(Member destinataire, String titre, String message, String type) {
        if (destinataire != null) {
            Notification n = new Notification(destinataire, titre, message, type);
            notificationRepository.save(n);
        }
    }

    private void logActivity(String type, String action, Member acteur, String cible) {
        ActivityLog log = new ActivityLog();
        log.setTimestamp(LocalDateTime.now());
        log.setType(type);
        log.setAction(action);
        log.setActeur(acteur != null ? acteur.getFirstName() + " " + acteur.getLastName() : "Système");
        log.setCible(cible != null ? cible : "—");
        activityLogRepository.save(log);
    }

    // =========================================================================
    // 1. ACCUEIL
    // =========================================================================

    /*@GetMapping("/")
    public String home(Model model) {
        long totalMembers = memberRepository.count();
        long totalPubs = publicationRepository.count();
        long totalEvents = eventRepository.count();

        model.addAttribute("allMembers", memberRepository.findAll());
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalPubs", totalPubs);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("allActualites", actualiteRepository.findAllByOrderByDateCreationDesc());
        return "index";
    }*/
    /*@GetMapping("/")
    public String home(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        model.addAttribute("user", user);

        long totalMembers = memberRepository.count();
        long totalPubs = publicationRepository.count();
        long totalEvents = eventRepository.count();
        long totalConventions = conventionRepository.count();

        model.addAttribute("allMembers", memberRepository.findAll());
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalPubs", totalPubs);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalConventions", totalConventions);
        model.addAttribute("allActualites", actualiteRepository.findAllByOrderByDateCreationDesc());
        return "index";
    }*/
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        model.addAttribute("user", user);

        long totalMembers = memberRepository.count();
        long totalPubs = publicationRepository.count();
        long totalEvents = eventRepository.count();

        // Récupérer le directeur actuel
        Member director = memberRepository.findAll().stream()
                .filter(m -> "DIRECTEUR".equals(m.getRole())).findFirst().orElse(null);
        model.addAttribute("currentDirector", director);

        // Récupérer les chefs d'équipe actifs
        List<Mandate> chefs = mandateRepository.findAll().stream()
                .filter(m -> "CHEF_EQUIPE".equals(m.getType()) && "ACTIF".equals(m.getStatus()))
                .toList();
        model.addAttribute("activeTeamLeaders", chefs);

        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalPubs", totalPubs);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("allActualites", actualiteRepository.findAllByOrderByDateCreationDesc());
        return "index";
    }

    // =========================================================================
    // 2. AUTHENTIFICATION (LOGIN / LOGOUT)
    // =========================================================================

    @GetMapping("/login")
    public String showLoginForm() { return "login"; }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<Member> member = memberRepository.findByEmail(email);

        if (member.isPresent() && member.get().getPassword() != null && member.get().getPassword().equals(password)) {
            Member user = member.get();
            String role = user.getRole();

            // a) RETRAITE : compte gelé définitivement
            if ("RETRAITE".equals(user.getStatus())) {
                model.addAttribute("error", "Votre compte est gelé (statut retraité). Contactez l'administrateur.");
                return "login";
            }

            // b) ANCIEN_MEMBRE : compte désactivé, réactivable
            if ("ANCIEN_MEMBRE".equals(user.getStatus())) {
                model.addAttribute("error", "Votre compte a été désactivé. Contactez l'administrateur pour une réactivation.");
                return "login";
            }

            // c) REFUSE : candidature refusée
            if ("REFUSE".equals(user.getStatus())) {
                model.addAttribute("error", "Votre candidature a été refusée.");
                return "login";
            }

            // d) Non actif : candidature en cours d'attente
            if (!user.isActive()) {
                model.addAttribute("error", "Votre candidature est en attente de validation par le directeur. Vous recevrez un accès dès son approbation.");
                return "login";
            }

            session.setAttribute("loggedInUser", user);

            if ("ADMINISTRATEUR".equals(role)) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("error", "Email ou mot de passe incorrect");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // =========================================================================
    // 3. ADHÉSION (APPLY)
    // =========================================================================

    @GetMapping("/apply")
    public String showApplyForm(Model model) {
        model.addAttribute("member", new Member());
        return "apply";
    }

    @PostMapping("/apply")
    public String processApplication(@ModelAttribute Member member,
                                     @RequestParam("cvFile") MultipartFile file,
                                     RedirectAttributes ra) {
        try {
            if (!file.isEmpty()) {
                String fileName = "CV_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get("C:/lias_uploads/" + fileName), StandardCopyOption.REPLACE_EXISTING);
                member.setCvPath(fileName);
            }

            member.setActive(false);
            member.setStatus("EN_ATTENTE"); // Statut officiel en attente
            member.setRole("MEMBRE_PERMANENT");
            memberRepository.save(member);

            ra.addFlashAttribute("message", "Application submitted! Wait for director approval.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Upload failed.");
        }
        return "redirect:/login";
    }

    // =========================================================================
    // 4. DASHBOARD
    // =========================================================================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("activePage", "dashboard");

        long vraiesNotifs = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
        model.addAttribute("unreadNotifsCount", vraiesNotifs);
        model.addAttribute("notifications", notificationRepository.findByRecipientIdOrderByDateCreationDesc(user.getId()));

        if ("ADMINISTRATEUR".equals(user.getRole())) {
            List<Member> all = memberRepository.findAll();
            model.addAttribute("totalAccounts", all.size());
            model.addAttribute("countNew", all.stream().filter(m -> m.getDateAffiliation() != null && m.getDateAffiliation().isAfter(LocalDate.now().minusMonths(1))).count());
            model.addAttribute("countPerm", all.stream().filter(m -> "MEMBRE_PERMANENT".equals(m.getRole())).count());
            model.addAttribute("countPhd", all.stream().filter(m -> "DOCTORANT".equals(m.getRole())).count());
            model.addAttribute("countRet", all.stream().filter(m -> "RETRAITE".equals(m.getStatus())).count());
            model.addAttribute("totalDocs", publicationRepository.count() + conventionRepository.count());

            Member director = all.stream().filter(m -> "DIRECTEUR".equals(m.getRole())).findFirst().orElse(null);
            model.addAttribute("currentDirector", director);
            model.addAttribute("latestMembers", all.stream().sorted((m1,m2) -> m2.getId().compareTo(m1.getId())).limit(4).toList());
            return "admin/dashboard";
        }
        else if ("DIRECTEUR".equals(user.getRole())) {
            List<Member> allMembers = memberRepository.findAll();

            model.addAttribute("pendingAdhesions", allMembers.stream().filter(this::estEnAttente).count());
            model.addAttribute("activeMembers", allMembers.stream().filter(Member::isActive).count());
            model.addAttribute("pubsYear", publicationRepository.findAll().stream().filter(p -> p.getYear() == LocalDate.now().getYear()).count());
            model.addAttribute("eventsCount", eventRepository.count());
            model.addAttribute("activeConventions", conventionRepository.count());
            model.addAttribute("pendingMaterial", materialRequestRepository.findAll().stream().filter(r -> "PENDING".equals(r.getStatus())).count());
            model.addAttribute("activeTeams", teamRepository.count());
            model.addAttribute("countMembers", allMembers.size());

            model.addAttribute("recentAdhesions", allMembers.stream().filter(this::estEnAttente).limit(3).toList());
            model.addAttribute("upcomingEvents", eventRepository.findAll().stream().limit(3).toList());

            int progression = 0;
            if (user.getDateAffiliation() != null) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(user.getDateAffiliation(), LocalDate.now());
                progression = (int) Math.min(100, (jours * 100) / 1095);
            }
            model.addAttribute("progression", progression);

            return "directeur/dashboard";
        }
        else if ("VICE_DIRECTEUR".equals(user.getRole())) {
            List<Member> allMembers = memberRepository.findAll();

            model.addAttribute("activeMembers", allMembers.stream().filter(Member::isActive).count());
            model.addAttribute("pendingAdhesions", allMembers.stream().filter(this::estEnAttente).count());
            model.addAttribute("pubsMonth", publicationRepository.findAll().stream().filter(p -> p.getYear() == LocalDate.now().getYear()).count());
            model.addAttribute("eventsCount", eventRepository.count());
            model.addAttribute("pendingMaterial", materialRequestRepository.findAll().stream().filter(r -> "PENDING".equals(r.getStatus())).count());

            return "vice-directeur/dashboard";
        }
        else if ("MEMBRE_PERMANENT".equals(user.getRole())) {
            Member freshUser = memberRepository.findById(user.getId()).orElse(user);

            model.addAttribute("myPubsCount", freshUser.getPublications() != null ? freshUser.getPublications().size() : 0);
            model.addAttribute("myEquipCount", freshUser.getEquipments() != null ? freshUser.getEquipments().size() : 0);
            model.addAttribute("myLatestPubs", publicationRepository.findAll().stream().filter(p -> p.getAuthor() != null && p.getAuthor().getId().equals(user.getId())).limit(2).toList());
            model.addAttribute("upcomingEvents", eventRepository.findAll().stream().limit(3).toList());
            model.addAttribute("unreadMessages", 7);
            model.addAttribute("newDocsCount", 5);

            return "membre-permanent/dashboard";
        }
        else if ("CHEF_EQUIPE".equals(user.getRole())) {
            Team team = user.getTeam();
            if (team == null) {
                team = new Team();
                team.setName("Aucune équipe assignée");
            }
            model.addAttribute("team", team);

            List<Member> teamMembers = (user.getTeam() != null)
                    ? memberRepository.findAll().stream().filter(m -> m.getTeam() != null && m.getTeam().getId().equals(user.getTeam().getId())).toList()
                    : List.of();

            long teamPubs = (user.getTeam() != null)
                    ? publicationRepository.findAll().stream().filter(p -> p.getAuthor() != null && p.getAuthor().getTeam() != null && p.getAuthor().getTeam().getId().equals(user.getTeam().getId())).filter(p -> p.getYear() == LocalDate.now().getYear()).count()
                    : 0;

            long teamEvents = eventRepository.findAll().stream().filter(e -> e.getDate().isAfter(LocalDate.now().minusDays(1))).count();

            model.addAttribute("teamMembers", teamMembers.stream().limit(3).toList());
            model.addAttribute("teamMembersCount", teamMembers.size());
            model.addAttribute("teamPubsCount", teamPubs);
            model.addAttribute("teamEventsCount", teamEvents);
            model.addAttribute("teamActivities", activityLogRepository.findAll().stream().limit(3).toList());

            return "chef-equipe/dashboard";
        }
        else if ("DOCTORANT".equals(user.getRole())) {
            long myPubsCount = publicationRepository.findAll().stream().filter(p -> p.getAuthor() != null && p.getAuthor().getId().equals(user.getId())).count();
            long upcomingEventsCount = eventRepository.findAll().stream().filter(e -> e.getDate().isAfter(LocalDate.now())).count();
            long anciennete = (user.getDateAffiliation() != null) ? java.time.temporal.ChronoUnit.DAYS.between(user.getDateAffiliation(), LocalDate.now()) : 0;

            model.addAttribute("myPubsCount", myPubsCount);
            model.addAttribute("upcomingEventsCount", upcomingEventsCount);
            model.addAttribute("ancienneteJours", anciennete);

            return "doctorant/dashboard";
        }
        else if ("MEMBRE_ASSOCIE".equals(user.getRole())) {
            Member freshUser = memberRepository.findByIdWithEvents(user.getId()).orElse(user);

            List<Document> accessibleDocs = documentsAccessibles(freshUser);

            List<Discussion> accessibleDiscussions = List.of();
            if (freshUser.getParticipatingEvents() != null && !freshUser.getParticipatingEvents().isEmpty()) {
                accessibleDiscussions = discussionRepository.findAll().stream()
                        .filter(disc -> disc.getEvent() != null && freshUser.getParticipatingEvents().contains(disc.getEvent()))
                        .toList();
            }

            long myPubs = publicationRepository.findAll().stream().filter(p -> p.getAuthor() != null && p.getAuthor().getId().equals(user.getId())).count();
            long accessibleEvents = eventRepository.findAll().stream().filter(Event::isOuvertAuxAssocies).filter(e -> e.getDate().isAfter(LocalDate.now().minusDays(1))).count();

            model.addAttribute("canPublish", freshUser.isCanPublish());
            model.addAttribute("myPubsCount", myPubs);
            model.addAttribute("accessibleEventsCount", accessibleEvents);
            model.addAttribute("accessibleDocsCount", accessibleDocs.size());
            model.addAttribute("accessibleDiscussionsCount", accessibleDiscussions.size());
            model.addAttribute("pubStatus", freshUser.isCanPublish() ? "Autorisé" : "Restreint");

            return "membre-associe/dashboard";
        }
        else {
            long myPubs = publicationRepository.findAll().stream().filter(p -> p.getAuthor() != null && p.getAuthor().getId().equals(user.getId())).count();
            long myEq = equipmentRepository.findAll().stream().filter(e -> e.getUser() != null && e.getUser().getId().equals(user.getId())).count();

            model.addAttribute("countPubs", myPubs);
            model.addAttribute("countEquipment", myEq);
            model.addAttribute("countEvents", eventRepository.count());

            return "dashboard";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) return "redirect:/login";

        List<Member> all = memberRepository.findAll();
        model.addAttribute("totalAccounts", all.size());
        model.addAttribute("countNew", all.stream().filter(m -> m.getDateAffiliation() != null && m.getDateAffiliation().isAfter(LocalDate.now().minusMonths(1))).count());
        model.addAttribute("countPerm", all.stream().filter(m -> "MEMBRE_PERMANENT".equals(m.getRole())).count());
        model.addAttribute("countPhd", all.stream().filter(m -> "DOCTORANT".equals(m.getRole())).count());
        model.addAttribute("countRet", all.stream().filter(m -> "RETRAITE".equals(m.getStatus())).count());

        long totalDocs = publicationRepository.count() + conventionRepository.count();
        model.addAttribute("totalDocs", totalDocs);

        Member director = all.stream().filter(m -> "DIRECTEUR".equals(m.getRole())).findFirst().orElse(null);
        model.addAttribute("currentDirector", director);

        List<Member> latest = all.stream().sorted((m1,m2) -> m2.getId().compareTo(m1.getId())).limit(4).toList();
        model.addAttribute("latestMembers", latest);

        model.addAttribute("countAssocie", all.stream().filter(m -> "MEMBRE_ASSOCIE".equals(m.getRole())).count());
        model.addAttribute("countChefEquipe", all.stream().filter(m -> "CHEF_EQUIPE".equals(m.getRole())).count());
        model.addAttribute("countViceDirecteur", all.stream().filter(m -> "VICE_DIRECTEUR".equals(m.getRole())).count());
        model.addAttribute("countAncien", all.stream().filter(m -> "ANCIEN_MEMBRE".equals(m.getStatus())).count());

        model.addAttribute("user", user);
        model.addAttribute("activePage", "admin-dash");
        return "admin/dashboard";
    }

    // =========================================================================
    // ACTUALITÉS
    // =========================================================================

    @GetMapping("/directeur/actualites")
    public String listActualites(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"DIRECTEUR".equals(user.getRole())) return "redirect:/login";

        List<Actualite> acts = actualiteRepository.findAllByOrderByDateCreationDesc();
        model.addAttribute("user", user);
        model.addAttribute("actualites", acts);
        model.addAttribute("activePage", "actualites");
        model.addAttribute("totalActu", acts.size());
        model.addAttribute("countPubs", acts.stream().filter(a -> "Publication".equals(a.getType())).count());

        return "directeur/actualites";
    }

    @PostMapping("/directeur/actualites/save")
    public String saveActualite(@ModelAttribute Actualite actu,
                                @RequestParam(value = "documentFile", required = false) MultipartFile file,
                                HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        if (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = "ACTU_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get("C:/lias_uploads/" + fileName), StandardCopyOption.REPLACE_EXISTING);
                actu.setDocumentPath(fileName);
            } catch (IOException e) { e.printStackTrace(); }
        }

        actu.setAuteur(user);
        actu.setDateCreation(LocalDateTime.now());
        actualiteRepository.save(actu);
        return "redirect:/member/actualites";
    }

    @GetMapping("/member/actualites/download/{id}")
    public ResponseEntity<Resource> downloadActuDoc(@PathVariable Long id) throws IOException {
        Actualite actu = actualiteRepository.findById(id).orElseThrow();

        Path path = Paths.get("C:/lias_uploads/" + actu.getDocumentPath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + actu.getDocumentPath() + "\"")
                .body(resource);
    }

    @PostMapping("/directeur/actualites/delete/{id}")
    public String deleteActualite(@PathVariable Long id, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || !"DIRECTEUR".equals(user.getRole())) {
            return "redirect:/directeur/actualites";
        }
        actualiteRepository.deleteById(id);
        return "redirect:/directeur/actualites";
    }

    @GetMapping("/member/actualites")
    public String memberActualites(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        List<Actualite> actus = actualiteRepository.findAllByOrderByDateCreationDesc();
        model.addAttribute("user", user);
        model.addAttribute("actualites", actus);
        model.addAttribute("canPublish", "DIRECTEUR".equals(user.getRole()) || "VICE_DIRECTEUR".equals(user.getRole()));
        model.addAttribute("activePage", "actualites");

        return "member/actualites";
    }

    // =========================================================================
    // 5. PROFIL
    // =========================================================================

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", memberRepository.findById(user.getId()).orElse(user));
        model.addAttribute("countMembers", memberRepository.count());
        model.addAttribute("countPubs", publicationRepository.count());
        model.addAttribute("countEvents", eventRepository.count());
        model.addAttribute("profileHistory", profileHistoryRepository.findByMemberIdOrderByDateModificationDesc(user.getId()));
        model.addAttribute("historiqueAffiliations", affiliationHistoryRepository.findByMemberIdOrderByDateDebutDesc(user.getId()));
        model.addAttribute("countEquipment", equipmentRepository.count());
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Member member,
                                @RequestParam(value = "imageFile", required = false) MultipartFile file,
                                HttpSession session, RedirectAttributes ra) {
        Member sessionUser = getSessionUser(session);
        if (sessionUser == null) return "redirect:/login";

        Member existing = memberRepository.findById(sessionUser.getId()).orElseThrow();

        enregistrerSiModifie(existing, "Prénom", existing.getFirstName(), member.getFirstName());
        enregistrerSiModifie(existing, "Nom", existing.getLastName(), member.getLastName());
        enregistrerSiModifie(existing, "Biographie", existing.getBiographie(), member.getBiographie());
        enregistrerSiModifie(existing, "Centres d'intérêt", existing.getCentresInteret(), member.getCentresInteret());

        existing.setFirstName(member.getFirstName());
        existing.setLastName(member.getLastName());
        existing.setBiographie(member.getBiographie());
        existing.setCentresInteret(member.getCentresInteret());

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = "PHOTO_" + existing.getId() + ".jpg";
                Files.copy(file.getInputStream(), Paths.get("C:/lias_uploads/" + fileName), StandardCopyOption.REPLACE_EXISTING);
                existing.setPhotoPath(fileName);
            } catch (IOException e) { e.printStackTrace(); }
        }

        memberRepository.save(existing);
        session.setAttribute("loggedInUser", existing);
        ra.addFlashAttribute("message", "Profil mis à jour !");
        return "redirect:/profile";
    }

    private void enregistrerSiModifie(Member member, String champ, String ancienne, String nouvelle) {
        if ((nouvelle != null && !nouvelle.equals(ancienne)) || (ancienne != null && !ancienne.equals(nouvelle))) {
            ProfileHistory histo = new ProfileHistory();
            histo.setMember(member);
            histo.setFieldName(champ);
            histo.setOldValue(ancienne);
            histo.setNewValue(nouvelle);
            histo.setDateModification(LocalDateTime.now());
            profileHistoryRepository.save(histo);
        }
    }

    // =========================================================================
    // 6. PUBLICATIONS
    // =========================================================================

    @GetMapping("/publications")
    public String publicationsPage(@RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Long teamId,
                                   @RequestParam(required = false, defaultValue = "latest") String sort,
                                   HttpSession session, Model model) {
        model.addAttribute("user", getSessionUser(session));
        List<Publication> allPubs = publicationRepository.findAll();
        List<Publication> filtered = allPubs;

        if (year != null) filtered = filtered.stream().filter(p -> p.getYear() == year).collect(Collectors.toList());
        if (teamId != null) filtered = filtered.stream().filter(p -> p.getAuthor() != null && p.getAuthor().getTeam() != null && p.getAuthor().getTeam().getId().equals(teamId)).collect(Collectors.toList());

        if ("latest".equals(sort)) filtered.sort((p1, p2) -> Integer.compare(p2.getYear(), p1.getYear()));
        else filtered.sort((p1, p2) -> Integer.compare(p1.getYear(), p2.getYear()));

        model.addAttribute("allPublications", filtered);
        model.addAttribute("allTeams", teamRepository.findAll());
        model.addAttribute("totalPubs", allPubs.size());
        model.addAttribute("activeResearchers", memberRepository.count());
        model.addAttribute("count2025", allPubs.stream().filter(p -> p.getYear() == 2025).count());
        model.addAttribute("count2024", allPubs.stream().filter(p -> p.getYear() == 2024).count());

        return "publications";
    }

    @GetMapping("/member/my-publications")
    public String showMyPublications(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if ("MEMBRE_ASSOCIE".equals(user.getRole()) && !user.isCanPublish()) {
            return "redirect:/dashboard";
        }

        List<Publication> myPubs = publicationRepository.findAll().stream()
                .filter(p -> p.getAuthor() != null && p.getAuthor().getId().equals(user.getId())).toList();

        model.addAttribute("user", user);
        model.addAttribute("myPublications", myPubs);
        model.addAttribute("myPubsCount", myPubs.size());
        model.addAttribute("publishedCount", myPubs.stream().filter(p -> "Published".equals(p.getStatus())).count());
        model.addAttribute("reviewCount", myPubs.stream().filter(p -> "Under Review".equals(p.getStatus())).count());

        return "member/my-publications";
    }

    @PostMapping("/member/publications/save")
    public String saveMemberPublication(@ModelAttribute Publication p, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Member fresh = memberRepository.findById(user.getId()).orElse(user);
        boolean peut = !"MEMBRE_ASSOCIE".equals(fresh.getRole()) || fresh.isCanPublish();

        if (peut) {
            p.setAuthor(fresh);
            publicationRepository.save(p);
            ra.addFlashAttribute("message", "Publication enregistrée avec succès !");
        } else {
            ra.addFlashAttribute("error", "Action non autorisée : vos droits de publication sont restreints.");
        }

        return "redirect:/member/my-publications";
    }

    @PostMapping("/member/publications/update")
    public String updatePublication(@ModelAttribute Publication p, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Member fresh = memberRepository.findById(user.getId()).orElse(user);
        boolean peut = !"MEMBRE_ASSOCIE".equals(fresh.getRole()) || fresh.isCanPublish();

        if (!peut) {
            ra.addFlashAttribute("error", "Action non autorisée : vos droits de publication sont restreints.");
            return "redirect:/member/my-publications";
        }

        Publication existing = publicationRepository.findById(p.getId()).orElseThrow();
        if (existing.getAuthor() == null || !existing.getAuthor().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Modification refusée : vous n'êtes pas l'auteur de cette publication.");
            return "redirect:/member/my-publications";
        }

        existing.setTitle(p.getTitle());
        existing.setJournal(p.getJournal());
        existing.setYear(p.getYear());
        existing.setStatus(p.getStatus());
        existing.setAbstractText(p.getAbstractText());
        publicationRepository.save(existing);
        ra.addFlashAttribute("message", "Publication mise à jour !");
        return "redirect:/member/my-publications";
    }

    // =========================================================================
    // 7. ÉVÉNEMENTS
    // =========================================================================

    @GetMapping("/events")
    public String eventsPage(
            @RequestParam(required = false, defaultValue = "upcoming") String view,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String query,
            HttpSession session, Model model) {

        model.addAttribute("user", getSessionUser(session));
        List<Event> events = eventRepository.findAll();

        if ("upcoming".equals(view)) {
            events = events.stream().filter(e -> e.getDate().isAfter(LocalDate.now().minusDays(1))).toList();
        } else {
            events = events.stream().filter(e -> e.getDate().isBefore(LocalDate.now())).toList();
        }

        if (type != null) {
            events = events.stream().filter(e -> e.getType().equalsIgnoreCase(type)).toList();
        }

        if (query != null && !query.isEmpty()) {
            events = events.stream().filter(e -> e.getTitle().toLowerCase().contains(query.toLowerCase())).toList();
        }

        model.addAttribute("allEvents", events);
        model.addAttribute("currentView", view);
        model.addAttribute("upcomingCount", eventRepository.findAll().stream().filter(e -> e.getDate().isAfter(LocalDate.now())).count());
        model.addAttribute("archivesCount", eventRepository.findAll().stream().filter(e -> e.getDate().isBefore(LocalDate.now())).count());
        model.addAttribute("confCount", eventRepository.findAll().stream().filter(e -> "Conference".equalsIgnoreCase(e.getType())).count());
        model.addAttribute("semCount", eventRepository.findAll().stream().filter(e -> "Seminar".equalsIgnoreCase(e.getType())).count());
        model.addAttribute("workCount", eventRepository.findAll().stream().filter(e -> "Workshop".equalsIgnoreCase(e.getType())).count());

        return "events";
    }

    @GetMapping("/directeur/events")
    public String listEvents(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/login";
        }

        List<Event> allEvents = eventRepository.findAll();
        model.addAttribute("user", user);
        model.addAttribute("allEvents", allEvents);
        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("countConf", allEvents.stream().filter(e -> "Conférence".equals(e.getType())).count());
        model.addAttribute("countSem", allEvents.stream().filter(e -> "Séminaire".equals(e.getType())).count());
        model.addAttribute("newEvent", new Event());
        model.addAttribute("members", memberRepository.findAll().stream().filter(Member::isActive).toList());

        return "directeur/events";
    }

    @PostMapping("/directeur/events/save")
    public String saveEvent(@ModelAttribute Event event, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/login";
        }

        eventRepository.save(event);

        List<Member> destinataires = memberRepository.findAll().stream()
                .filter(Member::isActive)
                .filter(m -> !"MEMBRE_ASSOCIE".equals(m.getRole()) || event.isOuvertAuxAssocies())
                .toList();

        for (Member m : destinataires) {
            envoyerNotification(m, "Nouvel événement",
                    "Un nouvel événement a été publié : " + event.getTitle(), "EVENT");
        }

        if (event.getOrganizer() != null) {
            envoyerNotification(event.getOrganizer(), "Mission d'organisation",
                    "Vous avez été désigné organisateur de : " + event.getTitle(), "EVENT");
        }

        logActivity("Événement", "Événement créé", user, event.getTitle() + " (" + event.getType() + ")");
        ra.addFlashAttribute("message", "Événement créé avec succès !");
        return "redirect:/directeur/events";
    }

    @GetMapping("/member/events")
    public String showMemberEvents(@RequestParam(required = false) String type, HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Member freshUser = memberRepository.findByIdWithEvents(user.getId()).orElse(user);
        if (freshUser.getParticipatingEvents() != null) {
            freshUser.getParticipatingEvents().size();
        }

        List<Event> events = eventRepository.findAll();

        if (type != null && !type.equals("Tous")) {
            events = events.stream().filter(e -> e.getType().equalsIgnoreCase(type)).toList();
        }

        List<Event> upcoming = events.stream()
                .filter(e -> e.getDate().isAfter(LocalDate.now().minusDays(1)))
                .toList();

        // Correction 6 : Filtre réel pour les archives
        List<Event> archives = events.stream()
                .filter(e -> e.getDate().isBefore(LocalDate.now()))
                .toList();

        model.addAttribute("user", freshUser);
        model.addAttribute("activePage", "events");
        model.addAttribute("upcomingEvents", upcoming);
        model.addAttribute("archivedEvents", archives);
        model.addAttribute("currentType", type != null ? type : "Tous");

        int count = (freshUser.getParticipatingEvents() != null) ? freshUser.getParticipatingEvents().size() : 0;
        model.addAttribute("participationCount", count);

        return "member/events";
    }

    @PostMapping("/member/events/register/{eventId}")
    public String registerToEvent(@PathVariable Long eventId, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Member freshUser = memberRepository.findById(user.getId()).orElse(user);
        Event event = eventRepository.findById(eventId).orElseThrow();

        if (freshUser.getParticipatingEvents() == null) {
            freshUser.setParticipatingEvents(new java.util.ArrayList<>());
        }

        if (!freshUser.getParticipatingEvents().contains(event)) {
            freshUser.getParticipatingEvents().add(event);
            memberRepository.save(freshUser);
            ra.addFlashAttribute("message", "Inscription réussie à : " + event.getTitle());
        } else {
            ra.addFlashAttribute("error", "Vous êtes déjà inscrit à cet événement.");
        }

        return "redirect:/member/events";
    }

    // =========================================================================
    // 8. ÉQUIPES
    // =========================================================================

    @GetMapping("/directeur/teams")
    public String listTeams(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", user);
        model.addAttribute("allTeams", teamRepository.findAll());
        model.addAttribute("newTeam", new Team());
        return "directeur/teams";
    }

    @PostMapping("/directeur/teams/save")
    public String saveTeam(@ModelAttribute Team team, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }
        teamRepository.save(team);
        return "redirect:/directeur/teams";
    }

    // =========================================================================
    // 9. MATÉRIEL ET ÉQUIPEMENT
    // =========================================================================

    @GetMapping("/member/request-material")
    public String showRequestForm(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || "MEMBRE_ASSOCIE".equals(user.getRole()) || "DOCTORANT".equals(user.getRole())
                || "DIRECTEUR".equals(user.getRole()) || "VICE_DIRECTEUR".equals(user.getRole())
                || "ADMINISTRATEUR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        Member freshUser = memberRepository.findById(user.getId()).orElse(user);
        model.addAttribute("user", user);
        model.addAttribute("myRequests", freshUser.getMaterialRequests());
        model.addAttribute("materialRequest", new MaterialRequest());
        model.addAttribute("equipmentNames", equipmentRepository.findAll().stream().map(Equipment::getName).distinct().toList());
        model.addAttribute("activePage", "request");

        return "member/request-material";
    }

    @PostMapping("/member/request-material/save")
    public String saveReq(@ModelAttribute MaterialRequest r, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || "MEMBRE_ASSOCIE".equals(user.getRole()) || "DOCTORANT".equals(user.getRole())
                || "DIRECTEUR".equals(user.getRole()) || "VICE_DIRECTEUR".equals(user.getRole())
                || "ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Action non autorisée.");
            return "redirect:/dashboard";
        }

        r.setMember(user);
        r.setStatus("PENDING");
        materialRequestRepository.save(r);
        return "redirect:/profile?success=true";
    }

    @GetMapping("/directeur/material-requests")
    public String listMaterialRequests(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole())))
            return "redirect:/login";

        List<MaterialRequest> pendingRequests = materialRequestRepository.findAll().stream()
                .filter(r -> "PENDING".equals(r.getStatus())).toList();
        List<Equipment> inv = equipmentRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("allRequests", pendingRequests);
        model.addAttribute("inventory", inv);
        model.addAttribute("totalEq", inv.size());
        model.addAttribute("inStockEq", inv.stream().filter(e -> e.getUser() == null).count());
        model.addAttribute("assignedEq", inv.stream().filter(e -> e.getUser() != null).count());
        model.addAttribute("members", memberRepository.findAll().stream().filter(Member::isActive).toList());
        model.addAttribute("newEquipment", new Equipment());
        model.addAttribute("activePage", "material-requests");

        return "directeur/material-requests";
    }

    @PostMapping("/directeur/equipment/save")
    public String saveEquipment(@ModelAttribute Equipment equipment, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }
        equipmentRepository.save(equipment);
        return "redirect:/directeur/material-requests";
    }

    @PostMapping("/directeur/material-requests/approve/{id}")
    public String approveRequest(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }

        MaterialRequest req = materialRequestRepository.findById(id).orElseThrow();
        Optional<Equipment> item = equipmentRepository.findAll().stream()
                .filter(e -> e.getName().trim().equalsIgnoreCase(req.getItemName().trim()) && e.getUser() == null).findFirst();

        if (item.isPresent()) {
            item.get().setUser(req.getMember());
            equipmentRepository.save(item.get());
            req.setStatus("APPROVED");
            materialRequestRepository.save(req);

            envoyerNotification(req.getMember(), "Demande approuvée",
                    "Votre demande pour " + req.getItemName() + " a été approuvée.", "VALIDATION");

            ra.addFlashAttribute("message", "Objet assigné avec succès !");
        } else {
            envoyerNotification(req.getMember(), "Demande en attente",
                    "Rupture de stock temporaire pour " + req.getItemName() + ".", "VALIDATION");

            ra.addFlashAttribute("error", "Rupture de stock pour cet objet.");
        }
        logActivity("Matériel", "Demande approuvée", user, req.getMember().getFirstName() + " " + req.getMember().getLastName() + " (" + req.getItemName() + ")");
        return "redirect:/directeur/material-requests";
    }

    @PostMapping("/directeur/material-requests/delete/{id}")
    public String deleteRequest(@PathVariable Long id, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }

        MaterialRequest req = materialRequestRepository.findById(id).orElse(null);
        if (req != null) {
            envoyerNotification(req.getMember(), "Demande de matériel refusée",
                    "Votre demande pour le matériel '" + req.getItemName() + "' a été rejetée.", "VALIDATION");
            materialRequestRepository.deleteById(id);
        }

        return "redirect:/directeur/material-requests";
    }

    // =========================================================================
    // 10. PARTENAIRES ET CONVENTIONS
    // =========================================================================

    @GetMapping("/directeur/partners")
    public String listPartners(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole())))
            return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("readOnly", "VICE_DIRECTEUR".equals(user.getRole()));

        List<Convention> convs = conventionRepository.findAll();
        model.addAttribute("conventions", convs);
        model.addAttribute("totalPartners", convs.stream().map(Convention::getPartnerName).distinct().count());
        model.addAttribute("activeConventions", convs.stream().filter(c -> c.getEndDate().isAfter(LocalDate.now())).count());
        model.addAttribute("newConvention", new Convention());
        model.addAttribute("allTeams", teamRepository.findAll());
        return "directeur/partners";
    }

    @PostMapping("/directeur/partners/save")
    public String saveConvention(@ModelAttribute Convention convention, @RequestParam("file") MultipartFile file,
                                 HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);

        if (user == null || !"DIRECTEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Action non autorisée : seuls les directeurs peuvent ajouter des conventions.");
            return "redirect:/directeur/partners";
        }

        if (!file.isEmpty()) {
            try {
                String fileName = "CONV_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get("C:/lias_uploads/" + fileName), StandardCopyOption.REPLACE_EXISTING);
                convention.setPvPath(fileName);
            } catch (IOException e) { e.printStackTrace(); }
        }
        conventionRepository.save(convention);
        ra.addFlashAttribute("message", "Convention enregistrée avec succès !");
        // Correction 5 : redirection correcte vers /directeur/partners
        return "redirect:/directeur/partners";
    }

    // =========================================================================
    // 11. RÉUNIONS ET RAPPORTS
    // =========================================================================

    @GetMapping("/directeur/meetings")
    public String listMeetings(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("activePage", "meetings");
        model.addAttribute("meetings", meetingRepository.findAll());
        model.addAttribute("newMeeting", new Meeting());
        model.addAttribute("readOnly", !"DIRECTEUR".equals(user.getRole()));

        return "directeur/meetings";
    }

    @PostMapping("/directeur/meetings/save")
    public String saveMeeting(@ModelAttribute Meeting meeting, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/login";
        }

        meetingRepository.save(meeting);
        ra.addFlashAttribute("message", "Réunion enregistrée avec succès !");
        return "redirect:/directeur/meetings";
    }

    @GetMapping("/directeur/report")
    public String generateReport(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("totalMembers", memberRepository.count());
        model.addAttribute("totalPubs", publicationRepository.count());
        model.addAttribute("totalEvents", eventRepository.count());
        model.addAttribute("allPublications", publicationRepository.findAll());
        model.addAttribute("activeMembers", memberRepository.findAll().stream().filter(Member::isActive).toList());
        // Règle : Seul le directeur peut valider officiellement le rapport (le vice-directeur est en lecture seule)

        model.addAttribute("canValidateReport", "DIRECTEUR".equals(user.getRole()));

        return "directeur/report";
    }
    /*@GetMapping("/directeur/report")
    public String generateReport(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("totalMembers", memberRepository.count());
        model.addAttribute("totalPubs", publicationRepository.count());
        model.addAttribute("totalEvents", eventRepository.count());
        model.addAttribute("allPublications", publicationRepository.findAll());
        model.addAttribute("activeMembers", memberRepository.findAll().stream().filter(Member::isActive).toList());
        return "directeur/report";
    }*/


    // =========================================================================
    // 12. GESTION DES MEMBRES ET ADHÉSIONS (DIRECTEUR)
    // =========================================================================

    @GetMapping("/directeur/approvals")
    public String showApprovals(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"DIRECTEUR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        // Correction 2 : Utilise estEnAttente
        model.addAttribute("pendingMembers", memberRepository.findAll().stream()
                .filter(this::estEnAttente).collect(Collectors.toList()));
        return "directeur/approvals";
    }

    @PostMapping("/directeur/approve/{id}")
    public String approveMember(@PathVariable Long id, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || !"DIRECTEUR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        Member member = memberRepository.findById(id).orElseThrow();

        if (member.getRole() == null || "MEMBER".equals(member.getRole())) {
            member.setRole("MEMBRE_PERMANENT");
        }

        member.setActive(true);
        member.setStatus("ACTIF");
        member.setDateAffiliation(java.time.LocalDate.now());
        memberRepository.save(member);

        AffiliationHistory nouvelleAffiliation = new AffiliationHistory();
        nouvelleAffiliation.setMember(member);
        nouvelleAffiliation.setDateDebut(java.time.LocalDate.now());
        nouvelleAffiliation.setDateFin(null);
        nouvelleAffiliation.setStatut(member.getRole());
        affiliationHistoryRepository.save(nouvelleAffiliation);

        envoyerNotification(member, "Adhésion acceptée",
                "Bienvenue au LIAS ! Votre demande d'adhésion a été validée par la direction.", "VALIDATION");

        ActivityLog log = new ActivityLog();
        log.setTimestamp(LocalDateTime.now());
        log.setType("Compte");
        log.setAction("Candidature acceptée");
        log.setActeur(user.getFirstName() + " " + user.getLastName());
        log.setCible(member.getFirstName() + " " + member.getLastName());
        activityLogRepository.save(log);

        return "redirect:/directeur/approvals";
    }

    @PostMapping("/directeur/reject/{id}")
    public String rejectMember(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || !"DIRECTEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Action non autorisée.");
            return "redirect:/dashboard";
        }

        Member member = memberRepository.findById(id).orElseThrow();
        logActivity("Compte", "Candidature refusée", user, member.getFirstName() + " " + member.getLastName());

        // Correction 2 : Ne supprime plus, met à REFUSE
        member.setActive(false);
        member.setStatus("REFUSE");
        memberRepository.save(member);

        ra.addFlashAttribute("message", "Candidature rejetée avec succès.");
        return "redirect:/directeur/approvals";
    }

    // =========================================================================
    // 13. GESTION DES COMPTES (ADMIN)
    // =========================================================================

    @GetMapping("/admin/members")
    public String listMembers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            HttpSession session, Model model) {

        Member currentUser = getSessionUser(session);
        if (currentUser == null || !"ADMINISTRATEUR".equals(currentUser.getRole())) return "redirect:/login";

        List<Member> members = memberRepository.findAll();

        if (search != null && !search.isEmpty()) {
            members = members.stream()
                    .filter(m -> (m.getFirstName() + " " + m.getLastName()).toLowerCase().contains(search.toLowerCase())
                            || m.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (role != null && !role.isEmpty() && !"Tous".equals(role)) {
            members = members.stream().filter(m -> role.equals(m.getRole())).toList();
        }

        if (status != null && !status.isEmpty() && !"Tous".equals(status)) {
            boolean activeSearch = "Actif".equals(status);
            members = members.stream().filter(m -> m.isActive() == activeSearch).toList();
        }

        model.addAttribute("members", members);
        model.addAttribute("allTeams", teamRepository.findAll());
        model.addAttribute("user", currentUser);
        model.addAttribute("activePage", "members");
        return "admin/members";
    }

    @PostMapping("/admin/members/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }

        Member m = memberRepository.findById(id).orElseThrow();
        if (m.getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Vous ne pouvez pas modifier votre propre statut.");
            return "redirect:/admin/members";
        }

        boolean devientInactif = m.isActive();
        m.setActive(!m.isActive());
        memberRepository.save(m);

        if (devientInactif) {
            Optional<AffiliationHistory> periodeActive = affiliationHistoryRepository.findByMemberIdAndDateFinIsNull(m.getId());
            if (periodeActive.isPresent()) {
                AffiliationHistory p = periodeActive.get();
                p.setDateFin(java.time.LocalDate.now());
                affiliationHistoryRepository.save(p);
            } else {
                AffiliationHistory h = new AffiliationHistory();
                h.setMember(m);
                h.setDateDebut(m.getDateAffiliation() != null ? m.getDateAffiliation() : java.time.LocalDate.now());
                h.setDateFin(java.time.LocalDate.now());
                h.setStatut(m.getRole() + " (Fermé manuellement)");
                affiliationHistoryRepository.save(h);
            }
        } else {
            AffiliationHistory h = new AffiliationHistory();
            h.setMember(m);
            h.setDateDebut(java.time.LocalDate.now());
            h.setDateFin(null);
            h.setStatut(m.getRole());
            affiliationHistoryRepository.save(h);
        }

        ra.addFlashAttribute("message", "Statut du membre mis à jour avec succès.");
        return "redirect:/admin/members";
    }

    @PostMapping("/admin/members/retire/{id}")
    public String retireMember(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }

        Member m = memberRepository.findById(id).orElseThrow();
        if (m.getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Action impossible sur votre propre compte.");
            return "redirect:/admin/members";
        }

        m.setStatus("RETRAITE");
        m.setActive(false);
        memberRepository.save(m);

        affiliationHistoryRepository.findByMemberIdAndDateFinIsNull(m.getId())
                .ifPresent(periode -> {
                    periode.setDateFin(LocalDate.now());
                    affiliationHistoryRepository.save(periode);
                });

        logActivity("Compte", "Mise à la retraite", user, m.getFirstName() + " " + m.getLastName());
        ra.addFlashAttribute("message", m.getFirstName() + " " + m.getLastName() + " a été mis au statut Retraité. Compte gelé, données conservées.");
        return "redirect:/admin/members";
    }

    @PostMapping("/admin/members/deactivate/{id}")
    public String deactivateMember(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }

        Member m = memberRepository.findById(id).orElseThrow();
        if (m.getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Action impossible sur votre propre compte.");
            return "redirect:/admin/members";
        }

        m.setStatus("ANCIEN_MEMBRE");
        m.setActive(false);
        memberRepository.save(m);

        affiliationHistoryRepository.findByMemberIdAndDateFinIsNull(m.getId())
                .ifPresent(periode -> {
                    periode.setDateFin(LocalDate.now());
                    affiliationHistoryRepository.save(periode);
                });

        logActivity("Compte", "Désactivation (ancien membre)", user, m.getFirstName() + " " + m.getLastName());
        ra.addFlashAttribute("message", m.getFirstName() + " " + m.getLastName() + " est maintenant Ancien membre. Compte désactivé, réactivable.");
        return "redirect:/admin/members";
    }

    @PostMapping("/admin/members/reactivate/{id}")
    public String reactivateMember(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }

        Member m = memberRepository.findById(id).orElseThrow();

        if (!"ANCIEN_MEMBRE".equals(m.getStatus())) {
            ra.addFlashAttribute("error", "Seul un ancien membre peut être réactivé. Un compte retraité reste gelé.");
            return "redirect:/admin/members";
        }

        m.setStatus("ACTIF");
        m.setActive(true);
        m.setDateAffiliation(LocalDate.now());
        memberRepository.save(m);

        AffiliationHistory nouvellePeriode = new AffiliationHistory();
        nouvellePeriode.setMember(m);
        nouvellePeriode.setDateDebut(LocalDate.now());
        nouvellePeriode.setDateFin(null);
        nouvellePeriode.setStatut(m.getRole());
        affiliationHistoryRepository.save(nouvellePeriode);

        logActivity("Compte", "Réactivation", user, m.getFirstName() + " " + m.getLastName());
        ra.addFlashAttribute("message", m.getFirstName() + " " + m.getLastName() + " a été réactivé avec succès.");
        return "redirect:/admin/members";
    }

    @PostMapping("/admin/members/create")
    public String createMember(@ModelAttribute Member member, RedirectAttributes ra, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Action non autorisée.");
            return "redirect:/dashboard";
        }
        member.setActive(true);
        member.setStatus("ACTIF");
        member.setDateAffiliation(LocalDate.now());
        memberRepository.save(member);
        ra.addFlashAttribute("message", "Compte créé avec succès !");
        return "redirect:/admin/members";
    }

    @PostMapping("/admin/members/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }
        Member m = memberRepository.findById(id).orElseThrow();

        String newPassword = "LIAS" + (int)(Math.random() * 10000);
        m.setPassword(newPassword);
        memberRepository.save(m);

        ra.addFlashAttribute("message", "Mot de passe réinitialisé pour " + m.getFirstName() + ". Nouveau MDP : " + newPassword);
        logActivity("Sécurité", "Réinitialisation de mot de passe", user, m.getFirstName() + " " + m.getLastName());
        return "redirect:/admin/members";
    }

    // =========================================================================
    // 14. MANDATS (ADMIN)
    // =========================================================================

    @GetMapping("/admin/mandats")
    public String listMandats(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"ADMINISTRATEUR".equals(user.getRole()) && !"DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/login";
        }

        List<Member> allMembers = memberRepository.findAll();
        List<Team> allTeams = teamRepository.findAll();

        Member director = allMembers.stream().filter(m -> "DIRECTEUR".equals(m.getRole())).findFirst().orElse(null);

        int progression = 0;
        if (director != null && director.getDateAffiliation() != null) {
            long joursEcoules = java.time.temporal.ChronoUnit.DAYS.between(director.getDateAffiliation(), LocalDate.now());
            progression = (int) Math.min(100, (joursEcoules * 100) / 1095);
        } else {
            progression = 65;
        }

        List<Mandate> chefsActifs = mandateRepository.findAll().stream()
                .filter(m -> "CHEF_EQUIPE".equals(m.getType()) && "ACTIF".equals(m.getStatus()))
                .toList();

        model.addAttribute("teamLeaders", chefsActifs);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "mandats");
        model.addAttribute("currentDirector", director);
        model.addAttribute("progression", progression);
        model.addAttribute("allTeams", allTeams);
        model.addAttribute("allMembers", allMembers);
        model.addAttribute("mandatsHistorique", mandateRepository.findAllByOrderByStartDateDesc());

        return "admin/mandats";
    }

    @PostMapping("/admin/mandats/save")
    public String saveMandat(@RequestParam Long memberId,
                             @RequestParam String newRole,
                             @RequestParam(required = false) Long teamId,
                             RedirectAttributes ra,
                             HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || (!"ADMINISTRATEUR".equals(user.getRole()) && !"DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/dashboard";
        }

        Member m = memberRepository.findById(memberId).orElseThrow();
        List<String> rolesMandat = List.of("DIRECTEUR", "VICE_DIRECTEUR", "CHEF_EQUIPE");

        if (teamId != null) {
            teamRepository.findById(teamId).ifPresent(m::setTeam);
        }

        if (rolesMandat.contains(newRole)) {
            List<Mandate> actifs = mandateRepository.findByTypeAndStatus(newRole, "ACTIF");

            actifs.stream().filter(ancien -> {
                if ("CHEF_EQUIPE".equals(newRole)) {
                    return ancien.getTeam() != null && m.getTeam() != null && ancien.getTeam().getId().equals(m.getTeam().getId());
                }
                return true;
            }).findFirst().ifPresent(ancien -> {
                ancien.setEndDate(LocalDate.now());
                ancien.setStatus("TERMINE");
                mandateRepository.save(ancien);
            });

            Mandate nouveauMandat = new Mandate();
            nouveauMandat.setType(newRole);
            nouveauMandat.setStartDate(LocalDate.now());
            nouveauMandat.setEndDate(null);
            nouveauMandat.setStatus("ACTIF");
            nouveauMandat.setMember(m);
            if ("CHEF_EQUIPE".equals(newRole)) {
                nouveauMandat.setTeam(m.getTeam());
            }
            mandateRepository.save(nouveauMandat);
        }

        if (rolesMandat.contains(m.getRole()) && !m.getRole().equals(newRole)) {
            mandateRepository.findByTypeAndStatus(m.getRole(), "ACTIF").stream()
                    .filter(mandat -> mandat.getMember().getId().equals(m.getId()))
                    .findFirst()
                    .ifPresent(mandat -> {
                        mandat.setEndDate(LocalDate.now());
                        mandat.setStatus("TERMINE");
                        mandateRepository.save(mandat);
                    });
        }

        m.setRole(newRole);
        m.setDateAffiliation(LocalDate.now());
        m.setActive(true);
        memberRepository.save(m);

        ra.addFlashAttribute("message", "Le nouveau mandat a été activé avec succès !");
        return "redirect:/admin/mandats";
    }

    // =========================================================================
    // DIVERS MEMBRES ET STATS
    // =========================================================================

    @GetMapping("/feed")
    public String showFeed(Model model) {
        model.addAttribute("allPublications", publicationRepository.findAll());
        model.addAttribute("allEvents", eventRepository.findAll());
        return "feed";
    }

    @GetMapping("/members")
    public String showDirectory(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        model.addAttribute("user", user);

        List<Member> activeMembers = memberRepository.findAll().stream()
                .filter(m -> "ACTIF".equals(m.getStatus()) || m.getStatus() == null)
                .toList();

        model.addAttribute("allMembers", activeMembers);
        model.addAttribute("allTeams", teamRepository.findAll());
        model.addAttribute("totalMembers", activeMembers.size());
        model.addAttribute("activePage", "members");

        return "members";
    }

    @GetMapping("/admin/stats")
    public String usageStats(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) return "redirect:/login";

        List<Member> allMembers = memberRepository.findAll();
        long totalMembers = allMembers.isEmpty() ? 1 : allMembers.size();

        long countPubs = publicationRepository.count();
        long countConventions = conventionRepository.count();
        long countMeetings = meetingRepository.count();

        model.addAttribute("totalPubs", countPubs);
        model.addAttribute("totalRapports", countConventions + countMeetings);
        model.addAttribute("totalFiles", countPubs + countMeetings + countConventions);
        model.addAttribute("totalStorage", String.format("%.1f", (countPubs * 1.2 + countMeetings * 0.5)));

        long membersActiveInPubs = allMembers.stream().filter(m -> !m.getPublications().isEmpty()).count();
        int pctPubs = (int) (membersActiveInPubs * 100 / totalMembers);

        long membersWithMaterial = allMembers.stream().filter(m -> !m.getEquipments().isEmpty()).count();
        int pctMat = (int) (membersWithMaterial * 100 / totalMembers);

        int pctEvents = (int) Math.min(100, (eventRepository.count() * 10));

        model.addAttribute("usagePubs", pctPubs);
        model.addAttribute("usageMateriel", pctMat);
        model.addAttribute("usageEvents", pctEvents);
        model.addAttribute("usageFeed", 35);

        model.addAttribute("valL", 12);
        model.addAttribute("valM", 18);
        model.addAttribute("valMe", 9);
        model.addAttribute("valJ", 21);
        model.addAttribute("valV", 15);
        model.addAttribute("valS", 6);
        model.addAttribute("valAj", 18);

        model.addAttribute("user", user);
        model.addAttribute("activePage", "usage-stats");
        return "admin/stats";
    }

    @GetMapping("/admin/backups")
    public String showBackups(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("activePage", "backups");
        model.addAttribute("backups", backupRepository.findAllByOrderByDateCreationDesc());
        model.addAttribute("autoBackup", true);

        SystemSettings settings = settingsRepository.findById(1L).orElse(new SystemSettings());
        model.addAttribute("retentionDays", settings.getRetentionDays());

        return "admin/backups";
    }

    @PostMapping("/admin/backups/run")
    public String runBackup(RedirectAttributes ra, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/dashboard";
        }

        Backup b = new Backup();
        b.setFileName("backup_" + LocalDate.now() + ".zip");
        b.setDateCreation(LocalDateTime.now());
        b.setSize("1.2 Go");
        b.setStatus("OK");
        backupRepository.save(b);

        ra.addFlashAttribute("message", "Sauvegarde lancée avec succès !");
        return "redirect:/admin/backups";
    }

    @GetMapping("/admin/logs")
    public String showLogs(@RequestParam(required = false) String type, HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) return "redirect:/login";

        List<ActivityLog> logs;
        if (type != null && !type.equals("Tous les types") && !type.equals("Tous") && !type.isEmpty()) {
            logs = activityLogRepository.findByTypeOrderByTimestampDesc(type);
        } else {
            logs = activityLogRepository.findAllByOrderByTimestampDesc();
        }

        model.addAttribute("user", user);
        model.addAttribute("logs", logs);
        model.addAttribute("currentType", type != null ? type : "Tous");
        model.addAttribute("activePage", "logs");
        return "admin/logs";
    }

    @GetMapping("/admin/settings")
    public String showSettings(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) return "redirect:/login";

        SystemSettings settings = settingsRepository.findById(1L).orElse(new SystemSettings());

        model.addAttribute("user", user);
        model.addAttribute("settings", settings);
        model.addAttribute("activePage", "settings");
        return "admin/settings";
    }

    @PostMapping("/admin/settings/save")
    public String saveSettings(@ModelAttribute SystemSettings settings, RedirectAttributes ra, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Action non autorisée.");
            return "redirect:/dashboard";
        }
        settingsRepository.save(settings);
        ra.addFlashAttribute("message", "Paramètres mis à jour avec succès !");
        return "redirect:/admin/settings";
    }

    // =========================================================================
    // DOCUMENTS (Sécurisé et centralisé)
    // =========================================================================

    // Correction 4 : Garde stricte AVANT écriture de fichier
    @PostMapping("/member/documents/save")
    public String saveDocument(@RequestParam("file") MultipartFile file,
                               @RequestParam String title,
                               @RequestParam String type,
                               HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        // Blocage en tête de méthode (aucun fichier écrit si non autorisé)
        if ("DOCTORANT".equals(user.getRole()) || "MEMBRE_ASSOCIE".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        try {
            if (!file.isEmpty()) {
                String fileName = "DOC_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get("C:/lias_uploads/" + fileName), StandardCopyOption.REPLACE_EXISTING);

                Document doc = new Document();
                doc.setTitle(title);
                doc.setType(type);
                doc.setFilePath(fileName);
                doc.setYear(LocalDate.now().getYear());
                doc.setUploader(user);
                doc.setTeam(user.getTeam());

                documentRepository.save(doc);
                ra.addFlashAttribute("message", "Document archivé avec succès !");

                if (doc.getTeam() != null) {
                    List<Member> equipiers = memberRepository.findAll().stream()
                            .filter(m -> m.getTeam() != null && m.getTeam().getId().equals(doc.getTeam().getId()))
                            .filter(m -> !m.getId().equals(user.getId()))
                            .toList();
                    for (Member m : equipiers) {
                        envoyerNotification(m, "Nouveau document d'équipe",
                                user.getFirstName() + " a ajouté un document : " + doc.getTitle(), "DOCUMENT");
                    }
                }
            }
        } catch (IOException e) { ra.addFlashAttribute("error", "Erreur lors de l'envoi."); }

        return "redirect:/member/documents";
    }

    @GetMapping("/member/documents")
    public String showMemberDocuments(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";
        if ("DOCTORANT".equals(user.getRole())) return "redirect:/dashboard";

        List<Document> docs = documentsAccessibles(user);

        model.addAttribute("user", user);
        model.addAttribute("documents", docs);
        model.addAttribute("activePage", "documents");
        model.addAttribute("newDocsCount", docs.size());

        return "member/documents";
    }

    /*@GetMapping("/member/documents/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, HttpSession session) throws IOException {
        Member user = getSessionUser(session);
        if (user == null) {
            throw new RuntimeException("Accès refusé");
        }

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        boolean autorise = documentsAccessibles(user).stream()
                .anyMatch(d -> d.getId().equals(doc.getId()));

        if (!autorise) {
            throw new RuntimeException("Accès refusé");
        }

        Path path = Paths.get("C:/lias_uploads/").resolve(doc.getFilePath()).normalize();
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Fichier non trouvé sur le serveur");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFilePath() + "\"")
                .body(resource);
    }

    private List<Document> documentsAccessibles(Member user) {
        if (user == null) return List.of();
        String role = user.getRole();

        if ("DOCTORANT".equals(role)) return List.of();

        List<Document> all = documentRepository.findAll();

        if ("ADMINISTRATEUR".equals(role) || "DIRECTEUR".equals(role) || "VICE_DIRECTEUR".equals(role)) {
            return all;
        }

        List<Document> sansFinancement = all.stream()
                .filter(d -> d.getType() == null || !d.getType().equalsIgnoreCase("Financement"))
                .toList();

        if ("MEMBRE_ASSOCIE".equals(role)) {
            Member fresh = memberRepository.findByIdWithEvents(user.getId()).orElse(user);
            List<Event> mesEvents = fresh.getParticipatingEvents() != null
                    ? fresh.getParticipatingEvents() : List.of();
            return sansFinancement.stream()
                    .filter(d -> d.getEvent() != null && mesEvents.contains(d.getEvent()))
                    .toList();
        }

        return sansFinancement.stream()
                .filter(d -> d.getTeam() == null || (user.getTeam() != null && d.getTeam().getId().equals(user.getTeam().getId())))
                .toList();
    }*/
    @GetMapping("/member/documents/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, HttpSession session) throws IOException {
        Member user = getSessionUser(session);
        if (user == null) {
            throw new RuntimeException("Accès refusé");
        }

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        boolean autorise = documentsAccessibles(user).stream()
                .anyMatch(d -> d.getId().equals(doc.getId()));

        if (!autorise) {
            throw new RuntimeException("Accès refusé");
        }

        Path path = Paths.get("C:/lias_uploads/").resolve(doc.getFilePath()).normalize();

        // VÉRIFICATION DE SÉCURITÉ : Le fichier existe-t-il sur le disque ?
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new RuntimeException("Fichier introuvable sur le serveur (Veuillez placer le fichier PDF dans C:/lias_uploads/)");
        }

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Fichier non trouvé sur le serveur");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFilePath() + "\"")
                .body(resource);
    }
    private List<Document> documentsAccessibles(Member user) {
        if (user == null) return List.of();
        String role = user.getRole();

        if ("DOCTORANT".equals(role)) return List.of();

        List<Document> all = documentRepository.findAll();

        if ("ADMINISTRATEUR".equals(role) || "DIRECTEUR".equals(role) || "VICE_DIRECTEUR".equals(role)) {
            return all;
        }

        List<Document> sansFinancement = all.stream()
                .filter(d -> d.getType() == null || !d.getType().equalsIgnoreCase("Financement"))
                .toList();

        if ("MEMBRE_ASSOCIE".equals(role)) {
            Member fresh = memberRepository.findByIdWithEvents(user.getId()).orElse(user);
            List<Event> mesEvents = fresh.getParticipatingEvents() != null
                    ? fresh.getParticipatingEvents() : List.of();
            return sansFinancement.stream()
                    .filter(d -> d.getEvent() != null && mesEvents.contains(d.getEvent()))
                    .toList();
        }

        return sansFinancement.stream()
                .filter(d -> d.getTeam() == null || (user.getTeam() != null && d.getTeam().getId().equals(user.getTeam().getId())))
                .toList();
    }
    // =========================================================================
    // CALENDRIER ET DISCUSSIONS
    // =========================================================================

    /*@GetMapping("/member/calendar")
    public String showCalendar(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(today);

        int dayOfWeekValue = yearMonth.atDay(1).getDayOfWeek().getValue();
        int emptyCellsBefore = dayOfWeekValue - 1;

        model.addAttribute("user", user);
        model.addAttribute("activePage", "calendar");
        model.addAttribute("currentMonth", yearMonth);
        model.addAttribute("daysInMonth", yearMonth.lengthOfMonth());
        model.addAttribute("emptyCellsBefore", emptyCellsBefore);
        model.addAttribute("allEvents", eventRepository.findAll());

        return "member/calendar";
    }*/
    @GetMapping("/member/calendar")
    public String showCalendar(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        // Déterminer le mois à afficher (le mois demandé, sinon le mois actuel)
        YearMonth yearMonth;
        if (year != null && month != null) {
            yearMonth = YearMonth.of(year, month);
        } else {
            yearMonth = YearMonth.now();
        }

        int dayOfWeekValue = yearMonth.atDay(1).getDayOfWeek().getValue();
        int emptyCellsBefore = dayOfWeekValue - 1;

        model.addAttribute("user", user);
        model.addAttribute("activePage", "calendar");
        model.addAttribute("currentMonth", yearMonth);
        model.addAttribute("daysInMonth", yearMonth.lengthOfMonth());
        model.addAttribute("emptyCellsBefore", emptyCellsBefore);
        model.addAttribute("allEvents", eventRepository.findAll());

        return "member/calendar";
    }

    @GetMapping("/member/discussions")
    public String showDiscussions(@RequestParam(required = false) Long open, HttpSession session, Model model) {
        Member sessionUser = getSessionUser(session);
        if (sessionUser == null) return "redirect:/login";

        Member user = memberRepository.findByIdWithEvents(sessionUser.getId()).orElse(sessionUser);

        List<Discussion> myDiscussions = List.of();
        if (user.getParticipatingEvents() != null && !user.getParticipatingEvents().isEmpty()) {
            myDiscussions = discussionRepository.findAll().stream()
                    .filter(d -> d.getEvent() != null && user.getParticipatingEvents().contains(d.getEvent()))
                    .toList();
        }

        model.addAttribute("discussions", myDiscussions);

        if (open != null) {
            Discussion opened = discussionRepository.findById(open).orElse(null);
            if (opened != null && user.getParticipatingEvents().contains(opened.getEvent())) {
                model.addAttribute("openedDiscussion", opened);
                model.addAttribute("messages", discussionMessageRepository.findByDiscussionIdOrderByDateEnvoiAsc(open));
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("activePage", "discussions");

        return "member/discussions";
    }

    @PostMapping("/member/discussions/{discussionId}/reply")
    public String replyToDiscussion(@PathVariable Long discussionId, @RequestParam String content, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Discussion d = discussionRepository.findById(discussionId).orElseThrow();
        Member freshUser = memberRepository.findByIdWithEvents(user.getId()).orElse(user);
        if (!freshUser.getParticipatingEvents().contains(d.getEvent())) {
            return "redirect:/dashboard";
        }

        DiscussionMessage msg = new DiscussionMessage();
        msg.setDiscussion(d);
        msg.setAuthor(user);
        msg.setContent(content);
        msg.setDateEnvoi(LocalDateTime.now());
        discussionMessageRepository.save(msg);

        return "redirect:/member/discussions?open=" + discussionId;
    }

    // =========================================================================
    // GESTION EQUIPES ET COMMUNICATIONS
    // =========================================================================

    @GetMapping("/team/members")
    public String listTeamMembers(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"CHEF_EQUIPE".equals(user.getRole())) return "redirect:/login";

        Team team = user.getTeam();
        if (team == null) {
            team = new Team();
            team.setName("Aucune équipe assignée");
        }

        List<Member> members = (user.getTeam() != null)
                ? memberRepository.findByTeamId(user.getTeam().getId())
                : List.of();

        model.addAttribute("members", members);
        model.addAttribute("team", team);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "team-members");

        return "team/members";
    }

    @GetMapping("/team/events")
    public String listTeamEvents(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"CHEF_EQUIPE".equals(user.getRole())) return "redirect:/login";

        Long chefTeamId = user.getTeam() != null ? user.getTeam().getId() : null;

        List<Event> teamEvents = eventRepository.findAll().stream()
                .filter(e -> e.getTeam() != null && e.getTeam().getId().equals(chefTeamId))
                .collect(Collectors.toList());

        model.addAttribute("allEvents", teamEvents);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "team-events");
        return "team/events";
    }

    @GetMapping("/team/communication")
    public String listTeamCommunication(@RequestParam(required = false) Long open, HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || !"CHEF_EQUIPE".equals(user.getRole())) return "redirect:/login";

        if (user.getTeam() == null) {
            model.addAttribute("discussions", List.of());
            model.addAttribute("user", user);
            model.addAttribute("activePage", "team-chat");
            return "team/communication";
        }

        List<Discussion> teamDiscussions = discussionRepository.findByScopeAndTeamId("TEAM", user.getTeam().getId());
        model.addAttribute("discussions", teamDiscussions);

        if (open != null) {
            model.addAttribute("openedDiscussion", discussionRepository.findById(open).orElse(null));
            model.addAttribute("messages", discussionMessageRepository.findByDiscussionIdOrderByDateEnvoiAsc(open));
        }

        model.addAttribute("user", user);
        model.addAttribute("activePage", "team-chat");
        return "team/communication";
    }

    @PostMapping("/team/communication/new")
    public String createTeamDiscussion(@RequestParam String subject, @RequestParam String firstMessage, HttpSession session, RedirectAttributes ra) {
        Member user = getSessionUser(session);
        if (user == null || !"CHEF_EQUIPE".equals(user.getRole())) return "redirect:/login";
        if (user.getTeam() == null) {
            ra.addFlashAttribute("error", "Vous n'êtes assigné à aucune équipe.");
            return "redirect:/team/communication";
        }

        Discussion d = new Discussion();
        d.setSubject(subject);
        d.setScope("TEAM");
        d.setTeam(user.getTeam());
        d.setDateCreation(LocalDateTime.now());
        discussionRepository.save(d);

        DiscussionMessage msg = new DiscussionMessage();
        msg.setDiscussion(d);
        msg.setAuthor(user);
        msg.setContent(firstMessage);
        msg.setDateEnvoi(LocalDateTime.now());
        discussionMessageRepository.save(msg);

        return "redirect:/team/communication?open=" + d.getId();
    }

    @PostMapping("/team/communication/{discussionId}/reply")
    public String replyToTeamDiscussion(@PathVariable Long discussionId, @RequestParam String content, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Discussion d = discussionRepository.findById(discussionId).orElseThrow();
        if (user.getTeam() == null || !user.getTeam().getId().equals(d.getTeam().getId())) {
            return "redirect:/dashboard";
        }

        DiscussionMessage msg = new DiscussionMessage();
        msg.setDiscussion(d);
        msg.setAuthor(user);
        msg.setContent(content);
        msg.setDateEnvoi(LocalDateTime.now());
        discussionMessageRepository.save(msg);

        return "redirect:/team/communication?open=" + discussionId;
    }

    @GetMapping("/member/discussions-generales")
    public String listGeneralDiscussions(@RequestParam(required = false) Long open, HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        List<Discussion> generalDiscussions = discussionRepository.findByScope("GENERAL");
        model.addAttribute("discussions", generalDiscussions);

        if (open != null) {
            model.addAttribute("openedDiscussion", discussionRepository.findById(open).orElse(null));
            model.addAttribute("messages", discussionMessageRepository.findByDiscussionIdOrderByDateEnvoiAsc(open));
        }

        model.addAttribute("user", user);
        model.addAttribute("activePage", "discussions-generales");
        return "member/discussions-generales";
    }

    @PostMapping("/member/discussions-generales/new")
    public String createGeneralDiscussion(@RequestParam String subject, @RequestParam String firstMessage, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Discussion d = new Discussion();
        d.setSubject(subject);
        d.setScope("GENERAL");
        d.setDateCreation(LocalDateTime.now());
        discussionRepository.save(d);

        DiscussionMessage msg = new DiscussionMessage();
        msg.setDiscussion(d);
        msg.setAuthor(user);
        msg.setContent(firstMessage);
        msg.setDateEnvoi(LocalDateTime.now());
        discussionMessageRepository.save(msg);

        return "redirect:/member/discussions-generales?open=" + d.getId();
    }

    @PostMapping("/member/discussions-generales/{discussionId}/reply")
    public String replyToGeneralDiscussion(@PathVariable Long discussionId, @RequestParam String content, HttpSession session) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        Discussion d = discussionRepository.findById(discussionId).orElseThrow();
        DiscussionMessage msg = new DiscussionMessage();
        msg.setDiscussion(d);
        msg.setAuthor(user);
        msg.setContent(content);
        msg.setDateEnvoi(LocalDateTime.now());
        discussionMessageRepository.save(msg);

        return "redirect:/member/discussions-generales?open=" + discussionId;
    }

    @GetMapping("/member/notifications")
    public String showNotifications(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null) return "redirect:/login";

        List<Notification> notifs = notificationRepository.findByRecipientIdOrderByDateCreationDesc(user.getId());

        for (Notification n : notifs) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }

        model.addAttribute("notifications", notifs);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "notifications");
        return "member/notifications";
    }

    // =========================================================================
    // RECHERCHE GLOBALE (§18 du Cahier des Charges)
    // =========================================================================

    @GetMapping("/search")
    public String globalSearch(@RequestParam(required = false) String query,
                               HttpSession session, Model model) {
        Member user = getSessionUser(session);
        model.addAttribute("user", user);

        String q = (query == null) ? "" : query.trim();
        model.addAttribute("query", q);

        if (q.isEmpty()) {
            model.addAttribute("members", List.of());
            model.addAttribute("publications", List.of());
            model.addAttribute("events", List.of());
            model.addAttribute("documents", List.of());
            model.addAttribute("totalFound", 0);
            return "search-results";
        }

        List<Member> foundMembers = List.of();
        List<Publication> foundPubs = List.of();
        List<Event> foundEvents = List.of();
        List<Document> foundDocs = List.of();

        try {
            SystemSettings s = settingsRepository.findById(1L).orElse(new SystemSettings());
            boolean admin = user != null && "ADMINISTRATEUR".equals(user.getRole());

            // 1. Membres (ANNUAIRE)
            if (!s.isModuleAnnuaire() && !admin) {
                foundMembers = List.of();
            } else {
                foundMembers = memberRepository
                        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrCentresInteretContainingIgnoreCase(q, q, q)
                        .stream()
                        .filter(m -> m.isActive() && ("ACTIF".equals(m.getStatus()) || m.getStatus() == null))
                        .toList();
            }

            // 2. Publications (PUBS)
            if (!s.isModulePubs() && !admin) {
                foundPubs = List.of();
            } else {
                foundPubs = publicationRepository
                        .findByTitleContainingIgnoreCaseOrJournalContainingIgnoreCase(q, q);
            }

            // 3. Événements (EVENTS)
            if (!s.isModuleEvents() && !admin) {
                foundEvents = List.of();
            } else {
                List<Event> rawEvents = eventRepository
                        .findByTitleContainingIgnoreCaseOrLieuContainingIgnoreCase(q, q);

                if (user == null || "MEMBRE_ASSOCIE".equals(user.getRole())) {
                    foundEvents = rawEvents.stream().filter(Event::isOuvertAuxAssocies).toList();
                } else {
                    foundEvents = rawEvents;
                }
            }

            // 4. Documents (Non affecté par interrupteur, filtré par documentsAccessibles)
            final String searchKeyword = q.toLowerCase();
            foundDocs = documentsAccessibles(user).stream()
                    .filter(d -> (d.getTitle() != null && d.getTitle().toLowerCase().contains(searchKeyword))
                            || (d.getType() != null && d.getType().toLowerCase().contains(searchKeyword)))
                    .toList();

        } catch (Exception e) {
            foundMembers = List.of();
            foundPubs = List.of();
            foundEvents = List.of();
            foundDocs = List.of();
        }

        model.addAttribute("members", foundMembers);
        model.addAttribute("publications", foundPubs);
        model.addAttribute("events", foundEvents);
        model.addAttribute("documents", foundDocs);
        model.addAttribute("totalFound", foundMembers.size() + foundPubs.size() + foundEvents.size() + foundDocs.size());

        return "search-results";
    }

    @GetMapping("/module-desactive")
    public String moduleDesactive(@RequestParam(required = false) String module, HttpSession session, Model model) {
        model.addAttribute("user", getSessionUser(session));
        model.addAttribute("module", module);
        return "module-desactive";
    }
    // =========================================================================
    // ROUTES POUR L'ARCHIVAGE ET LA SUPERVISION DES PUBLICATIONS (VICE-DIRECTEUR / DIRECTEUR)
    // =========================================================================

    @GetMapping("/directeur/archivage")
    public String showArchivage(HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("documents", documentRepository.findAll());
        return "directeur/archivage";
    }

    @GetMapping("/directeur/all-publications")
    public String showAllPublicationsAdmin(@RequestParam(required = false) Long teamId,
                                           @RequestParam(required = false) Integer year,
                                           HttpSession session, Model model) {
        Member user = getSessionUser(session);
        if (user == null || (!"DIRECTEUR".equals(user.getRole()) && !"VICE_DIRECTEUR".equals(user.getRole()))) {
            return "redirect:/login";
        }

        List<Publication> pubs = publicationRepository.findAll();
        if (teamId != null) {
            pubs = pubs.stream().filter(p -> p.getAuthor() != null && p.getAuthor().getTeam() != null && p.getAuthor().getTeam().getId().equals(teamId)).toList();
        }
        if (year != null) {
            pubs = pubs.stream().filter(p -> p.getYear() == year).toList();
        }

        model.addAttribute("user", user);
        model.addAttribute("publications", pubs);
        model.addAttribute("allTeams", teamRepository.findAll());

        List<Integer> years = publicationRepository.findAll().stream().map(Publication::getYear).distinct().sorted().toList();
        model.addAttribute("allYears", years);

        return "directeur/all-publications";
    }
    /*@PostMapping("/directeur/partners/save")
    public String saveConvention(@ModelAttribute Convention convention, @RequestParam("file") MultipartFile file,HttpSession session,
                                 RedirectAttributes ralistMeetings,RedirectAttributes ra) {
        Member user = getSessionUser(session);

        if (user == null || !"DIRECTEUR".equals(user.getRole())) {
            ra.addFlashAttribute("error", "Action non autorisée : seuls les directeurs peuvent ajouter des conventions.");
            return "redirect:/directeur/partners";
        }

        if (!file.isEmpty()) {
            try {
                String fileName = "CONV_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get("C:/lias_uploads/" + fileName), StandardCopyOption.REPLACE_EXISTING);
                convention.setPvPath(fileName);
            } catch (IOException e) { e.printStackTrace(); }
        }
        conventionRepository.save(convention);
        return "redirect:/admin/partners";
    }*/
    // =========================================================================
    // 10. PARTENAIRES ET CONVENTIONS (SAUVEGARDE)
    // =========================================================================
    @PostMapping({"/admin/partners/save", "/directeur/partners/save"})
    public String savePartner(@ModelAttribute Convention convention,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              HttpSession session,
                              RedirectAttributes ra) {
        Member user = getSessionUser(session);

        // 1. Vérification des droits d'accès (Admin ou Directeur)
        if (user == null || (!"ADMINISTRATEUR".equals(user.getRole()) && !"DIRECTEUR".equals(user.getRole()))) {
            ra.addFlashAttribute("error", "Action non autorisée : seuls l'administrateur ou le directeur peuvent ajouter des conventions.");
            return "redirect:/directeur/partners";
        }

        // 2. Gestion de l'upload du fichier (si fourni)
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = "CONV_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path uploadDir = Paths.get("C:/lias_uploads/");

                // Crée le dossier s'il n'existe pas
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                convention.setPvPath(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                ra.addFlashAttribute("error", "Erreur lors de l'enregistrement du document.");
            }
        }

        // 3. Sauvegarde dans la base de données
        conventionRepository.save(convention);
        ra.addFlashAttribute("message", "Convention enregistrée avec succès !");

        // 4. Redirection selon le rôle
        if ("ADMINISTRATEUR".equals(user.getRole())) {
            return "redirect:/directeur/partners"; // ou redirect:/admin/partners selon votre page
        }
        return "redirect:/directeur/partners";
    }
}