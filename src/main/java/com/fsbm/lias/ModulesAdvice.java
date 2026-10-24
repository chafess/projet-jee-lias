/*package com.fsbm.lias;

public class ModulesAdvice {
}*/
package com.fsbm.lias;

import com.fsbm.lias.model.SystemSettings;
import com.fsbm.lias.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ModulesAdvice {

    @Autowired
    private SystemSettingsRepository settingsRepository;

    @ModelAttribute("modules")
    public SystemSettings addModulesToModel() {
        // Récupère les paramètres (ID 1) ou retourne des valeurs par défaut si vide
        return settingsRepository.findById(1L).orElse(new SystemSettings());
    }
}
