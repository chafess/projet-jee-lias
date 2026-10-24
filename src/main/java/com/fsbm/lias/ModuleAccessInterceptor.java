/*package com.fsbm.lias;

public class ModuleAccessInterceptor {
}*/
package com.fsbm.lias;

import com.fsbm.lias.model.Member;
import com.fsbm.lias.model.SystemSettings;
import com.fsbm.lias.repository.SystemSettingsRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ModuleAccessInterceptor implements HandlerInterceptor {

    @Autowired private SystemSettingsRepository settingsRepository;

    private boolean matches(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private String moduleFor(String path) {
        if (matches(path, "/publications") || matches(path, "/member/my-publications")
                || matches(path, "/member/publications")
                || matches(path, "/directeur/all-publications")) return "PUBS";
        if (matches(path, "/member/request-material")
                || matches(path, "/directeur/material-requests")
                || matches(path, "/directeur/equipment")) return "MATERIEL";
        if (matches(path, "/events") || matches(path, "/member/events")
                || matches(path, "/member/calendar")
                || matches(path, "/directeur/events")
                || matches(path, "/team/events")) return "EVENTS";
        if (matches(path, "/feed") || matches(path, "/member/actualites")
                || matches(path, "/directeur/actualites")) return "FEED";
        if (matches(path, "/members")) return "ANNUAIRE";
        return null;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String module = moduleFor(req.getRequestURI());
        if (module == null) return true; // Si ce n'est pas une page de module, on laisse passer

        HttpSession session = req.getSession(false);
        Member user = (session != null) ? (Member) session.getAttribute("loggedInUser") : null;

        // L'ADMINISTRATEUR N'EST JAMAIS BLOQUÉ
        if (user != null && "ADMINISTRATEUR".equals(user.getRole())) return true;

        SystemSettings s = settingsRepository.findById(1L).orElse(new SystemSettings());
        boolean actif = switch (module) {
            case "PUBS" -> s.isModulePubs();
            case "MATERIEL" -> s.isModuleMateriel();
            case "EVENTS" -> s.isModuleEvents();
            case "FEED" -> s.isModuleFeed();
            case "ANNUAIRE" -> s.isModuleAnnuaire();
            default -> true;
        };

        if (actif) return true; // Le module est allumé, on laisse passer

        // MODULE ÉTEINT -> ON REDIRIGE VERS LA PAGE D'ERREUR PROPRE
        res.sendRedirect("/module-desactive?module=" + module);
        return false;
    }
}