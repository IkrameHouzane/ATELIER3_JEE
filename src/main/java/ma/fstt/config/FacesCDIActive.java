package ma.fstt.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.FacesConfig;

/**
 * Configuration pour activer CDI avec JSF
 * Cette classe est nécessaire pour que JSF utilise CDI
 */
@ApplicationScoped
@FacesConfig
public class FacesCDIActive {
    // on n a Pas besoin de code ici, les annotations suffisent
}